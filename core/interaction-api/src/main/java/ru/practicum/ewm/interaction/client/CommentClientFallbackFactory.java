package ru.practicum.ewm.interaction.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

@Slf4j
@Component
public class CommentClientFallbackFactory implements FallbackFactory<CommentClient> {

    @Override
    public CommentClient create(Throwable cause) {
        return new CommentClient() {
            @Override
            public Map<Long, Long> getCommentCounts(Collection<Long> eventIds) {
                log.warn("Comment counts are unavailable: {}", cause.getMessage());
                return Map.of();
            }

            @Override
            public void deleteUserData(Long userId, Collection<Long> eventIds) {
                log.error("Failed to delete comments of user {}: {}", userId, cause.getMessage());
            }
        };
    }
}
