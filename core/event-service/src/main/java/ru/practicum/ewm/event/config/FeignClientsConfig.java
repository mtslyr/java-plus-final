package ru.practicum.ewm.event.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.interaction.client.UserClient;
import ru.practicum.ewm.interaction.client.RequestClient;
import ru.practicum.ewm.interaction.client.CommentClient;

@Configuration
@EnableFeignClients(clients = {UserClient.class, RequestClient.class, CommentClient.class})
public class FeignClientsConfig {
}
