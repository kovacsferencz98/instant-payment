package com.example.payment.it;

import com.example.payment.InstantPaymentApiApplication;
import com.example.payment.config.KafkaContainerConfig;
import com.example.payment.config.RedisContainerConfig;
import com.example.payment.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.payment.config.PostgreSQLContainerConfig;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

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
public class ValidationItTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnBadRequest_whenTransferAmountIsBelowZero() throws Exception {
        var response =
        mockMvc.perform(post("/api/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(String.format("""
            {
              "sender_id": 123,
              "recipient_id": 456,
              "amount": "-1"
            }
            """, UUID.randomUUID())));

        // then
        response.andExpect(status().isBadRequest());
        var errorResponse = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(),
            ErrorResponse.class);
        assertThat(errorResponse.code()).isEqualTo(4001);
        assertThat(errorResponse.message())
            .isEqualTo("Amount must be greater than zero.");
    }
    
    @Test
    void shouldReturnBadRequest_whenTransferAmountIsNull() throws Exception {
        var response =
                mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sender_id": 123,
                                  "recipient_id": 456,
                                  "amount": null
                                }
                                """));

        // then
        response.andExpect(status().isBadRequest());
        var errorResponse = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(),
                ErrorResponse.class);
        assertThat(errorResponse.code()).isEqualTo(4001);
        assertThat(errorResponse.message())
                .isEqualTo("Amount cannot be null.");
    }

    @Test
    void shouldReturnBadRequest_whenSenderIdIsNull() throws Exception {
        var response =
                mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sender_id": null,
                                  "recipient_id": 456,
                                  "amount": "100"
                                }
                                """));

        // then
        response.andExpect(status().isBadRequest());
        var errorResponse = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(),
                ErrorResponse.class);
        assertThat(errorResponse.code()).isEqualTo(4001);
        assertThat(errorResponse.message())
                .isEqualTo("Sender ID cannot be null.");
    }

    @Test
    void shouldReturnBadRequest_whenRecipientIdIsNull() throws Exception {
        var response =
                mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sender_id": 123,
                                  "recipient_id": null,
                                  "amount": "100"
                                }
                                """));

        // then
        response.andExpect(status().isBadRequest());
        var errorResponse = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(),
                ErrorResponse.class);
        assertThat(errorResponse.code()).isEqualTo(4001);
        assertThat(errorResponse.message())
                .isEqualTo("Recipient ID cannot be null.");
    }

    @Test
    void shouldReturnBadRequest_whenTransferAmountIsZero() throws Exception {
        var response =
                mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sender_id": 123,
                                  "recipient_id": 456,
                                  "amount": "0"
                                }
                                """));

        // then
        response.andExpect(status().isBadRequest());
        var errorResponse = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(),
                ErrorResponse.class);
        assertThat(errorResponse.code()).isEqualTo(4001);
        assertThat(errorResponse.message())
                .isEqualTo("Amount must be greater than zero.");
    }
    
    
}
