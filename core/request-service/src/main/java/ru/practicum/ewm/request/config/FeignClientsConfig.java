package ru.practicum.ewm.request.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.interaction.client.UserClient;
import ru.practicum.ewm.interaction.client.EventClient;

@Configuration
@EnableFeignClients(clients = {UserClient.class, EventClient.class})
public class FeignClientsConfig {
}
