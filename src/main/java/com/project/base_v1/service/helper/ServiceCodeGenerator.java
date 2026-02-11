package com.project.base_v1.service.helper;

import com.project.base_v1.enums.ServiceType;
import com.project.base_v1.repository.ServiceCatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServiceCodeGenerator {

    private final ServiceCatalogRepository repository;

    public String nextCode(ServiceType type) {
        String prefix = type == ServiceType.PACKAGE ? "PK" : "DV";
        int pad = 6;

        // đơn giản: dùng code lớn nhất toàn bảng -> vẫn ok vì prefix khác nhau (PK..., DV...)
        String last = repository.findLatestCode().orElse(prefix + "0".repeat(pad));

        // nếu last là prefix khác => reset về 0 cho prefix hiện tại
        if (!last.startsWith(prefix)) {
            last = prefix + "0".repeat(pad);
        }

        int lastNumber;
        try {
            lastNumber = Integer.parseInt(last.substring(prefix.length()));
        } catch (Exception e) {
            lastNumber = 0;
        }

        return prefix + String.format("%0" + pad + "d", lastNumber + 1);
    }
}
