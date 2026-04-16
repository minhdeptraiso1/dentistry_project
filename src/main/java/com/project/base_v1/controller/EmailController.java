package com.project.base_v1.controller;

import com.project.base_v1.dto.request.email.SendEmailRequest;
import com.project.base_v1.dto.request.email.SendTemplateEmailRequest;
import com.project.base_v1.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/emails")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send")
    public Map<String, Object> send(@RequestBody @Valid SendEmailRequest request) {
        emailService.send(
                request.to(),
                request.subject(),
                request.html()
        );

        return Map.of(
                "success", true,
                "message", "Đã tiếp nhận yêu cầu gửi email"
        );
    }

    @PostMapping("/send-template")
    public Map<String, Object> sendTemplate(@RequestBody @Valid SendTemplateEmailRequest request) {
        emailService.sendTemplate(
                request.to(),
                request.subject(),
                request.template(),
                request.model()
        );

        return Map.of(
                "success", true,
                "message", "Đã tiếp nhận yêu cầu gửi email template"
        );
    }
}