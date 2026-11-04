package ru.practicum.ewm.locations;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.common.exception.LocationNotFoundException;
import ru.practicum.ewm.event.exception.EventNotFoundException;
import ru.practicum.ewm.locations.controllers.AdminLocationController;
import ru.practicum.ewm.locations.dto.LocationDtoResponse;
import ru.practicum.ewm.locations.dto.NewLocationDto;
import ru.practicum.ewm.locations.dto.UpdateLocationRequest;
import ru.practicum.ewm.locations.services.LocationService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminLocationController.class)
public class AdminLocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LocationService locationService;

    private static final String BASE_URL = "/admin/locations";

    @Test
    public void create_shouldReturnCreatedLocation() throws Exception {
        NewLocationDto dto = NewLocationDto.builder()
                .eventId(1L)
                .name("TL")
                .lat(55.75f)
                .lon(37.61f)
                .build();

        LocationDtoResponse response = LocationDtoResponse.builder()
                .id(1L)
                .eventId(1L)
                .name("TL")
                .lat(55.75f)
                .lon(37.61f)
                .build();

        when(locationService.create(any(NewLocationDto.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.eventId").value(1L))
                .andExpect(jsonPath("$.name").value("TL"))
                .andExpect(jsonPath("$.lat").value(55.75))
                .andExpect(jsonPath("$.lon").value(37.61));
    }

    @Test
    public void create_shouldThrowEventNotFoundException() throws Exception {
        NewLocationDto dto = NewLocationDto.builder()
                .eventId(999L)
                .name("Test")
                .lat(55.75f)
                .lon(37.61f)
                .build();

        when(locationService.create(any(NewLocationDto.class)))
                .thenThrow(new EventNotFoundException(999L));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void patch_shouldReturnUpdatedLocation() throws Exception {
        UpdateLocationRequest dto = UpdateLocationRequest.builder()
                .name("Updated Name")
                .lat(56.0f)
                .build();

        LocationDtoResponse response = LocationDtoResponse.builder()
                .id(1L)
                .eventId(1L)
                .name("Updated Name")
                .lat(56.0f)
                .lon(37.61f)
                .build();

        when(locationService.patch(eq(1L), any(UpdateLocationRequest.class))).thenReturn(response);

        mockMvc.perform(patch(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.lat").value(56.0));
    }

    @Test
    public void patch_shouldThrowLocationNotFoundException() throws Exception {
        UpdateLocationRequest dto = UpdateLocationRequest.builder()
                .name("Updated")
                .build();

        when(locationService.patch(eq(999L), any(UpdateLocationRequest.class)))
                .thenThrow(new LocationNotFoundException(999L));

        mockMvc.perform(patch(BASE_URL + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void patch_shouldThrowEventNotFoundException() throws Exception {
        UpdateLocationRequest dto = UpdateLocationRequest.builder()
                .eventId(888L)
                .build();

        when(locationService.patch(eq(1L), any(UpdateLocationRequest.class)))
                .thenThrow(new EventNotFoundException(888L));

        mockMvc.perform(patch(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void delete_shouldReturnNoContent() throws Exception {
        doNothing().when(locationService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());

        verify(locationService, times(1)).delete(1L);
    }

    @Test
    public void delete_shouldThrowLocationNotFoundException() throws Exception {
        doThrow(new LocationNotFoundException(999L)).when(locationService).delete(999L);

        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getAll_shouldReturnLocationsList() throws Exception {
        LocationDtoResponse loc1 = LocationDtoResponse.builder()
                .id(1L).eventId(1L).name("Loc1").lat(55.75f).lon(37.61f).build();
        LocationDtoResponse loc2 = LocationDtoResponse.builder()
                .id(2L).eventId(2L).name("Loc2").lat(56.0f).lon(38.0f).build();

        when(locationService.getAll(any(PageRequest.class))).thenReturn(List.of(loc1, loc2));

        mockMvc.perform(get(BASE_URL)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    public void getById_shouldReturnLocation() throws Exception {
        LocationDtoResponse response = LocationDtoResponse.builder()
                .id(1L).eventId(1L).name("Test").lat(55.75f).lon(37.61f).build();

        when(locationService.getById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test"));
    }

    @Test
    public void getById_shouldThrowLocationNotFoundException() throws Exception {
        when(locationService.getById(999L)).thenThrow(new LocationNotFoundException(999L));

        mockMvc.perform(get(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }
}