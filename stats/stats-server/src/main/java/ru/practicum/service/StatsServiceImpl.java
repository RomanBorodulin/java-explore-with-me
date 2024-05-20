package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.EndpointHitMapper;
import ru.practicum.model.EndpointHit;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

    @Override
    public EndpointHitDto add(EndpointHitDto endpointHitDto) {
        EndpointHit endpointHit = EndpointHitMapper.toEndpointHit(endpointHitDto);
        validateEndpointHit(endpointHit);
        return EndpointHitMapper.toEndpointHitDto(statsRepository.save(endpointHit));
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (start.isAfter(end) || start.isEqual(end)) {
            throw new ValidationException("Дата окончания не может быть раньше или равна дате начала");
        }
        if (uris == null) {
            return unique ? statsRepository.findUniqueStats(start, end) :
                    statsRepository.findStats(start, end);
        } else {
            return unique ? statsRepository.findUniqueStats(start, end, uris) :
                    statsRepository.findStats(start, end, uris);
        }
    }

    private void validateEndpointHit(EndpointHit endpointHit) {
        if (endpointHit == null) {
            log.warn("Получен null");
            throw new ValidationException("Передан null объект");
        }
    }
}
