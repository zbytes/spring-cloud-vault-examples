package io.github.zbytes.examples.orderservice.core;

public class EntityNotFound extends RuntimeException {

    public EntityNotFound(String id) {
        super(String.format("Entity %s not found", id));
    }

}
