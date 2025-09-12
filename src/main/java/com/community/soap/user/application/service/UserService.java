package com.community.soap.user.application.service;

import com.community.soap.common.jwt.JwtErrorCode;
import com.community.soap.common.jwt.JwtProvider;
import com.community.soap.common.jwt.TokenException;
import com.community.soap.common.snowflake.Snowflake;
import com.community.soap.common.util.TokenHash;
import com.community.soap.user.application.policy.EmailVerificationPolicy;
import com.community.soap.user.application.port.in.UserUseCase;
import com.community.soap.user.application.port.out.EmailSenderPort;
import com.community.soap.user.application.port.out.EmailVerificationRepositoryPort;
import com.community.soap.user.application.port.out.TokenRepositoryPort;
import com.community.soap.user.application.port.out.UserRepositoryPort;
import com.community.soap.user.application.request.EmailVerificationCodeRequest;
import com.community.soap.user.application.request.EmailVerifyCodeRequest;
import com.community.soap.user.application.request.SignInRequest;
import com.community.soap.user.application.request.SignUpRequest;
import com.community.soap.user.application.response.EmailVerificationCodeResponse;
import com.community.soap.user.application.response.MyPageResponse;
import com.community.soap.user.application.response.SignInResponse;
import com.community.soap.user.application.response.SignUpResponse;
import com.community.soap.user.domain.entity.User;
import com.community.soap.user.domain.exception.UserErrorCode;
import com.community.soap.user.domain.exception.UserException;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService implements UserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final Snowflake snowflake;

    private final JwtProvider jwtProvider;
    private final TokenRepositoryPort tokenRepositoryPort;

    private final EmailVerificationRepositoryPort emailVerificationRepositoryPort;
    private final EmailVerificationPolicy emailPolicy;
    private final EmailSenderPort emailSenderPort;

    private User findUserByEmail(String email) {
        return userRepositoryPort.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorCode.EMAIL_NOT_FOUND));
    }

    private User findUserById(Long userId) {
        return userRepositoryPort.findByUserId(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    }

    private void checkEmailDuplication(String email) {
        if (userRepositoryPort.existsByEmail(email)) {
            throw new UserException(UserErrorCode.EMAIL_DUPLICATED);
        }
    }

    private void checkPassword(String rawPassword, String encryptedPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new UserException(UserErrorCode.PASSWORD_NULL);
        }

        if (!passwordEncoder.matches(rawPassword, encryptedPassword)) {
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

        userRepositoryPort.save(register);

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
        String refreshHash = TokenHash.sha256(refreshToken);
        tokenRepositoryPort.saveRefreshToken(rJti, user.getUserId(), refreshHash, refreshTtlMs);

        // 4) 응답 구성
        return SignInResponse.of(user, accessToken, accessTtlMs, refreshToken, refreshTtlMs);
    }


    /**
     * 로그아웃: 단일 세션(rJti)만 정확히 폐기. - AT는 소유자 일치 시 블랙리스트 - RT는 rJti 단위로 검증/블랙리스트/삭제/인덱스 제거
     */
    @Transactional
    @Override
    public void logout(String authorizationHeader, String refreshToken, Long userIdFromCtx) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new TokenException(JwtErrorCode.INVALID_BEARER_TOKEN);
        }

        Long userIdFromRt = jwtProvider.getUserIdFromRefresh(refreshToken);
        if (!userIdFromRt.equals(userIdFromCtx)) {
            throw new TokenException(JwtErrorCode.INVALID_BEARER_TOKEN);
        }

        String rJti = jwtProvider.getRefreshJti(refreshToken);
        if (!tokenRepositoryPort.hasUserRefreshJti(userIdFromCtx, rJti)) {
            throw new TokenException(JwtErrorCode.INVALID_BEARER_TOKEN);
        }

        // AT 블랙리스트 (소유자 일치시에만)
        blacklistAccessIfOwner(authorizationHeader, userIdFromCtx);

        // RT 폐기 (단일 rJti) - 해시/TTL 1회 계산
        String refreshHash = TokenHash.sha256(refreshToken);
        long refreshTtlMs = jwtProvider.refreshTokenTtlOf(refreshToken).toMillis();
        revokeRefreshByJti(userIdFromCtx, rJti, refreshHash, refreshTtlMs);
    }

    @Transactional
    @Override
    public SignInResponse refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new TokenException(JwtErrorCode.INVALID_BEARER_TOKEN);
        }

        // 1) 유효성/블랙리스트/해시 비교
        String rJti = jwtProvider.getRefreshJti(refreshToken);

        if (tokenRepositoryPort.isRefreshJtiBlacklisted(rJti)) {
            throw new TokenException(JwtErrorCode.INVALID_BEARER_TOKEN);
        }

        String inputHash = TokenHash.sha256(refreshToken);
        String storedHash = tokenRepositoryPort.getRefreshTokenHashByJti(rJti)
                .orElseThrow(() -> new TokenException(JwtErrorCode.INVALID_BEARER_TOKEN));
        if (!storedHash.equals(inputHash)) {
            throw new TokenException(JwtErrorCode.TAMPERED_TOKEN);
        }

        Long userId = jwtProvider.getUserIdFromRefresh(refreshToken);
        User user = userRepositoryPort.findByUserId(userId)
                .orElseThrow(() -> new TokenException(JwtErrorCode.INVALID_BEARER_TOKEN));

        // 2) 이전 rJti 폐기(블랙리스트 + 삭제 + 인덱스 제거)
        long oldRttl = jwtProvider.refreshTokenTtlOf(refreshToken).toMillis();
        if (oldRttl > 0) {
            tokenRepositoryPort.blacklistRefreshJti(rJti, oldRttl);
        }
        tokenRepositoryPort.deleteRefreshTokenByJti(rJti);
        tokenRepositoryPort.removeUserRefreshIndex(userId, rJti);

        // 3) 새 토큰 발급 및 저장
        String newAccess = jwtProvider.generateAccessToken(user.getUserId(), user.getUserRole());
        String newRefresh = jwtProvider.generateRefreshToken(user.getUserId());

        String newRJti = jwtProvider.getRefreshJti(newRefresh);
        long accessTtlMs = jwtProvider.accessTokenTtlOf(newAccess).toMillis();
        long refreshTtlMs = jwtProvider.refreshTokenTtlOf(newRefresh).toMillis();

        String newRHash = TokenHash.sha256(newRefresh);
        tokenRepositoryPort.saveRefreshToken(newRJti, userId, newRHash, refreshTtlMs);

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
    public void deleteMe(String authorizationHeader, Long userIdFromCtx) {
        // 1) 사용자 존재 확인
        User user = findUserById(userIdFromCtx);

        // 2) AT 블랙리스트 + RT 전부 폐기
        blacklistAccessIfOwner(authorizationHeader, user.getUserId());
        revokeAllRefreshOfUser(user.getUserId());

        // 3) 유저 soft-delete
        user.softDelete(user.getUserId());
    }

    /**
     * 관리자 강제 탈퇴: 세션 정리 + soft-delete (컨트롤러/어드바이저에서 관리자 권한 체크)
     */
    @Transactional
    @Override
    public void deleteUserAsAdmin(Long targetUserId) {
        User target = findUserById(targetUserId);

        revokeAllRefreshOfUser(target.getUserId()); // AT 모를 수 있으니 RT만 전부 폐기
        target.softDelete(target.getUserId());
    }

    /**
     * AT가 주어졌고 소유자가 targetUserId와 일치하면 블랙리스트 등록
     */
    private void blacklistAccessIfOwner(String authorizationHeader, Long targetUserId) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return;
        }

        try {
            Long uidFromAt = jwtProvider.getUserId(authorizationHeader);
            if (!uidFromAt.equals(targetUserId)) {
                return;
            }

            String aJti = jwtProvider.getJti(authorizationHeader);
            long aTtlMs = jwtProvider.accessTokenTtlOf(authorizationHeader).toMillis();
            if (aTtlMs > 0) {
                tokenRepositoryPort.blacklistAccessJti(aJti, aTtlMs); // setIfAbsent로 TTL 연장 방지
            }
        } catch (TokenException ignore) {
            // AT 만료/형식 오류 등은 무시 (주 목적은 RT 폐기)
        }
    }

    /**
     * 단일 rJti에 대한 RT 폐기(검증·블랙리스트·삭제·인덱스제거)
     */
    private void revokeRefreshByJti(Long userId, String rJti, String inputRefreshHash,
            long refreshTtlMs) {
        // 1) 멱등/위변조 방지: 저장된 해시와 비교 (저장소에 없으면 이미 처리된 것으로 간주)
        String storedHash = tokenRepositoryPort.getRefreshTokenHashByJti(rJti).orElse(null);
        if (storedHash != null && !storedHash.equals(inputRefreshHash)) {
            throw new TokenException(JwtErrorCode.TAMPERED_TOKEN);
        }

        // 2) 블랙리스트 (TTL 있을 때만)
        if (refreshTtlMs > 0) {
            tokenRepositoryPort.blacklistRefreshJti(rJti, refreshTtlMs);
        }

        // 3) 저장소 삭제 + 인덱스 제거
        tokenRepositoryPort.deleteRefreshTokenByJti(rJti);
        tokenRepositoryPort.removeUserRefreshIndex(userId, rJti);
    }

    /**
     * 해당 유저의 모든 RT 세션을 일괄 폐기(블랙리스트 가능 시 포함) -> revokeAllRefreshOfUser를 배치 API로 치환
     */
    private void revokeAllRefreshOfUser(Long targetUserId) {
        // 1) rJti 전부 꺼내면서 인덱스 비우기 (원자)
        Set<String> rJtis = tokenRepositoryPort.popAllUserRefreshJtis(targetUserId);
        if (rJtis.isEmpty()) {
            return;
        }

        // 2) TTL 일괄 조회(파이프라인)
        Map<String, Long> jtiToTtl = tokenRepositoryPort.mgetRemainingRefreshTtlsMs(rJtis);
        // 3) 블랙리스트 일괄 등록(파이프라인) – TTL 있는 것만
        tokenRepositoryPort.mblacklistRefreshJtis(jtiToTtl);
        // 4) RT 해시 일괄 삭제(파이프라인)
        tokenRepositoryPort.mdeleteRefreshTokensByJtis(rJtis);
    }

    // ---- 이메일 인증 코드 요청 ----
    @Override
    public EmailVerificationCodeResponse emailVerificationCode(
            EmailVerificationCodeRequest request) {
        final String email = request.email();

        // 이미 가입된 이메일이면 굳이 인증코드 발송 X (정책에 따라 허용 가능)
        if (userRepositoryPort.existsByEmail(email)) {
            throw new UserException(UserErrorCode.EMAIL_DUPLICATED);
        }

        // 차단 상태 체크
        if (emailVerificationRepositoryPort.isBlocked(email)) {
            throw new UserException(UserErrorCode.EMAIL_VERIFICATION_BLOCKED);
        }

        // 쿨타임 체크
        if (emailVerificationRepositoryPort.inCooltime(email)) {
            throw new UserException(UserErrorCode.EMAIL_VERIFICATION_COOLTIME);
        }

        // 코드 생성 (6자리)
        String code = generate6DigitCode();

        // 코드 해시 저장
        String codeHash = TokenHash.sha256(code);
        emailVerificationRepositoryPort.saveCodeHash(email, codeHash, emailPolicy.codeTtl());

        // 쿨타임 세팅
        emailVerificationRepositoryPort.setCooltime(email, emailPolicy.cooltime());

        // 메일 발송
        emailSenderPort.sendVerificationCode(email, code, emailPolicy.codeTtl(),
                "Community SOAP");

        // 응답
        long expireMs = emailPolicy.codeTtl().toMillis();
        return EmailVerificationCodeResponse.of(email, expireMs);
    }

    // ---- 이메일 인증 코드 검증 ----
    @Override
    public void emailVerifyCode(EmailVerifyCodeRequest request) {
        final String email = request.email();

        if (emailVerificationRepositoryPort.isBlocked(email)) {
            throw new UserException(UserErrorCode.EMAIL_VERIFICATION_BLOCKED);
        }

        String storedHash = emailVerificationRepositoryPort.getCodeHash(email)
                .orElseThrow(
                        () -> new UserException(UserErrorCode.EMAIL_VERIFICATION_NOT_REQUESTED));

        String inputHash = TokenHash.sha256(String.valueOf(request.verifyCode()));

        if (!storedHash.equals(inputHash)) {
            long attempts = emailVerificationRepositoryPort.incrementAttempts(email,
                    emailPolicy.codeTtl());
            if (attempts >= emailPolicy.maxAttempts()) {
                emailVerificationRepositoryPort.block(email, emailPolicy.blockTtl());
                emailVerificationRepositoryPort.deleteCode(email); // 코드 폐기
            }
            throw new UserException(UserErrorCode.EMAIL_VERIFY_CODE_MISMATCH);
        }

        // 성공: 코드/시도 수/블록 정보 정리
        emailVerificationRepositoryPort.deleteCode(email);
        emailVerificationRepositoryPort.resetAttempts(email);
        emailVerificationRepositoryPort.clearVerified(email);     // 기존 플래그 제거 후
        emailVerificationRepositoryPort.markVerified(email, emailPolicy.verifiedTtl());
    }

    private String generate6DigitCode() {
        SecureRandom r = new SecureRandom();
        int n = 100000 + r.nextInt(900000);
        return String.valueOf(n);
    }
}
