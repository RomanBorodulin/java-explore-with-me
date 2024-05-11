package ru.practicum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.mapper.EndpointHitMapper;
import ru.practicum.model.EndpointHit;
import ru.practicum.service.StatsService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.constants.Constants.FORMATTER;

@WebMvcTest(controllers = StatsController.class)
public class StatsControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private StatsService statsService;

    private EndpointHitDto expectedEndpointHitDto;
    private EndpointHit expectedEndpointHit;

    @BeforeEach
    void setUp() {
        expectedEndpointHitDto = EndpointHitDto.builder()
                .id(1)
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.163.0.1")
                .timestamp(LocalDateTime.parse("2022-09-06 11:00:23", FORMATTER))
                .build();
        expectedEndpointHit = EndpointHitMapper.toEndpointHit(expectedEndpointHitDto);
    }

    @Test
    @SneakyThrows
    public void save_whenEndpointValid_thenReturnCreatedRequest() {
        when(statsService.add(expectedEndpointHitDto)).thenReturn(expectedEndpointHitDto);
        String result = mockMvc.perform(MockMvcRequestBuilders.post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expectedEndpointHitDto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(expectedEndpointHitDto), result);
    }

    @Test
    @SneakyThrows
    public void save_whenEndpointNotValid_thenReturnBadRequest() {
        EndpointHitDto endpointToSave = EndpointHitMapper.toEndpointHitDto(expectedEndpointHit);
        endpointToSave.setIp("");
        String result = mockMvc.perform(MockMvcRequestBuilders.post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(endpointToSave)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(statsService, never()).add(any());
    }

    @Test
    @SneakyThrows
    public void getStats_whenParametersNotValid_thenReturnException() {
        ViewStatsDto viewStatsDto = new ViewStatsDto();
        viewStatsDto.setApp("ewm-main-service");
        viewStatsDto.setUri("/events/1");
        viewStatsDto.setHits(1L);
        String result = mockMvc.perform(MockMvcRequestBuilders.get("/stats")
                        .param("start", "2022-09-06T11:00:23")
                        .param("end", "")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(viewStatsDto)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(statsService, never()).getStats(any(), any(), anyList(), any());
    }

}
