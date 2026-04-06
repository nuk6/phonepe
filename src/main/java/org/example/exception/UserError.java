package org.example.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserError implements Error {

    USER_NOT_FOUND(404, "USER_NOT_FOUND", "No user found with the given id");

    private final int status;
    private final String code;
    private final String description;
}

