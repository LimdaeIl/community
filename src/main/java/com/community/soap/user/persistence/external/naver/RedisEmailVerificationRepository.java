package com.community.soap.user.persistence.external.naver;

import com.community.soap.user.domain.repository.EmailVerificationRepository;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisEmailVerificationRepository implements EmailVerificationRepository {

    private final StringRedisTemplate redis;

    private String codeKey(String email) {
        return "EV:code:" + email;
    }

    private String attemptsKey(String email) {
        return "EV:attempts:" + email;
    }

    private String coolKey(String email) {
        return "EV:cool:" + email;
    }

    private String blockKey(String email) {
        return "EV:block:" + email;
    }

    private String verifiedKey(String email) {
        return "EV:verified:" + email;
    }

    @Override
    public void saveCodeHash(String email, String codeHash, Duration ttl) {
        redis.opsForValue().set(codeKey(email), codeHash, ttl);
    }

    @Override
    public Optional<String> getCodeHash(String email) {
        return Optional.ofNullable(redis.opsForValue().get(codeKey(email)));
    }

    @Override
    public void deleteCode(String email) {
        redis.delete(codeKey(email));
    }

    @Override
    public long incrementAttempts(String email, Duration windowTtl) {
        Long v = redis.opsForValue().increment(attemptsKey(email));
        // 첫 증가 시 TTL 부여
        if (v != null && v == 1L) {
            redis.expire(attemptsKey(email), windowTtl);
        }
        return v == null ? 0L : v;
    }

    @Override
    public void resetAttempts(String email) {
        redis.delete(attemptsKey(email));
    }

    @Override
    public void setCooltime(String email, Duration coolTtl) {
        redis.opsForValue().set(coolKey(email), "1", coolTtl);
    }

    @Override
    public boolean inCooltime(String email) {
        return Boolean.TRUE.equals(redis.hasKey(coolKey(email)));
    }

    @Override
    public void block(String email, Duration ttl) {
        redis.opsForValue().set(blockKey(email), "1", ttl);
    }

    @Override
    public boolean isBlocked(String email) {
        return Boolean.TRUE.equals(redis.hasKey(blockKey(email)));
    }

    @Override
    public void markVerified(String email, Duration ttl) {
        redis.opsForValue().set(verifiedKey(email), "1", ttl);
    }

    @Override
    public boolean isVerified(String email) {
        return Boolean.TRUE.equals(redis.hasKey(verifiedKey(email)));
    }

    @Override
    public void clearVerified(String email) {
        redis.delete(verifiedKey(email));
    }
}