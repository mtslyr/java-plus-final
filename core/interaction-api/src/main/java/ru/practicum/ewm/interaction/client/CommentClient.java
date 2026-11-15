package ru.practicum.ewm.interaction.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.ewm.interaction.api.CommentApi;

@FeignClient(name = "comment-service", fallbackFactory = CommentClientFallbackFactory.class)
public interface CommentClient extends CommentApi {
}
