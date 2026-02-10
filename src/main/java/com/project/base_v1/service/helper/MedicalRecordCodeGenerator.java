package com.project.base_v1.service.helper;

import com.project.base_v1.repository.MedicalRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MedicalRecordCodeGenerator {

    private static final String PREFIX = "HS";
    private static final int PAD_LENGTH = 6; // HS000001

    private final MedicalRecordRepository medicalRecordRepository;

    public String nextCode() {
        String last = medicalRecordRepository.findLatestRecordCode()
                .orElse(PREFIX + "0".repeat(PAD_LENGTH)); // HS000000

        int lastNumber = parseNumber(last);
        int nextNumber = lastNumber + 1;

        return PREFIX + String.format("%0" + PAD_LENGTH + "d", nextNumber);
    }

    private int parseNumber(String code) {
        try {
            return Integer.parseInt(code.substring(PREFIX.length()));
        } catch (Exception ex) {
            return 0;
        }
    }
}
