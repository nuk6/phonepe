package org.example.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class LumpSumRequest {

    @Min(value = 1, message = "missedInstallments must be at least 1")
    private int missedInstallments;
}

