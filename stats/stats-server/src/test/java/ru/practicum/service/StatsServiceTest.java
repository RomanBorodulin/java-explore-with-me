package ru.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.EndpointHitMapper;
import ru.practicum.model.EndpointHit;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.practicum.constants.Constants.FORMATTER;

@ExtendWith(MockitoExtension.class)
public class StatsServiceTest {

    @InjectMocks
    private StatsServiceImpl statsService;

    @Mock
    private StatsRepository statsRepository;

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
    public void addEndpoint_whenEndpointValid_thenSavedEndpoint() {
        when(statsRepository.save(any(EndpointHit.class))).thenReturn(expectedEndpointHit);

        EndpointHitDto actualEndpoint = statsService.add(expectedEndpointHitDto);

        assertEquals(expectedEndpointHitDto.getId(), actualEndpoint.getId());
        assertEquals(expectedEndpointHitDto.getApp(), actualEndpoint.getApp());
        assertEquals(expectedEndpointHitDto.getUri(), actualEndpoint.getUri());
        assertEquals(expectedEndpointHitDto.getIp(), actualEndpoint.getIp());
        assertEquals(expectedEndpointHitDto.getTimestamp(), actualEndpoint.getTimestamp());
        verify(statsRepository, times(1)).save(expectedEndpointHit);
    }

    @Test
    public void addEndpoint_whenEndpointNotValid_thenNotSavedEndpoint() {
        assertThrows(ValidationException.class,
                () -> statsService.add(EndpointHitMapper.toEndpointHitDto(null)));
        verify(statsRepository, never()).save(null);
    }

    @Test
    public void getStats_whenStartIsAfterEnd_thenThrownException() {
        assertThrows(ValidationException.class,
                () -> statsService.getStats(LocalDateTime.MAX, LocalDateTime.MIN, null, false));
    }

    @Test
    public void getStats_whenParametersValid_thenReturnViewStatsList() {
        ViewStatsDto viewStatsDto = new ViewStatsDto();
        viewStatsDto.setApp("ewm-main-service");
        viewStatsDto.setUri("/events/1");
        viewStatsDto.setHits(1L);
        List<ViewStatsDto> expectedStats = List.of(viewStatsDto);
        EndpointHitDto actualEndpoint = statsService.add(expectedEndpointHitDto);
        when(statsRepository.findStats(LocalDateTime.MIN, LocalDateTime.MAX))
                .thenReturn(expectedStats);
        when(statsRepository.findStats(LocalDateTime.MIN, LocalDateTime.MAX, List.of("/events/1")))
                .thenReturn(expectedStats);
        when(statsRepository.findUniqueStats(LocalDateTime.MIN, LocalDateTime.MAX))
                .thenReturn(expectedStats);
        when(statsRepository.findUniqueStats(LocalDateTime.MIN, LocalDateTime.MAX, List.of("/events/1")))
                .thenReturn(expectedStats);

        List<ViewStatsDto> actualStats1 = statsService.getStats(LocalDateTime.MIN, LocalDateTime.MAX,
                null, false);
        List<ViewStatsDto> actualStats2 = statsService.getStats(LocalDateTime.MIN, LocalDateTime.MAX,
                List.of("/events/1"), false);
        List<ViewStatsDto> actualStats3 = statsService.getStats(LocalDateTime.MIN, LocalDateTime.MAX,
                null, true);
        List<ViewStatsDto> actualStats4 = statsService.getStats(LocalDateTime.MIN, LocalDateTime.MAX,
                List.of("/events/1"), true);

        assertEquals(expectedStats, actualStats1);
        assertEquals(expectedStats, actualStats2);
        assertEquals(expectedStats, actualStats3);
        assertEquals(expectedStats, actualStats4);
    }
}
