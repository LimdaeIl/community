package com.community.soap.user.persistence.external.email;

import jakarta.mail.internet.InternetAddress;
import java.time.Duration;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ThymeleafEmailSender {

    private final JavaMailSender mailSender;
    private final MailTemplateRenderer renderer;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public void sendVerificationCode(String to, String code, Duration ttl, String brand) {
        long minutes = Math.max(1, ttl.toMinutes());
        String html = renderer.render("verification-code", Map.of(
                "brand", brand,
                "code", code,
                "minutes", minutes
        ));
        String subject = "[" + brand + "] 이메일 인증코드";

        mailSender.send(mime -> {
            MimeMessageHelper h = new MimeMessageHelper(mime, true, "UTF-8");
            h.setTo(to);
            h.setSubject(subject);
            h.setFrom(new InternetAddress(fromAddress, brand, "UTF-8")); // ✅ 헤더 From
            // h.setReplyTo(fromAddress); // 선택
            h.setText(html, true);
        });
    }
}
