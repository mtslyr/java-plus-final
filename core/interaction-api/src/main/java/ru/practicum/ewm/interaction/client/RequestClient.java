package ru.practicum.ewm.interaction.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.ewm.interaction.api.RequestApi;

@FeignClient(name = "request-service", fallbackFactory = RequestClientFallbackFactory.class)
public interface RequestClient extends RequestApi {
}
