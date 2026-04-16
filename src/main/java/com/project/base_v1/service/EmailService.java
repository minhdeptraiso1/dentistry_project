package com.project.base_v1.service;

import java.util.Map;

public interface EmailService {
    void send(String to, String subject, String html);

    void sendTemplate(String to, String subject, String template, Map<String, Object> model);
}