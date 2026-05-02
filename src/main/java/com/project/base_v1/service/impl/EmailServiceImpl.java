package com.project.base_v1.service.impl;

import com.project.base_v1.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String from;

    @Override
    @Async
    public void send(String to, String subject, String html) {
        try {
            var mime = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mime, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(mime);

            log.info("Gửi email thành công tới {}", to);
        } catch (Exception e) {
            log.error("Gửi email thất bại tới {}", to, e);
        }
    }

    @Override
    @Async
    public void sendTemplate(String to, String subject, String template, Map<String, Object> model) {
        try {
            String html = templateEngine.process(template, new Context(Locale.forLanguageTag("vi"), model));
            send(to, subject, html);
        } catch (Exception e) {
            log.error("Render template email thất bại tới {}", to, e);
        }
    }
    @Override
    @Async
    public void sendForgotPasswordOtp(String to, String name, String identifier, String otp, int expiredMinutes) {
        try {
            Map<String, Object> model = new HashMap<>();
            model.put("name", name);
            model.put("identifier", identifier);
            model.put("otp", otp);
            model.put("expiredMinutes", expiredMinutes);

            String html = templateEngine.process(
                    "forgot-password-otp",
                    new Context(Locale.forLanguageTag("vi"), model)
            );

            send(to, "Mã OTP đặt lại mật khẩu", html);
        } catch (Exception e) {
            log.error("Gửi email OTP thất bại tới {}", to, e);
        }
    }
}