package examples.orderservice.core;

import lombok.experimental.UtilityClass;

import java.util.function.Supplier;

@UtilityClass
public class Rejects {

    public static void ifNegative(Long value, Supplier<String> message) {
        if (value == null || value < 0) {
            raise(message);
        }
    }
    public static void ifNegative(Integer value, Supplier<String> message) {
        if (value == null || value < 0) {
            raise(message);
        }
    }

    public static void ifFalse(boolean value, Supplier<String> message) {
        if (!value) {
            raise(message);
        }
    }

    public static void ifBlank(String value, Supplier<String> message) {
        if (value == null || value.trim().isEmpty()) {
            raise(message);
        }
    }

    private static void raise(Supplier<String> message) {
        throw new ViolationError(message.get());
    }
}
