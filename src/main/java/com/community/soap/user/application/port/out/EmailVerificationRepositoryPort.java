package com.community.soap.user.application.port.out;

import java.time.Duration;
import java.util.Optional;

public interface EmailVerificationRepositoryPort {
    void saveCodeHash(String email, String codeHash, Duration ttl);
    Optional<String> getCodeHash(String email);
    void deleteCode(String email);

    long incrementAttempts(String email, Duration windowTtl);
    void resetAttempts(String email);

    void setCooltime(String email, Duration coolTtl);
    boolean inCooltime(String email);

    void block(String email, Duration ttl);
    boolean isBlocked(String email);

    void markVerified(String email, Duration ttl);
    boolean isVerified(String email);
    void clearVerified(String email);
}
