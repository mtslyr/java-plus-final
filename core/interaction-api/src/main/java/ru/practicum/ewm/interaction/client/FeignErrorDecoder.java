package ru.practicum.ewm.interaction.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.common.exception.ConflictException;
import ru.practicum.ewm.common.exception.NotFoundException;
import ru.practicum.ewm.common.exception.ValidationException;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper;
    private final ErrorDecoder defaultDecoder = new ErrorDecoder.Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        return switch (response.status()) {
            case 400 -> new ValidationException(readMessage(response));
            case 404 -> new NotFoundException(readMessage(response));
            case 409 -> new ConflictException(readMessage(response));
            default -> defaultDecoder.decode(methodKey, response);
        };
    }

    private String readMessage(Response response) {
        if (response.body() == null) {
            return response.reason();
        }
        try (InputStream body = response.body().asInputStream()) {
            JsonNode message = objectMapper.readTree(body).get("message");
            return message != null ? message.asText() : response.reason();
        } catch (IOException e) {
            log.warn("Failed to read error response body: {}", e.getMessage());
            return response.reason();
        }
    }
}
