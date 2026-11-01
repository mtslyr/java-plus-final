package ru.practicum.ewm.subscription.dto;

import lombok.*;
import ru.practicum.ewm.user.dto.UserShortDto;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionDto {

    private Long id;

    private UserShortDto follower;

    private UserShortDto followed;

    private String status;

    private String created;
}
