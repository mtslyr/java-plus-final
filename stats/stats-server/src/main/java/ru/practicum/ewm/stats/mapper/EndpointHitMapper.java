package ru.practicum.ewm.stats.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.stats.dto.StatHitRequest;
import ru.practicum.ewm.stats.model.EndpointHit;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EndpointHitMapper {
    public static EndpointHit toEntity(StatHitRequest request) {
        return new EndpointHit(
                null,
                request.getAppName(),
                request.getUri(),
                request.getIp(),
                request.getTimestamp()
        );
    }
}
