package ru.practicum.ewm.stats.clients;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.ewm.stats.base.BaseClient;
import ru.practicum.ewm.stats.dto.StatHitResponseElement;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static ru.practicum.ewm.stats.base.ClientConstants.*;

@Slf4j
@Component
public class StatsClient extends BaseClient {

    public StatsClient(RestTemplateBuilder builder, @Value("${stats-server.url}") String serverUrl) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(() -> new JdkClientHttpRequestFactory())
                        .build()
        );
    }

    public List<StatHitResponseElement> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        log.debug("Get stats from {} to {}, uris={}, unique={}", start, end, uris, unique);

        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(API_PREFIX_STATS)
                .queryParam(PARAM_START, start.format(DATE_TIME_FORMATTER))
                .queryParam(PARAM_END, end.format(DATE_TIME_FORMATTER))
                .queryParam(PARAM_UNIQUE, unique);

        if (uris != null && !uris.isEmpty()) {
            for (String uri : uris) {
                builder.queryParam(PARAM_URIS, uri);
            }
        }

        String uri = builder.build().toUriString();

        ResponseEntity<List<StatHitResponseElement>> response = rest.exchange(
                uri,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<StatHitResponseElement>>() {
                }
        );
        List<StatHitResponseElement> body = response.getBody();
        return body != null ? body : Collections.emptyList();
    }
}