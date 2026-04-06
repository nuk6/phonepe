package org.example.exception;

import lombok.Getter;

@Getter
public class PhonePeRuntimeException extends RuntimeException {

    private final Error error;

    public PhonePeRuntimeException(Error error) {
        super(error.getDescription());
        this.error = error;
    }

    public PhonePeRuntimeException(Error error, String message) {
        super(message);
        this.error = error;
    }

    public PhonePeRuntimeException(Error error, Throwable cause) {
        super(error.getDescription(), cause);
        this.error = error;
    }
}

