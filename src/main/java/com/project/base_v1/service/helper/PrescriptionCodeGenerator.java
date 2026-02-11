package com.project.base_v1.service.helper;

import com.project.base_v1.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PrescriptionCodeGenerator {

    private static final String PREFIX = "DT";
    private static final int PAD = 6;

    private final PrescriptionRepository repo;

    public String nextCode() {
        String last = repo.findLatestCode().orElse(PREFIX + "0".repeat(PAD));
        int n;
        try {
            n = Integer.parseInt(last.substring(PREFIX.length()));
        } catch (Exception e) {
            n = 0;
        }
        return PREFIX + String.format("%0" + PAD + "d", n + 1);
    }
}
