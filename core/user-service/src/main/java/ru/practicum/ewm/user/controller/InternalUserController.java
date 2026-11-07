package ru.practicum.ewm.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.interaction.api.UserApi;
import ru.practicum.ewm.interaction.dto.UserShortDto;
import ru.practicum.ewm.subscription.repository.SubscriptionRepository;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InternalUserController implements UserApi {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserMapper mapper;

    @Override
    public List<UserShortDto> getShortByIds(Collection<Long> ids) {
        return userRepository.findAllById(ids).stream()
                .map(mapper::toUserShortDto)
                .toList();
    }

    @Override
    public boolean existsById(Long userId) {
        return userRepository.existsById(userId);
    }

    @Override
    public Set<Long> getFollowedIds(Long followerId, Collection<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptySet();
        }
        return subscriptionRepository.findConfirmedFollowedIds(followerId, userIds);
    }
}
