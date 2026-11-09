package ru.practicum.ewm.interaction.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.interaction.dto.EventInternalDto;

import java.util.List;

@Slf4j
@Component
public class EventClientFallbackFactory implements FallbackFactory<EventClient> {

    private static final String SERVICE = "event-service";

    @Override
    public EventClient create(Throwable cause) {
        return new EventClient() {
            @Override
            public EventInternalDto getById(Long eventId) {
                throw FallbackSupport.propagate(SERVICE, cause);
            }

            @Override
            public List<Long> deleteUserData(Long userId) {
                log.error("Failed to delete events of user {}: {}", userId, cause.getMessage());
                return List.of();
            }
        };
    }
}
