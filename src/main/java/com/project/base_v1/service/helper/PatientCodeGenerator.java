package com.project.base_v1.service.helper;

import com.project.base_v1.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PatientCodeGenerator {

    private static final String PREFIX = "BN";
    private static final int PAD_LENGTH = 6; // BN000001

    private final PatientRepository patientRepository;

    public String nextCode() {
        String last = patientRepository.findLatestPatientCode()
                .orElse(PREFIX + "0".repeat(PAD_LENGTH)); // BN000000

        int lastNumber = parseNumber(last);
        int nextNumber = lastNumber + 1;

        return PREFIX + String.format("%0" + PAD_LENGTH + "d", nextNumber);
    }

    private int parseNumber(String code) {
        // code: BN000123
        try {
            return Integer.parseInt(code.substring(PREFIX.length()));
        } catch (Exception ex) {
            // fallback an toàn nếu DB có dữ liệu bẩn
            return 0;
        }
    }
}
