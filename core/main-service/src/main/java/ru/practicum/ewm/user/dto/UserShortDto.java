package ru.practicum.ewm.user.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserShortDto {

    private Long id;
    private String name;
}
