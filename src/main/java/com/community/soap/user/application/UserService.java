package com.community.soap.user.application;

import com.community.soap.common.jwt.JwtErrorCode;
import com.community.soap.common.jwt.JwtProvider;
import com.community.soap.common.jwt.TokenException;
import com.community.soap.common.snowflake.Snowflake;
import com.community.soap.common.util.TokenHash;
import com.community.soap.user.application.exception.UserErrorCode;
import com.community.soap.user.application.exception.UserException;
import com.community.soap.user.application.request.SignInRequest;
import com.community.soap.user.application.request.SignUpRequest;
import com.community.soap.user.application.response.MyPageResponse;
import com.community.soap.user.application.response.SignInResponse;
import com.community.soap.user.application.response.SignUpResponse;
import com.community.soap.user.domain.entity.User;
import com.community.soap.user.domain.repository.TokenRepository;
import com.community.soap.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService implements UserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Snowflake snowflake;

    private final JwtProvider jwtProvider;
    private final TokenRepository tokenRepository;

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorCode.EMAIL_NOT_FOUND));
    }

    private User findUserById(Long userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    }

    private void checkEmailDuplication(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new UserException(UserErrorCode.EMAIL_DUPLICATED);
        }
    }

    private void checkPassword(String rowPassword, String encryptedPassword) {
        if (rowPassword == null || rowPassword.isEmpty()) {
            throw new UserException(UserErrorCode.PASSWORD_NULL);
        }

        if (!passwordEncoder.matches(rowPassword, encryptedPassword)) {
            throw new UserException(UserErrorCode.PASSWORD_INCORRECT);
        }
    }

    @Transactional
    @Override
    public SignUpResponse signup(SignUpRequest request) {
        checkEmailDuplication(request.email());

        User register = User.register(
                snowflake.nextId(),
                request.email(),
                passwordEncoder.encode(request.password()),
                request.nickname()
        );

        userRepository.save(register);

        return SignUpResponse.from(register);
    }

    @Transactional
    @Override
    public SignInResponse signIn(SignInRequest request) {
        User user = findUserByEmail(request.email());
        checkPassword(request.password(), user.getPassword());

        // 1) 토큰 발급
        String accessToken = jwtProvider.generateAccessToken(user.getUserId(), user.getUserRole());
        String refreshToken = jwtProvider.generateRefreshToken(user.getUserId());

        // 2) jti/TTL 산출
        String rJti = jwtProvider.getRefreshJti(refreshToken);
        long accessTtlMs = jwtProvider.accessTokenTtlOf(accessToken).toMillis();
        long refreshTtlMs = jwtProvider.refreshTokenTtlOf(refreshToken).toMillis();

        // 3) 리프레시 토큰 해시 저장 (+ 유저-세션 인덱스)
        String refreshHash = com.community.soap.common.util.TokenHash.sha256(refreshToken);
        tokenRepository.saveRefreshToken(rJti, user.getUserId(), refreshHash, refreshTtlMs);

        // 4) 응답 구성
        return SignInResponse.of(user, accessToken, accessTtlMs, refreshToken, refreshTtlMs);
    }

    /**
     * 로그아웃:
     * 1) Access jti 블랙리스트(즉시 효과)
     * 2) Refresh jti 블랙리스트 + 저장소에서 제거 + 유저 인덱스 제거
     */
    @Transactional
    @Override
    public void logout(String authorizationHeader, String refreshToken, Long userId) {
        // 1) Access 무효화
        if (authorizationHeader != null && !authorizationHeader.isBlank()) {
            String aJti = jwtProvider.getJti(authorizationHeader);
            long aTtlMs = jwtProvider.accessTokenTtlOf(authorizationHeader).toMillis();
            if (aTtlMs > 0) {
                tokenRepository.blacklistAccessJti(aJti, aTtlMs);
            }
        }

        // 2) Refresh 폐기
        if (refreshToken == null || refreshToken.isBlank()) {
            // refresh 없이 오는 로그아웃도 허용할지 정책적으로 결정. 여기선 필요로 가정.
            throw new TokenException(JwtErrorCode.INVALID_BEARER_TOKEN);
        }

        String rJti = jwtProvider.getRefreshJti(refreshToken);

        // 저장된 해시와 일치해야만 본인 토큰
        String inputHash = TokenHash.sha256(refreshToken);
        String storedHash = tokenRepository.getRefreshTokenHashByJti(rJti)
                .orElseThrow(() -> new TokenException(JwtErrorCode.INVALID_BEARER_TOKEN));

        if (!storedHash.equals(inputHash)) {
            throw new TokenException(JwtErrorCode.TAMPERED_TOKEN);
        }

        long rTtlMs = jwtProvider.refreshTokenTtlOf(refreshToken).toMillis();
        if (rTtlMs > 0) {
            tokenRepository.blacklistRefreshJti(rJti, rTtlMs);
        }

        // 저장소에서 제거 + 유저 인덱스에서 제거
        tokenRepository.deleteRefreshTokenByJti(rJti);
        tokenRepository.removeUserRefreshIndex(userId, rJti);
    }

    @Transactional
    @Override
    public SignInResponse refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new TokenException(JwtErrorCode.INVALID_BEARER_TOKEN);
        }

        // 1) 유효성/블랙리스트/해시 비교
        String rJti = jwtProvider.getRefreshJti(refreshToken);

        if (tokenRepository.isRefreshJtiBlacklisted(rJti)) {
            throw new TokenException(JwtErrorCode.INVALID_BEARER_TOKEN);
        }

        String inputHash = TokenHash.sha256(refreshToken);
        String storedHash = tokenRepository.getRefreshTokenHashByJti(rJti)
                .orElseThrow(() -> new TokenException(JwtErrorCode.INVALID_BEARER_TOKEN));
        if (!storedHash.equals(inputHash)) {
            throw new TokenException(JwtErrorCode.TAMPERED_TOKEN);
        }

        Long userId = jwtProvider.getUserIdFromRefresh(refreshToken);
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new TokenException(JwtErrorCode.INVALID_BEARER_TOKEN));

        // 2) 이전 rJti 폐기(블랙리스트 + 삭제 + 인덱스 제거)
        long oldRttl = jwtProvider.refreshTokenTtlOf(refreshToken).toMillis();
        if (oldRttl > 0) {
            tokenRepository.blacklistRefreshJti(rJti, oldRttl);
        }
        tokenRepository.deleteRefreshTokenByJti(rJti);
        tokenRepository.removeUserRefreshIndex(userId, rJti);

        // 3) 새 토큰 발급 및 저장
        String newAccess  = jwtProvider.generateAccessToken(user.getUserId(), user.getUserRole());
        String newRefresh = jwtProvider.generateRefreshToken(user.getUserId());

        String newRJti     = jwtProvider.getRefreshJti(newRefresh);
        long   accessTtlMs = jwtProvider.accessTokenTtlOf(newAccess).toMillis();
        long   refreshTtlMs= jwtProvider.refreshTokenTtlOf(newRefresh).toMillis();

        String newRHash = TokenHash.sha256(newRefresh);
        tokenRepository.saveRefreshToken(newRJti, userId, newRHash, refreshTtlMs);

        // 4) 응답
        return SignInResponse.of(user, newAccess, accessTtlMs, newRefresh, refreshTtlMs);
    }

    @Transactional(readOnly = true)
    @Override
    public MyPageResponse me(Long userId) {
        User byUserId = findUserById(userId);

        return MyPageResponse.from(byUserId);
    }

    @Transactional
    @Override
    public void deleteUser(Long userId) {
        User byUserId = findUserById(userId);
        byUserId.delete(userId);
    }
}
