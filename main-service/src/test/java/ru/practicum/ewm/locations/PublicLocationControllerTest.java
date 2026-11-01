package ru.practicum.ewm.locations;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.locations.controllers.PublicLocationController;
import ru.practicum.ewm.locations.dto.LocationSearchRequest;
import ru.practicum.ewm.locations.services.LocationService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PublicLocationController.class)
class PublicLocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LocationService locationService;

    private static final String BASE_URL = "/locations";

    @Test
    void getEventsInLocation_shouldReturnEvents() throws Exception {
        EventShortDto event = EventShortDto.builder()
                .id(1L)
                .title("Event in radius")
                .annotation("Test event")
                .eventDate(String.valueOf(LocalDateTime.now().plusDays(1)))
                .build();

        when(locationService.getEventsInLocation(any(LocationSearchRequest.class)))
                .thenReturn(List.of(event));

        mockMvc.perform(get(BASE_URL)
                        .param("lat", "55.75")
                        .param("lon", "37.61")
                        .param("radius", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("Event in radius"));
    }

    @Test
    void getEventsInLocation_distanceEqualsRadius() throws Exception {
        EventShortDto eventOnBorder = EventShortDto.builder()
                .id(1L)
                .title("On border")
                .annotation("Event on border")
                .eventDate(String.valueOf(LocalDateTime.now().plusDays(1)))
                .build();

        when(locationService.getEventsInLocation(any(LocationSearchRequest.class)))
                .thenReturn(List.of(eventOnBorder));

        mockMvc.perform(get(BASE_URL)
                        .param("lat", "55.75")
                        .param("lon", "37.61")
                        .param("radius", "5.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("On border"));
    }

    @Test
    void getEventsInLocation_distanceGreaterThanRadius() throws Exception {
        when(locationService.getEventsInLocation(any(LocationSearchRequest.class)))
                .thenReturn(List.of());

        mockMvc.perform(get(BASE_URL)
                        .param("lat", "55.75")
                        .param("lon", "37.61")
                        .param("radius", "1.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getEventsInLocation_distanceLessThanRadius() throws Exception {
        EventShortDto eventInside = EventShortDto.builder()
                .id(2L)
                .title("Inside radius")
                .annotation("Event inside")
                .eventDate(String.valueOf(LocalDateTime.now().plusDays(2)))
                .build();

        when(locationService.getEventsInLocation(any(LocationSearchRequest.class)))
                .thenReturn(List.of(eventInside));

        mockMvc.perform(get(BASE_URL)
                        .param("lat", "55.75")
                        .param("lon", "37.61")
                        .param("radius", "50.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Inside radius"));
    }
}