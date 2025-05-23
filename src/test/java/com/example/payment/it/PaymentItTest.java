package com.example.payment.it;

import com.example.payment.InstantPaymentApiApplication;
import com.example.payment.config.AppProperties; 
import com.example.payment.config.KafkaContainerConfig;
import com.example.payment.config.PostgreSQLContainerConfig;
import com.example.payment.config.RedisContainerConfig;
import com.example.payment.dto.TransferRequest; 
import com.example.payment.repository.AccountRepository;
import com.example.payment.repository.TransactionRepository;
import com.example.payment.model.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.kafka.clients.consumer.ConsumerConfig; 
import org.apache.kafka.clients.consumer.ConsumerRecord; 
import org.apache.kafka.clients.consumer.ConsumerRecords; 
import org.apache.kafka.clients.consumer.KafkaConsumer; 
import org.apache.kafka.common.serialization.StringDeserializer; 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.math.BigDecimal;
import java.time.Duration; 
import java.util.Collections; 
import java.util.Properties; 
import java.util.UUID; 
import java.util.concurrent.TimeUnit; 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; 
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.support.serializer.JsonDeserializer; 
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@ContextConfiguration(classes = {InstantPaymentApiApplication.class, KafkaContainerConfig.class,
    PostgreSQLContainerConfig.class,
    RedisContainerConfig.class},
    initializers = {KafkaContainerConfig.class, PostgreSQLContainerConfig.class, RedisContainerConfig.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PaymentItTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AppProperties appProperties; // Added autowiring for AppProperties

    @Value("${spring.kafka.bootstrap-servers}") // Added value injection
    private String kafkaBootstrapServers;

    @BeforeEach
    void setUp() {
        // Reset account balances to initial state before each test
        // Assuming accounts 1 and 2 are created by init.sql with these initial balances
        var account1 = accountRepository.findById(1L).orElseThrow();
        account1.setBalance(new BigDecimal("1000.00"));
        accountRepository.save(account1);

        var account2 = accountRepository.findById(2L).orElseThrow();
        account2.setBalance(new BigDecimal("500.00"));
        accountRepository.save(account2);
    }

    @Test
    void shouldReturnOk_whenTransferIsValid() throws Exception {
        // given
        // Accounts 1 and 2 are created by init.sql with sufficient balance for account 1
        String requestBody = """
            {
              "sender_id": 1,
              "recipient_id": 2,
              "amount": "100.00"
            }
            """;

        // when
        var response =
            mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        response.andExpect(status().isOk());
    }

    @Test
    void shouldTransferFundsCorrectly_andUpdateAccountBalances() throws Exception {
        // given
        // Initial balances from init.sql
        String requestBody = """
            {
              "sender_id": 1,
              "recipient_id": 2,
              "amount": "50.00"
            }
            """;

        // when - execute the payment
        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk());

        // then - verify balances were updated correctly in the database 

        var senderAccount = accountRepository.findById(1L).orElseThrow();
        var recipientAccount = accountRepository.findById(2L).orElseThrow();

        assertThat(senderAccount.getBalance()).isEqualByComparingTo(new BigDecimal("950.00")); // 1000 - 50
        assertThat(recipientAccount.getBalance()).isEqualByComparingTo(new BigDecimal("550.00")); // 500 + 50
    }

    @Test
    void shouldReturnNotFound_whenSenderAccountDoesNotExist() throws Exception {
        // given
        String requestBody = """
            {
              "sender_id": 999,
              "recipient_id": 2,
              "amount": "100.00"
            }
            """;

        // when
        var response =
            mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        response.andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFound_whenRecipientAccountDoesNotExist() throws Exception {
        // given
        String requestBody = """
            {
              "sender_id": 1,
              "recipient_id": 998,
              "amount": "100.00"
            }
            """;

        // when
        var response =
            mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        response.andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequest_whenSenderHasInsufficientFunds() throws Exception {
        // given
        // Account 1 (sender) has an initial balance of 1000.00 (from setUp)
        String requestBody = """
            {
              "sender_id": 1,
              "recipient_id": 2,
              "amount": "2000.00" 
            }
            """; // Amount exceeds sender's balance

        // when
        var response =
            mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        response.andExpect(status().isForbidden());

        // Verify balances remain unchanged
        var senderAccount = accountRepository.findById(1L).orElseThrow();
        var recipientAccount = accountRepository.findById(2L).orElseThrow();

        assertThat(senderAccount.getBalance()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(recipientAccount.getBalance()).isEqualByComparingTo(new BigDecimal("500.00"));
    }

    @Test
    void shouldTransferExactAvailableBalance_andSetSenderBalanceToZero() throws Exception {
        // given
        // Account 1 (sender) has an initial balance of 1000.00 (from setUp)
        // Account 2 (recipient) has an initial balance of 500.00 (from setUp)
        String requestBody = """
            {
              "sender_id": 1,
              "recipient_id": 2,
              "amount": "1000.00"
            }
            """;

        // when
        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk());

        // then
        var senderAccount = accountRepository.findById(1L).orElseThrow();
        var recipientAccount = accountRepository.findById(2L).orElseThrow();

        assertThat(senderAccount.getBalance()).isEqualByComparingTo(new BigDecimal("0.00"));
        assertThat(recipientAccount.getBalance()).isEqualByComparingTo(new BigDecimal("1500.00")); // 500 + 1000
    }

    @Test
    void shouldStoreTransaction_whenPaymentIsSuccessful() throws Exception {
        // given
        long senderId = 1L;
        long recipientId = 2L;
        BigDecimal amount = new BigDecimal("75.00");

        String requestBody = String.format("""
            {
              "sender_id": %d,
              "recipient_id": %d,
              "amount": "%s"
            }
            """, senderId, recipientId, amount.toPlainString());

        // when
        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk());

        // then
        // Verify transaction was stored correctly
        var transactions = transactionRepository.findAll();
        
        Transaction storedTransaction = transactions.stream()
            .filter(t -> t.getSenderId().equals(senderId) &&
                         t.getRecipientId().equals(recipientId) &&
                         t.getAmount().compareTo(amount) == 0)
            .findFirst()
            .orElseThrow(() -> new AssertionError("Transaction not found for the payment"));

        assertThat(storedTransaction).isNotNull();
        assertThat(storedTransaction.getSenderId()).isEqualTo(senderId);
        assertThat(storedTransaction.getRecipientId()).isEqualTo(recipientId);
        assertThat(storedTransaction.getAmount()).isEqualByComparingTo(amount);
        assertThat(storedTransaction.getTimestamp()).isNotNull();
    }

    @Test
    void shouldProduceKafkaMessage_whenPaymentIsSuccessful() throws Exception {
        // given
        long senderId = 1L;
        long recipientId = 2L;
        BigDecimal amount = new BigDecimal("25.00");
        TransferRequest expectedTransferRequest = new TransferRequest(senderId, recipientId, amount);

        String requestBody = objectMapper.writeValueAsString(expectedTransferRequest);

        // Kafka Consumer Setup
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers); // Use injected property
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "payment-it-test-consumer-" + UUID.randomUUID());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.example.payment.dto");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.example.payment.dto.TransferRequest");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, TransferRequest> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(appProperties.getKafkaTopics().getTransactionCreated()));

        try {
            // when
            mockMvc.perform(post("/api/payments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isOk());

            // then - verify Kafka message
            ConsumerRecords<String, TransferRequest> records = consumer.poll(Duration.ofSeconds(10)); // Wait up to 10 seconds

            assertThat(records.isEmpty()).isFalse();

            boolean messageFound = false;
            for (ConsumerRecord<String, TransferRequest> record : records) {
                TransferRequest receivedRequest = record.value();
                if (receivedRequest.getSenderId().equals(senderId) &&
                    receivedRequest.getRecipientId().equals(recipientId) &&
                    receivedRequest.getAmount().compareTo(amount) == 0) {
                    messageFound = true;
                    assertThat(record.key()).isEqualTo(String.valueOf(senderId));
                    break;
                }
            }
            assertThat(messageFound).isTrue().withFailMessage("Kafka message for the transaction was not found.");

        } finally {
            consumer.close();
        }
    }
}
