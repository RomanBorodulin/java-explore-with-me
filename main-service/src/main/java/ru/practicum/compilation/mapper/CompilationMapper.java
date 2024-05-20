package ru.practicum.compilation.mapper;

import ru.practicum.client.StatsClient;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.repository.EventRepository;

import java.util.HashSet;
import java.util.stream.Collectors;

import static ru.practicum.utility.StatsUtils.getHits;

public final class CompilationMapper {
    private CompilationMapper() {
    }

    public static CompilationDto toCompilationDto(Compilation compilation, StatsClient statsClient) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .events(compilation.getEvents().stream()
                        .map(c -> EventMapper.toEventShortDto(c, getHits(c.getId(), statsClient)))
                        .collect(Collectors.toSet()))
                .build();
    }

    public static Compilation toCompilation(NewCompilationDto compilationDto, EventRepository eventRepository) {
        return Compilation.builder()
                .pinned(compilationDto.getPinned() != null ? compilationDto.getPinned() : false)
                .events(compilationDto.getEvents() != null ?
                        eventRepository.findAllByIdIn(compilationDto.getEvents()) : new HashSet<>())
                .title(compilationDto.getTitle())
                .build();
    }
}
