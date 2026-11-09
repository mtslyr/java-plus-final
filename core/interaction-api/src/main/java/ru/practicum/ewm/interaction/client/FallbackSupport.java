package ru.practicum.ewm.interaction.client;

import ru.practicum.ewm.common.exception.ApiException;
import ru.practicum.ewm.common.exception.ServiceUnavailableException;

final class FallbackSupport {

    private FallbackSupport() {
    }

    static RuntimeException propagate(String service, Throwable cause) {
        for (Throwable t = cause; t != null; t = t.getCause()) {
            if (t instanceof ApiException apiException) {
                return apiException;
            }
        }
        return new ServiceUnavailableException("%s is unavailable: %s".formatted(service, cause.getMessage()));
    }
}
