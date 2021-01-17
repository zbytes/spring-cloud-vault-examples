package io.github.zbytes.examples.orderservice.core;

public class ViolationError extends RuntimeException {

    public ViolationError(String message) {
        super(message);
    }
}
