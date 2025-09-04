package com.community.soap.user.persistence;

import com.community.soap.user.domain.repository.TokenRepository;
import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;


@RequiredArgsConstructor
@Repository
public class JwtRepository implements TokenRepository {

    private final StringRedisTemplate redis;

    private static final String PREFIX = "user-service";

    private static String kRt(String jti) {
        return PREFIX + ":RT:" + jti;
    }

    private static String kUserRt(Long userId) {
        return PREFIX + ":USER:" + userId + ":RT";
    }

    private static String kBlA(String jti) {
        return PREFIX + ":BL:A:" + jti;
    }

    private static String kBlR(String jti) {
        return PREFIX + ":BL:R:" + jti;
    }

    @Override
    public void saveRefreshToken(String jti, Long userId, String refreshTokenHash, long ttlMillis) {
        // 토큰 본문(해시) 저장
        redis.opsForValue().set(kRt(jti), refreshTokenHash, Duration.ofMillis(ttlMillis));
        // 유저-세션 인덱스(SET)에 jti 추가
        redis.opsForSet().add(kUserRt(userId), jti);
        // 인덱스 키는 굳이 TTL 줄 필요는 없음(원하면 정책적으로 부여)
    }

    @Override
    public Optional<String> getRefreshTokenHashByJti(String jti) {
        return Optional.ofNullable(redis.opsForValue().get(kRt(jti)));
    }

    @Override
    public void deleteRefreshTokenByJti(String jti) {
        redis.delete(kRt(jti));
        // user set 에서의 제거는 호출측에서 userId 를 알고 있을 때 removeUserRefreshIndex 로 함께 처리 권장
    }

    @Override
    public void addUserRefreshIndex(Long userId, String jti) {
        redis.opsForSet().add(kUserRt(userId), jti);
    }

    @Override
    public Set<String> getUserRefreshJtis(Long userId) {
        Set<String> members = redis.opsForSet().members(kUserRt(userId));
        return members == null ? Collections.emptySet() : members;
    }

    @Override
    public void removeUserRefreshIndex(Long userId, String jti) {
        redis.opsForSet().remove(kUserRt(userId), jti);
    }

    @Override
    public void deleteAllRefreshTokensOfUser(Long userId) {
        Set<String> jtis = getUserRefreshJtis(userId);
        if (jtis != null && !jtis.isEmpty()) {
            // RT 본문 삭제
            redis.delete(jtis.stream().map(JwtRepository::kRt).toList());
            // 인덱스 비움
            redis.delete(kUserRt(userId));
        }
    }

    @Override
    public void blacklistAccessJti(String jti, long ttlMillis) {
        // 덮어쓰기 대신 setIfAbsent를 쓰면 TTL 갱신으로 인한 “의도치 않은 연장”을 방지
        redis.opsForValue().setIfAbsent(kBlA(jti), "1", Duration.ofMillis(ttlMillis));
    }

    @Override
    public void blacklistRefreshJti(String jti, long ttlMillis) {
        redis.opsForValue().setIfAbsent(kBlR(jti), "1", Duration.ofMillis(ttlMillis));
    }

    @Override
    public boolean isAccessJtiBlacklisted(String jti) {
        return Boolean.TRUE.equals(redis.hasKey(kBlA(jti)));
    }

    @Override
    public boolean isRefreshJtiBlacklisted(String jti) {
        return Boolean.TRUE.equals(redis.hasKey(kBlR(jti)));
    }
}
