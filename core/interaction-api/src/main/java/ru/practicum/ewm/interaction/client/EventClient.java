package ru.practicum.ewm.interaction.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.ewm.interaction.api.EventApi;

@FeignClient(name = "event-service", fallbackFactory = EventClientFallbackFactory.class)
public interface EventClient extends EventApi {
}
