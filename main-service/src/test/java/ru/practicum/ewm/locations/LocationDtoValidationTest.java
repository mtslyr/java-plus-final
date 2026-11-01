package ru.practicum.ewm.locations;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.locations.controllers.*;
import ru.practicum.ewm.locations.dto.NewLocationDto;
import ru.practicum.ewm.locations.services.LocationService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AdminLocationController.class, PublicLocationController.class})
class LocationDtoValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LocationService locationService;

    @Test
    @DisplayName("NewLocationDto — отсутствует eventId")
    void create_shouldFailWithoutEventId() throws Exception {
        NewLocationDto dto = NewLocationDto.builder()
                .name("Test")
                .lat(55.75f)
                .lon(37.61f)
                .build();

        mockMvc.perform(post("/admin/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldFailWithoutLat() throws Exception {
        NewLocationDto dto = NewLocationDto.builder()
                .eventId(1L)
                .name("Test")
                .lon(37.61f)
                .build();

        mockMvc.perform(post("/admin/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldFailWithoutLon() throws Exception {
        NewLocationDto dto = NewLocationDto.builder()
                .eventId(1L)
                .name("Test")
                .lat(55.75f)
                .build();

        mockMvc.perform(post("/admin/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(floats = {-91.0f, -100.0f, 91.0f, 100.0f})
    void create_shouldFailWithInvalidLat(float invalidLat) throws Exception {
        NewLocationDto dto = NewLocationDto.builder()
                .eventId(1L)
                .name("Test")
                .lat(invalidLat)
                .lon(37.61f)
                .build();

        mockMvc.perform(post("/admin/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(floats = {-181.0f, -200.0f, 181.0f, 200.0f})
    void create_shouldFailWithInvalidLon(float invalidLon) throws Exception {
        NewLocationDto dto = NewLocationDto.builder()
                .eventId(1L)
                .name("Test")
                .lat(55.75f)
                .lon(invalidLon)
                .build();

        mockMvc.perform(post("/admin/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldFailWithLongName() throws Exception {
        String longName = "a".repeat(101);
        NewLocationDto dto = NewLocationDto.builder()
                .eventId(1L)
                .name(longName)
                .lat(55.75f)
                .lon(37.61f)
                .build();

        mockMvc.perform(post("/admin/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void search_shouldFailWithoutLat() throws Exception {
        mockMvc.perform(get("/locations")
                        .param("lon", "37.61")
                        .param("radius", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void search_shouldFailWithoutLon() throws Exception {
        mockMvc.perform(get("/locations")
                        .param("lat", "55.75")
                        .param("radius", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void search_shouldFailWithoutRadius() throws Exception {
        mockMvc.perform(get("/locations")
                        .param("lat", "55.75")
                        .param("lon", "37.61"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void search_shouldFailWithZeroRadius() throws Exception {
        mockMvc.perform(get("/locations")
                        .param("lat", "55.75")
                        .param("lon", "37.61")
                        .param("radius", "0"))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-91", "91", "-100", "100"})
    void search_shouldFailWithInvalidLat(String invalidLat) throws Exception {
        mockMvc.perform(get("/locations")
                        .param("lat", invalidLat)
                        .param("lon", "37.61")
                        .param("radius", "10"))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-181", "181", "-200", "200"})
    void search_shouldFailWithInvalidLon(String invalidLon) throws Exception {
        mockMvc.perform(get("/locations")
                        .param("lat", "55.75")
                        .param("lon", invalidLon)
                        .param("radius", "10"))
                .andExpect(status().isBadRequest());
    }
}