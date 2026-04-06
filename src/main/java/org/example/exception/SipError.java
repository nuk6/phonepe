package org.example.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SipError implements Error {

    SIP_NOT_FOUND(404, "SIP_NOT_FOUND", "No SIP found with the given id"),
    SIP_ALREADY_PAUSED(409, "SIP_ALREADY_PAUSED", "SIP is already in paused state"),
    SIP_ALREADY_ACTIVE(409, "SIP_ALREADY_ACTIVE", "SIP is already in active state"),
    SIP_STOPPED(400, "SIP_STOPPED", "Cannot perform operations on a stopped SIP"),
    INVALID_AMOUNT(400, "INVALID_AMOUNT", "SIP amount must be greater than zero"),
    INVALID_STEP_UP_PERCENTAGE(400, "INVALID_STEP_UP_PERCENTAGE", "Step up percentage must be between 0 and 100");

    private final int status;
    private final String code;
    private final String description;
}

