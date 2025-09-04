package com.community.soap.user.domain.repository;

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
}
