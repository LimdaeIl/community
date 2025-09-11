package com.community.soap.user.persistence;

import com.community.soap.user.domain.repository.TokenRepository;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisStringCommands.SetOption;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
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
        // 토큰 본문(해시) 저장 -> RT 해시 저장 + TTL 설정
        redis.opsForValue().set(kRt(jti), refreshTokenHash, Duration.ofMillis(ttlMillis));
        // 유저-세션 인덱스(SET)에 jti 추가
        redis.opsForSet().add(kUserRt(userId), jti);
        // 인덱스 키는 굳이 TTL 줄 필요는 없음(원하면 정책적으로 부여. 세션 수명과 무관, 정합성 측면에서 권장 X)
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
            // RT 본문 일괄 삭제
            redis.delete(jtis.stream().map(JwtRepository::kRt).toList());
            // 유저 인덱스 삭제
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

    @Override
    public Optional<Long> getRemainingRefreshTtlMs(String rJti) {
        // TTL(ms) 조회. Redis는 -2(키 없음), -1(TTL 없음) 반환 가능
        Long ttlMs = redis.getExpire(kRt(rJti), TimeUnit.MILLISECONDS);
        if (ttlMs == null || ttlMs <= 0) {
            // 키가 없거나, TTL이 없거나(영속), 이미 만료 직전 등 → 블랙리스트 생략
            return Optional.empty();
        }
        return Optional.of(ttlMs);
    }

    @Override
    public boolean hasUserRefreshJti(Long userId, String jti) {
        // Redis SISMEMBER 사용
        return Boolean.TRUE.equals(redis.opsForSet().isMember(kUserRt(userId), jti));
    }

    /**
     * 유저 인덱스(SET)에서 모든 rJti를 원자적으로 가져오고 키를 비운다. Lua: SMEMBERS key; DEL key; return members;
     */
    @Override
    public Set<String> popAllUserRefreshJtis(Long userId) {
        String idxKey = kUserRt(userId);
        byte[] key = redis.getStringSerializer().serialize(idxKey);

        // Lua 스크립트 (원자적 실행)
        String script = "local k=KEYS[1]; local m=redis.call('SMEMBERS',k); redis.call('DEL',k); return m;";
        @SuppressWarnings("unchecked")
        List<byte[]> result = (List<byte[]>) redis.execute((RedisCallback<Object>) connection ->
                connection.scriptingCommands().eval(
                        script.getBytes(StandardCharsets.UTF_8),
                        ReturnType.MULTI, 1, key
                )
        );

        if (result == null || result.isEmpty()) {
            return Collections.emptySet();
        }

        return result.stream()
                .map(b -> redis.getStringSerializer().deserialize(b))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * 여러 rJti의 남은 TTL(ms)을 파이프라인으로 일괄 조회(PTTL). TTL <= 0(-2, -1 포함)은 제외.
     */
    @Override
    public Map<String, Long> mgetRemainingRefreshTtlsMs(Set<String> rJtis) {
        if (rJtis == null || rJtis.isEmpty()) {
            return Collections.emptyMap();
        }

        final var ser = redis.getStringSerializer();
        final List<String> order = new ArrayList<>(rJtis);

        @SuppressWarnings("unchecked")
        List<Object> raw = redis.executePipelined((RedisCallback<Object>) connection -> {
            for (String rJti : order) {
                byte[] key = ser.serialize(kRt(rJti));
                connection.keyCommands().pTtl(key);
            }
            return null;
        });

        Map<String, Long> out = new LinkedHashMap<>(order.size());
        for (int i = 0; i < order.size(); i++) {
            Object val = raw.get(i);
            Long ttl = (val instanceof Long) ? (Long) val : null;
            if (ttl != null && ttl > 0) {
                out.put(order.get(i), ttl);
            }
        }
        return out;
    }

    /**
     * 여러 RT 해시 키를 파이프라인으로 일괄 삭제.
     */
    @Override
    public void mdeleteRefreshTokensByJtis(Set<String> rJtis) {
        if (rJtis == null || rJtis.isEmpty()) {
            return;
        }

        final var ser = redis.getStringSerializer();
        redis.executePipelined((RedisCallback<Object>) connection -> {
            for (String rJti : rJtis) {
                connection.keyCommands().del(ser.serialize(kRt(rJti)));
            }
            return null;
        });
    }

    /**
     * 여러 rJti를 블랙리스트에 일괄 등록(SET NX PX ttl) – 파이프라인 적용. 이미 존재하는 키는 TTL 연장하지 않음.
     */
    @Override
    public void mblacklistRefreshJtis(Map<String, Long> jtiToTtlMs) {
        if (jtiToTtlMs == null || jtiToTtlMs.isEmpty()) {
            return;
        }

        final var ser = redis.getStringSerializer();
        byte[] value = ser.serialize("1");

        redis.executePipelined((RedisCallback<Object>) connection -> {
            for (Map.Entry<String, Long> e : jtiToTtlMs.entrySet()) {
                String jti = e.getKey();
                long ttl = e.getValue() == null ? 0 : e.getValue();
                if (ttl <= 0) {
                    continue;
                }

                byte[] key = ser.serialize(kBlR(jti));
                connection.stringCommands().set(
                        key,
                        value,
                        Expiration.milliseconds(ttl),
                        SetOption.ifAbsent()
                );
            }
            return null;
        });
    }
}
