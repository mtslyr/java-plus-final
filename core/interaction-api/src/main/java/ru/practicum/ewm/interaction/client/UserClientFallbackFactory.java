package ru.practicum.ewm.interaction.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.interaction.dto.UserShortDto;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {

    private static final String SERVICE = "user-service";

    @Override
    public UserClient create(Throwable cause) {
        return new UserClient() {
            @Override
            public List<UserShortDto> getShortByIds(Collection<Long> ids) {
                throw FallbackSupport.propagate(SERVICE, cause);
            }

            @Override
            public boolean existsById(Long userId) {
                throw FallbackSupport.propagate(SERVICE, cause);
            }

            @Override
            public Set<Long> getFollowedIds(Long followerId, Collection<Long> userIds) {
                log.warn("Subscriptions of user {} are unavailable: {}", followerId, cause.getMessage());
                return Set.of();
            }
        };
    }
}
