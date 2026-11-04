package ru.practicum.ewm.stats.base;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.MaxAttemptsRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;
import ru.practicum.ewm.stats.exception.StatsServerUnavailable;

import java.net.URI;
import java.time.Duration;

public class BaseClient {
    private static final String STATS_SERVER_ID = "stats-server";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

    private final DiscoveryClient discoveryClient;
    private final RetryTemplate retryTemplate;
    protected final RestTemplate rest;

    public BaseClient(RestTemplateBuilder restTemplateBuilder, DiscoveryClient discoveryClient) {
        this.rest = restTemplateBuilder
                .setConnectTimeout(CONNECT_TIMEOUT)
                .setReadTimeout(READ_TIMEOUT)
                .build();
        this.discoveryClient = discoveryClient;
        this.retryTemplate = new RetryTemplate();

        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(3000L);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

        MaxAttemptsRetryPolicy retryPolicy = new MaxAttemptsRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);
    }

    private ServiceInstance getStatsService() {
        try {
            return discoveryClient
                    .getInstances(STATS_SERVER_ID)
                    .getFirst();
        } catch (Exception e) {
            throw new StatsServerUnavailable(
                    "Ошибка обнаружения адреса сервиса статистики с id: " + STATS_SERVER_ID,
                    e
            );
        }
    }

    protected URI makeUri(String path) {
        ServiceInstance service = retryTemplate.execute(ctx -> getStatsService());
        return URI.create("http://" + service.getHost() + ":" + service.getPort() + path);
    }
}
