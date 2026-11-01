package ru.practicum.ewm.stats.clients;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.ewm.stats.base.BaseClient;
import ru.practicum.ewm.stats.dto.StatHitRequest;

import static ru.practicum.ewm.stats.base.ClientConstants.API_PREFIX_HIT;

@Slf4j
@Component
public class HitClient extends BaseClient {

    public HitClient(RestTemplate restTemplate,
                     DiscoveryClient discoveryClient) {
        super(restTemplate, discoveryClient);
    }

    public void saveHit(StatHitRequest request) {
        log.debug("Post saveHit request: {}", request);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<StatHitRequest> entity = new HttpEntity<>(request, headers);

        rest.exchange(
                makeUri(API_PREFIX_HIT),
                HttpMethod.POST,
                entity,
                Void.class);
    }
}