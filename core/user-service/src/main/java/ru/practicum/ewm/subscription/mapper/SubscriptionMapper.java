package ru.practicum.ewm.subscription.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.subscription.dto.SubscriptionDto;
import ru.practicum.ewm.subscription.model.Subscription;
import ru.practicum.ewm.user.mapper.UserMapper;

@Component
public class SubscriptionMapper {

    private final UserMapper userMapper;

    public SubscriptionMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public SubscriptionDto toSubscriptionDto(Subscription subscription) {
        return SubscriptionDto.builder()
                .id(subscription.getId())
                .follower(userMapper.toUserShortDto(subscription.getFollower()))
                .followed(userMapper.toUserShortDto(subscription.getFollowed()))
                .status(subscription.getStatus().toString())
                .created(subscription.getCreated().toString())
                .build();
    }
}
