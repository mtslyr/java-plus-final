package ru.practicum.ewm.subscription.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionStatusUpdateRequest {

    private String status;
}
