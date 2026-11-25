package ru.practicum.ewm.interaction.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

@Slf4j
@Component
public class RequestClientFallbackFactory implements FallbackFactory<RequestClient> {

    private static final String SERVICE = "request-service";

    @Override
    public RequestClient create(Throwable cause) {
        return new RequestClient() {
            @Override
            public Map<Long, Long> getConfirmedCounts(Collection<Long> eventIds) {
                log.warn("Confirmed requests are unavailable: {}", cause.getMessage());
                return Map.of();
            }

            @Override
            public boolean isParticipant(Long userId, Long eventId) {
                throw FallbackSupport.propagate(SERVICE, cause);
            }

            @Override
            public void deleteUserData(Long userId, Collection<Long> eventIds) {
                log.error("Failed to delete requests of user {}: {}", userId, cause.getMessage());
            }
        };
    }
}
