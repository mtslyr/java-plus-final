package ru.practicum.ewm.stats.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.ewm.stats.dto.StatHitRequest;
import ru.practicum.ewm.stats.dto.StatHitResponseElement;
import ru.practicum.ewm.stats.mapper.EndpointHitMapper;
import ru.practicum.ewm.stats.repository.EndpointHitRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final EndpointHitRepository repository;

    @Override
    @Transactional
    public void saveHit(StatHitRequest request) {
        repository.save(EndpointHitMapper.toEntity(request));
    }

    @Override
    public List<StatHitResponseElement> getStats(LocalDateTime start,
                                                 LocalDateTime end,
                                                 List<String> uris,
                                                 Boolean unique) {
        if (start.isAfter(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start must be before end");
        }

        boolean hasUris = uris != null && !uris.isEmpty();
        if (Boolean.TRUE.equals(unique)) {
            return hasUris
                    ? repository.findUniqueStats(start, end, uris)
                    : repository.findUniqueStats(start, end);
        }

        return hasUris
                ? repository.findStats(start, end, uris)
                : repository.findStats(start, end);
    }
}
