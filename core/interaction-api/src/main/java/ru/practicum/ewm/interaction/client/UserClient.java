package ru.practicum.ewm.interaction.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.ewm.interaction.api.UserApi;

@FeignClient(name = "user-service", fallbackFactory = UserClientFallbackFactory.class)
public interface UserClient extends UserApi {
}
