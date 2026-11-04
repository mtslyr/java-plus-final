package ru.practicum.ewm.subscription.service;

import ru.practicum.ewm.subscription.dto.SubscriptionDto;
import ru.practicum.ewm.subscription.dto.SubscriptionInputDto;
import ru.practicum.ewm.subscription.dto.SubscriptionStatusUpdateResult;
import ru.practicum.ewm.user.dto.UserShortDto;

import java.util.List;

public interface SubscriptionService {

    SubscriptionDto create(Long userId, SubscriptionInputDto dto);

    SubscriptionStatusUpdateResult confirm(Long userId, Long subscriptionId);

    SubscriptionStatusUpdateResult reject(Long userId, Long subscriptionId);

    List<SubscriptionDto> getAllByUser(Long userId);

    List<SubscriptionDto> getIncoming(Long userId);

    List<SubscriptionDto> getOutgoing(Long userId);

    void cancel(Long userId, Long subscriptionId);

    List<UserShortDto> getFollowers(Long userId);
}
