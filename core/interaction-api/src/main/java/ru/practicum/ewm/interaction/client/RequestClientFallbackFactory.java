package ru.practicum.ewm.interaction.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

@Slf4j
@Component
public class RequestClientFallbackFactory implements FallbackFactory<RequestClient> {

    @Override
    public RequestClient create(Throwable cause) {
        return new RequestClient() {
            @Override
            public Map<Long, Long> getConfirmedCounts(Collection<Long> eventIds) {
                log.warn("Confirmed requests are unavailable: {}", cause.getMessage());
                return Map.of();
            }

            @Override
            public void deleteUserData(Long userId, Collection<Long> eventIds) {
                log.error("Failed to delete requests of user {}: {}", userId, cause.getMessage());
            }
        };
    }
}
