package com.community.soap.user.domain.repository;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface TokenRepository {

    // Refresh Token 저장/조회/삭제 (jti 기준)
    void saveRefreshToken(String jti, Long userId, String refreshTokenHash, long ttlMillis);
    Optional<String> getRefreshTokenHashByJti(String jti);
    void deleteRefreshTokenByJti(String jti);

    // 유저-세션 인덱스
    void addUserRefreshIndex(Long userId, String jti);
    Set<String> getUserRefreshJtis(Long userId);
    void removeUserRefreshIndex(Long userId, String jti);
    void deleteAllRefreshTokensOfUser(Long userId); // 전체 로그아웃

    // 블랙리스트
    void blacklistAccessJti(String jti, long ttlMillis);
    void blacklistRefreshJti(String jti, long ttlMillis); // 선택
    boolean isAccessJtiBlacklisted(String jti);
    boolean isRefreshJtiBlacklisted(String jti);

    Optional<Long> getRemainingRefreshTtlMs(String rJti);

    /** 유저의 인덱스 SET에 해당 rJti가 포함되는지 빠르게 확인 */
    boolean hasUserRefreshJti(Long userId, String jti);

    // TokenRepository (선택)
    Set<String> popAllUserRefreshJtis(Long userId); // 인덱스에서 rJti 모두 꺼내면서 비움
    Map<String, Long> mgetRemainingRefreshTtlsMs(Set<String> rJtis); // 여러 jti TTL 일괄 조회
    void mdeleteRefreshTokensByJtis(Set<String> rJtis); // 여러 RT 해시 일괄 삭제
    void mblacklistRefreshJtis(Map<String, Long> jtiToTtlMs); // 여러 rJti 블랙리스트
}
