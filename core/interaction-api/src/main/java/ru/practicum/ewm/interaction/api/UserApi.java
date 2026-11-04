package ru.practicum.ewm.interaction.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.ewm.interaction.dto.UserShortDto;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface UserApi {

    @GetMapping("/internal/users")
    List<UserShortDto> getShortByIds(@RequestParam Collection<Long> ids);

    @GetMapping("/internal/users/{userId}/exists")
    boolean existsById(@PathVariable Long userId);

    @GetMapping("/internal/users/{followerId}/followed")
    Set<Long> getFollowedIds(@PathVariable Long followerId, @RequestParam Collection<Long> userIds);
}
