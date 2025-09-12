package com.community.soap.user.application.policy;

import java.time.Duration;

public interface EmailVerificationPolicy {

    Duration codeTtl();

    Duration cooltime();

    Duration blockTtl();

    long maxAttempts();

    Duration verifiedTtl();
}