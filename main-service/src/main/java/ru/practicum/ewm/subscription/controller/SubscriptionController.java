package ru.practicum.ewm.subscription.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.subscription.dto.SubscriptionDto;
import ru.practicum.ewm.subscription.dto.SubscriptionInputDto;
import ru.practicum.ewm.subscription.dto.SubscriptionStatusUpdateResult;
import ru.practicum.ewm.subscription.service.SubscriptionService;

import jakarta.validation.Valid;
import ru.practicum.ewm.user.dto.UserShortDto;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users/{userId}/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionDto create(@PathVariable Long userId,
                                  @Valid @RequestBody SubscriptionInputDto dto) {
        log.debug("Request to create subscription: userId={}, targetUserId={}", userId, dto.getTargetUserId());
        return subscriptionService.create(userId, dto);
    }

    @PatchMapping("/{subscriptionId}/confirm")
    public SubscriptionStatusUpdateResult confirm(@PathVariable Long userId,
                                                  @PathVariable Long subscriptionId) {
        log.debug("Request to confirm subscription: userId={}, subscriptionId={}", userId, subscriptionId);
        return subscriptionService.confirm(userId, subscriptionId);
    }

    @PatchMapping("/{subscriptionId}/reject")
    public SubscriptionStatusUpdateResult reject(@PathVariable Long userId,
                                                 @PathVariable Long subscriptionId) {
        log.debug("Request to reject subscription: userId={}, subscriptionId={}", userId, subscriptionId);
        return subscriptionService.reject(userId, subscriptionId);
    }

    // получение объектов-подписок
    @GetMapping
    public List<SubscriptionDto> getAllByUser(@PathVariable Long userId) {
        log.debug("Request to get all subscriptions: userId={}", userId);
        return subscriptionService.getAllByUser(userId);
    }

    // получение юзеров-подписчиков
    @GetMapping("/followers")
    public List<UserShortDto> getFollowers(@PathVariable Long userId) {
        log.debug("Request to get followers: userId={}", userId);
        return subscriptionService.getFollowers(userId);
    }

    @GetMapping("/incoming")
    public List<SubscriptionDto> getIncoming(@PathVariable Long userId) {
        log.debug("Request to get incoming subscriptions: userId={}", userId);
        return subscriptionService.getIncoming(userId);
    }

    @GetMapping("/outgoing")
    public List<SubscriptionDto> getOutgoing(@PathVariable Long userId) {
        log.debug("Request to get outgoing subscriptions: userId={}", userId);
        return subscriptionService.getOutgoing(userId);
    }

    @DeleteMapping("/{subscriptionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long userId,
                       @PathVariable Long subscriptionId) {
        log.debug("Request to cancel subscription: userId={}, subscriptionId={}", userId, subscriptionId);
        subscriptionService.cancel(userId, subscriptionId);
    }
}
