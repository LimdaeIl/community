package com.community.soap.user.application.port.out;


import java.time.Duration;

public interface EmailSenderPort {
    void sendVerificationCode(String email, String code, Duration ttl, String brand);
}
