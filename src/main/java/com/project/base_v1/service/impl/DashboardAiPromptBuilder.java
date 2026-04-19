package com.project.base_v1.service.impl;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.base_v1.dto.response.dashboard.DashboardAiContextResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DashboardAiPromptBuilder {

    private final ObjectMapper objectMapper;

    public String buildPrompt(DashboardAiContextResponse context) {
        try {
            String jsonContext = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(context);

            return """
                    Bạn là trợ lý phân tích kinh doanh cho phòng khám nha khoa.
                    
                    Nhiệm vụ:
                    Phân tích dữ liệu kinh doanh theo tháng của phòng khám dựa hoàn toàn trên dữ liệu được cung cấp.
                    
                    Mục tiêu phân tích:
                    1. So sánh tháng hiện tại với tháng trước.
                    2. So sánh tháng hiện tại với cùng kỳ năm trước nếu dữ liệu đủ tin cậy.
                    3. Đánh giá xu hướng doanh thu, chi phí, lợi nhuận.
                    4. Phát hiện rủi ro tồn kho, thuốc sắp hết hạn, thuốc tồn thấp.
                    5. Đưa ra khuyến nghị hành động cụ thể trong 30 ngày tới.
                    6. Đưa ra nhận xét về danh sách đề xuất nhập thuốc đã được hệ thống tính sẵn.
                    
                    Quy tắc bắt buộc:
                    1. Chỉ sử dụng dữ liệu trong phần context được cung cấp.
                    2. Không được bịa thêm số liệu, không suy đoán vô căn cứ.
                    3. Phải phân biệt rõ:
                       - dữ liệu chắc chắn từ context
                       - suy luận hợp lý
                       - khuyến nghị hành động
                    4. Nếu phát hiện dữ liệu mâu thuẫn, bất thường hoặc không đủ cơ sở, không được kết luận chắc chắn.
                       Phải ghi rõ là:
                       - "cần kiểm tra lại dữ liệu"
                       - hoặc "có dấu hiệu bất nhất dữ liệu"
                    5. Không lặp lại nguyên văn quá nhiều KPI nếu đã rõ từ dữ liệu.
                    6. Mỗi ý trong highlights, risks, recommendations nên ngắn gọn, tối đa 1-2 câu.
                    7. Recommendations phải theo hướng hành động cụ thể, ưu tiên thực hiện trong 30 ngày tới.
                    8. Không được tự tính hoặc tự bịa số lượng nhập thuốc ngoài trường suggestedQuantity đã có sẵn từ hệ thống.
                       Nếu suggestedQuantity có dấu hiệu bất thường, chỉ được nêu là cần rà soát lại logic tính toán.
                    9. Nếu dữ liệu cùng kỳ năm trước bằng 0 hoặc không đủ, phải nói rõ là chưa đủ cơ sở so sánh năm.
                    10. Ngôn ngữ trả lời phải là tiếng Việt, rõ ràng, ngắn gọn, phù hợp cho dashboard quản trị.
                    
                    Cách viết mong muốn:
                    - executiveSummary: 2-4 câu, tóm tắt điều hành ngắn gọn
                    - highlights: 3-5 ý ngắn
                    - risks: 3-5 ý ngắn
                    - recommendations: 3-5 ý ngắn, có tính hành động
                    - dataQualityNotes: 0-5 ý, chỉ ghi khi có dữ liệu bất thường hoặc thiếu
                    - procurementSuggestions: giữ nguyên cấu trúc, nhưng có thể viết lại trường reason cho rõ ràng hơn
                    
                    Trả về JSON hợp lệ đúng schema sau:
                    {
                      "periodLabel": "string",
                      "executiveSummary": "string",
                      "highlights": ["string"],
                      "risks": ["string"],
                      "recommendations": ["string"],
                      "dataQualityNotes": ["string"],
                      "procurementSuggestions": [
                        {
                          "medicineId": "uuid",
                          "medicineCode": "string",
                          "medicineName": "string",
                          "currentStock": 0,
                          "nearExpiryQty": 0,
                          "monthlyDispensedQty": 0,
                          "suggestedQuantity": 0,
                          "priority": "HIGH|MEDIUM|LOW",
                          "reason": "string"
                        }
                      ]
                    }
                    
                    Hướng dẫn ưu tiên nội dung:
                    - Ưu tiên phân tích doanh thu thuần, lợi nhuận ước tính, chi phí nhập thuốc, chi phí vận hành.
                    - Ưu tiên nhận diện biến động lớn giữa tháng hiện tại và tháng trước.
                    - Nếu tháng trước có giá trị âm hoặc bất thường, hãy giải thích thận trọng, không diễn giải sai dấu phần trăm tăng trưởng.
                    - Ưu tiên cảnh báo thuốc tồn thấp, thuốc gần hết hạn, thuốc có dữ liệu bất nhất.
                    - Ưu tiên khuyến nghị hành động có thể triển khai trong 30 ngày tới.
                    
                    Context dữ liệu:
                    %s
                    """.formatted(jsonContext);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Không thể build AI prompt", e);
        }
    }
}