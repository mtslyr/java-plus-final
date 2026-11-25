package ru.practicum.ewm.stats.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;

@ConfigurationProperties(prefix = "stats.weights")
public record WeightsConfig(
        double view,
        double register,
        double like
) {
    public double weightOf(ActionTypeAvro type) {
        return switch (type) {
            case LIKE -> like;
            case VIEW -> view;
            case REGISTER -> register;
        };
    }
}
