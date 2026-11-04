package ru.practicum.ewm.user.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.interaction.client.EventClient;
import ru.practicum.ewm.interaction.client.RequestClient;
import ru.practicum.ewm.interaction.client.CommentClient;

@Configuration
@EnableFeignClients(clients = {EventClient.class, RequestClient.class, CommentClient.class})
public class FeignClientsConfig {
}
