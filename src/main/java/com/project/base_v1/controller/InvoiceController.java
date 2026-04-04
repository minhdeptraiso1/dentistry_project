package com.project.base_v1.controller;

import com.project.base_v1.dto.request.invoice.CreateInvoiceFromPrescriptionRequest;
import com.project.base_v1.dto.request.invoice.CreateInvoiceRequest;
import com.project.base_v1.dto.request.invoice.InvoiceSearchRequest;
import com.project.base_v1.dto.request.invoice.IssueInvoiceRequest;
import com.project.base_v1.dto.request.payment.AddPaymentRequest;
import com.project.base_v1.dto.response.core.ApiResponseSever;
import com.project.base_v1.dto.response.invoice.InvoiceMyResponse;
import com.project.base_v1.dto.response.invoice.InvoiceResponse;
import com.project.base_v1.dto.response.invoice.InvoiceSummaryResponse;
import com.project.base_v1.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Invoice", description = "APIs for invoices & payments")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/invoices")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InvoiceController {

    InvoiceService invoiceService;

    @Operation(
            summary = "Create invoice",
            description = """
                    Create invoice for a patient.
                    <br/>
                    - Option A: pass treatmentPlanId => auto import DONE treatment items
                    <br/>
                    - Option B: pass items manually
                    <br/>
                    <b>Roles:</b> CASHIER, ADMIN
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('CASHIER','ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponseSever<InvoiceResponse> create(@Valid @RequestBody CreateInvoiceRequest request) {
        return ApiResponseSever.ok(invoiceService.create(request));
    }

    @Operation(summary = "Get invoice detail", description = "Return invoice with items & payments")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PreAuthorize("hasAnyRole('CASHIER','ADMIN','DOCTOR')")
    @GetMapping("/{id}")
    public ApiResponseSever<InvoiceResponse> getById(@PathVariable UUID id) {
        return ApiResponseSever.ok(invoiceService.getById(id));
    }

    @Operation(
            summary = "Issue invoice",
            description = """
                    Confirm/issue invoice.
                    <br/>
                    Only DRAFT can be issued.
                    <br/>
                    <b>Roles:</b> CASHIER, ADMIN
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Issued"),
            @ApiResponse(responseCode = "400", description = "Invalid status"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PreAuthorize("hasAnyRole('CASHIER','ADMIN')")
    @PostMapping("/{id}/issue")
    public ApiResponseSever<InvoiceResponse> issue(
            @PathVariable UUID id,
            @RequestBody(required = false) IssueInvoiceRequest request
    ) {
        return ApiResponseSever.ok(invoiceService.issue(id, request));
    }

    @Operation(
            summary = "Add payment",
            description = """
                    Add payment to invoice.
                    <br/>
                    Only ISSUED/PARTIALLY_PAID can accept payment.
                    <br/>
                    Auto update status: PARTIALLY_PAID / PAID.
                    <br/>
                    <b>Roles:</b> CASHIER, ADMIN
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment added"),
            @ApiResponse(responseCode = "400", description = "Invalid payment"),
            @ApiResponse(responseCode = "404", description = "Invoice not found")
    })
    @PreAuthorize("hasAnyRole('CASHIER','ADMIN')")
    @PostMapping("/{id}/payments")
    public ApiResponseSever<InvoiceResponse> addPayment(
            @PathVariable UUID id,
            @Valid @RequestBody AddPaymentRequest request
    ) {
        return ApiResponseSever.ok(invoiceService.addPayment(id, request));
    }

    @Operation(
            summary = "Cancel invoice",
            description = """
                    Cancel invoice.
                    <br/>
                    PAID invoice cannot be cancelled.
                    <br/>
                    <b>Roles:</b> ADMIN only
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cancelled"),
            @ApiResponse(responseCode = "400", description = "Invoice locked"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/cancel")
    public ApiResponseSever<Void> cancel(
            @PathVariable UUID id,
            @RequestParam(required = false) String note
    ) {
        invoiceService.cancel(id, note);
        return ApiResponseSever.ok(null);
    }

    @Operation(
            summary = "Create invoice from dispensed prescription",
            description = """
                        Create invoice items from prescription items (after DISPENSED).
                        <br/>Pricing: unitPrice = importPrice * markupRate (default 1.2)
                        <br/><b>Roles:</b> CASHIER, ADMIN
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Invalid status / request")
    })
    @PreAuthorize("hasAnyRole('CASHIER','ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/from-prescription")
    public ApiResponseSever<InvoiceResponse> createFromPrescription(
            @Valid @RequestBody CreateInvoiceFromPrescriptionRequest request
    ) {
        return ApiResponseSever.ok(invoiceService.createFromPrescription(request));
    }


    @Operation(summary = "Search invoices", description = "Filter by patientId, status, fromDate, toDate")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Success")})
    @PreAuthorize("hasAnyRole('CASHIER','ADMIN','DOCTOR')")
    @GetMapping
    public ApiResponseSever<Page<InvoiceSummaryResponse>> search(
            @ParameterObject InvoiceSearchRequest request,
            @ParameterObject Pageable pageable
    ) {
        return ApiResponseSever.ok(invoiceService.search(request, pageable));
    }

    @GetMapping("/my")
    public List<InvoiceMyResponse> getMyInvoices() {
        return invoiceService.getMyInvoices();
    }

    @GetMapping("/my/{id}")
    public InvoiceMyResponse getMyInvoiceDetail(@PathVariable UUID id) {
        return invoiceService.getMyInvoiceDetail(id);
    }

}
