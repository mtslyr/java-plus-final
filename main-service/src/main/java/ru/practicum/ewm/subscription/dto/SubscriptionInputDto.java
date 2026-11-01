package ru.practicum.ewm.subscription.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionInputDto {

    private Long targetUserId;
}
