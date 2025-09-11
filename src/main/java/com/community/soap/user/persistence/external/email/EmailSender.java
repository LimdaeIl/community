package com.community.soap.user.persistence.external.email;


import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailSender {

    private final JavaMailSender mailSender;

    public void sendVerificationCode(String to, String code) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("[Community SOAP] 이메일 인증코드");
        msg.setText("""
                아래 인증코드를 5분 이내에 입력해 주세요.
                
                인증코드: %s
                유효시간: 5분
                """.formatted(code));
        mailSender.send(msg);
    }
}