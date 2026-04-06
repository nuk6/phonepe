package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.example.enums.SipMode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateSipRequest {

    @NotBlank(message = "userId is required")
    private String userId;

    @NotBlank(message = "fundId is required")
    private String fundId;

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "mode is required")
    private SipMode mode;

    @NotNull(message = "startDate is required")
    private LocalDate startDate;

    @Min(value = 0, message = "stepUpPercentage cannot be negative")
    @Max(value = 100, message = "stepUpPercentage cannot exceed 100")
    private double stepUpPercentage;
}

