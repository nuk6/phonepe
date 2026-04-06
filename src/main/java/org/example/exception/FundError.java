package org.example.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FundError implements Error {

    FUND_NOT_FOUND(404, "FUND_NOT_FOUND", "No mutual fund found with the given id");

    private final int status;
    private final String code;
    private final String description;
}

