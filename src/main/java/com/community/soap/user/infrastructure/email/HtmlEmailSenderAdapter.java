package com.community.soap.user.infrastructure.email;

import jakarta.mail.internet.MimeMessage;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HtmlEmailSenderAdapter {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public void sendVerificationCode(String to, String code, Duration ttl, @Nullable String brand) {
        String subject = "[" + (brand == null ? "Community SOAP" : brand) + "] 이메일 인증코드";
        long minutes = Math.max(1, ttl.toMinutes());

        String html = buildHtml(code, minutes, brand);
        String text = buildText(code, minutes, brand);

        mailSender.send((MimeMessage mime) -> {
            MimeMessageHelper h = new MimeMessageHelper(mime, true, "UTF-8");
            h.setTo(to);
            h.setSubject(subject);

            h.setFrom(new jakarta.mail.internet.InternetAddress(
                    fromAddress,
                    brand == null ? "Community SOAP" : brand, "UTF-8"
            ));

            h.setText(text, html); // plain, html

            // 로고 추가하고 싶으면 /static/logo.png 넣고 주석 해제
            // h.addInline("brandLogo", new ClassPathResource("static/logo.png"));
        });
    }

    private String buildHtml(String code, long minutes, String brand) {
        String title = brand == null ? "Community SOAP" : brand;
        return """
                <!doctype html>
                <html lang="ko">
                <head>
                  <meta charset="UTF-8" />
                  <meta name="viewport" content="width=device-width, initial-scale=1" />
                  <title>%1$s 인증코드</title>
                  <style>
                    /* 인라인 CSS: 다크모드/모바일 고려한 최소 스타일 */
                    body{margin:0;background:#f6f8fb;font-family:system-ui,-apple-system,Segoe UI,Roboto,Helvetica,Arial,sans-serif;color:#111827;}
                    .wrap{max-width:560px;margin:32px auto;padding:0 16px;}
                    .card{background:#fff;border-radius:16px;box-shadow:0 8px 24px rgba(2,6,23,.06);overflow:hidden;}
                    .header{padding:20px 24px;border-bottom:1px solid #eef2f7;display:flex;align-items:center;gap:12px;}
                    .brand{font-size:18px;font-weight:700;}
                    .content{padding:28px 24px;}
                    .title{font-size:20px;font-weight:700;margin:0 0 12px;}
                    .desc{margin:0 0 16px;line-height:1.6;color:#374151;}
                    .code{letter-spacing:.2em;font-weight:800;font-size:28px;background:#111827;color:#fff;
                          text-align:center;padding:14px 0;border-radius:12px;margin:16px 0;}
                    .muted{color:#6b7280;font-size:12px;margin-top:12px}
                    .footer{padding:16px 24px;color:#6b7280;font-size:12px;text-align:center;border-top:1px solid #eef2f7;}
                    @media (prefers-color-scheme: dark){
                      body{background:#0b1220;color:#e5e7eb;}
                      .card{background:#0f172a;border:1px solid #1f2937;}
                      .desc,.muted,.footer{color:#94a3b8;}
                      .header{border-color:#1f2937;}
                      .code{background:#e5e7eb;color:#111827;}
                    }
                  </style>
                </head>
                <body>
                  <div class="wrap">
                    <div class="card">
                      <div class="header">
                        <!-- 로고 사용 시: <img src="cid:brandLogo" alt="logo" height="24"/> -->
                        <div class="brand">%1$s</div>
                      </div>
                      <div class="content">
                        <h1 class="title">이메일 인증코드</h1>
                        <p class="desc">아래 숫자 코드를 <b>%2$d분</b> 이내에 입력해 주세요.</p>
                        <div class="code">%3$s</div>
                        <p class="muted">코드는 일회성이며, 타인과 공유하지 마세요.</p>
                      </div>
                      <div class="footer">
                        본 메일은 발신 전용입니다. 문의가 필요하면 앱/웹의 고객센터를 이용해주세요.
                      </div>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(title, minutes, code);
    }

    private String buildText(String code, long minutes, String brand) {
        String title = brand == null ? "Community SOAP" : brand;
        return """
                [%s] 이메일 인증코드
                
                아래 숫자 코드를 %d분 이내에 입력해 주세요.
                
                인증코드: %s
                
                안전을 위해 타인과 공유하지 마세요.
                """.formatted(title, minutes, code);
    }
}
