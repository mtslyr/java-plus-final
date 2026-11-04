package ru.practicum.ewm.subscription.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionStatusUpdateResult {

    private Long id;

    private String status;
}
