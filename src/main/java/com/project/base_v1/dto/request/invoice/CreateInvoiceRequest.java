package com.project.base_v1.dto.request.invoice;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateInvoiceRequest(
        @NotNull UUID patientId,
        UUID treatmentPlanId,                // optional: kéo item DONE
        String note,

        BigDecimal discountAmount,           // giảm cho toàn hóa đơn (optional)
        List<CreateInvoiceItemRequest> items // optional: nếu không kéo từ plan
) {
}
