package com.example.payment.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

@Slf4j
@Value(staticConstructor = "of")
public class RedisValueCache<T> {

    @Getter(AccessLevel.NONE)
    RedisTemplate<String, T> redisTemplate;
    String cacheNamePrefix;
    String cacheName;
    TriConsumer<ValueOperations<String, T>, String, T> setOperation;
    Duration cacheValueExpirationAfterAccess;

    public static <T> RedisValueCache<T> of(RedisTemplate<String, T> redisTemplate,
                                            String cacheNamePrefix,
                                            String cacheName,
                                            RedisCacheConfig cacheConfig) {
        return Optional.ofNullable(cacheConfig.getTimeToLive())
            .filter(Predicate.not(Duration.ZERO::equals))
            .map(ttl -> (TriConsumer<ValueOperations<String, T>, String, T>) (ops, k, v) -> ops.set(k, v, ttl))
            .or(() -> Optional.of(ValueOperations::set))
            .map(setOp ->
                of(redisTemplate, cacheNamePrefix, cacheName, setOp, cacheConfig.expireAfterAccess))
            .orElseThrow(() -> new IllegalStateException("Invalid redis cache config"));
    }

    public void set(String key, T value) {
            setOperation.accept(redisTemplate.opsForValue(), redisCacheKey(key), value);
    }

    public void set(long key, T value) {
        set(String.valueOf(key), value);
    }

    public T get(String key) {
            return redisTemplate.opsForValue().get(redisCacheKey(key));
    }

    public T get(long key) {
        return get(String.valueOf(key));
    }

    public T getAndExpire(String key) {
            return redisTemplate.opsForValue().getAndExpire(redisCacheKey(key),
                cacheValueExpirationAfterAccess);
    }

    public T getAndExpire(long key) {
        return getAndExpire(Long.toString(key));
    }

    public T getAndSet(String key, T value) {
        return  redisTemplate.opsForValue().getAndSet(redisCacheKey(key), value);
    }

    public T getAndSet(long key, T value) {
        return getAndSet(String.valueOf(key), value);
    }

    public boolean delete(String key) {
        return redisTemplate.delete(redisCacheKey(key));
    }

    public boolean delete(long key) {
        return delete(String.valueOf(key));
    }

    @Nullable
    public Long getSetSize(String key) {
        return Optional.ofNullable(redisTemplate.opsForSet().size(redisCacheKey(key))).orElse(0L);
    }

    public Set<T> popSetItems(String key, long count) {
        return Optional.ofNullable(redisTemplate.opsForSet().pop(redisCacheKey(key),
                count)).map(HashSet::new).orElseGet(HashSet::new);
    }

    @SafeVarargs
    @Nullable
    public final Long addToSet(String key, T... item) {
        return redisTemplate.opsForSet().add(redisCacheKey(key), item);
    }

    @Nullable
    public Long removeFromSet(String key, Object... items) {
        return redisTemplate.opsForSet().remove(redisCacheKey(key), items);
    }

    private String redisCacheKey(String cacheKey) {
        return String.join(":", cacheNamePrefix, cacheName, cacheKey);
    }

    @FunctionalInterface
    private interface TriConsumer<T, U, V> {
        void accept(T t, U u, V v);
    }
}