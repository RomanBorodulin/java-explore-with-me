package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatsClient;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequestDto;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.utility.PageUtils;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.utility.ValidationUtils.getCompilation;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final StatsClient statsClient;

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        Pageable pageable = PageUtils.getPageable(from, size);
        log.info("GET /compilations");
        if (pinned == null) {
            return compilationRepository.findAll(pageable).stream()
                    .map(compilation -> CompilationMapper.toCompilationDto(compilation, statsClient))
                    .collect(Collectors.toList());
        }
        return compilationRepository.findAllByPinned(pinned, pageable).stream()
                .map(compilation -> CompilationMapper.toCompilationDto(compilation, statsClient))
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = getCompilation(compId, compilationRepository);
        log.info("GET /compilations/{}", compId);
        return CompilationMapper.toCompilationDto(compilation, statsClient);
    }

    @Override
    @Transactional
    public CompilationDto add(NewCompilationDto compilationDto) {
        Compilation compilation = compilationRepository
                .save(CompilationMapper.toCompilation(compilationDto, eventRepository));
        log.info("POST /admin/compilations");
        return CompilationMapper.toCompilationDto(compilation, statsClient);
    }

    @Override
    @Transactional
    public void delete(Long compId) {
        getCompilation(compId, compilationRepository);
        log.info("DELETE /admin/compilations/{}", compId);
        compilationRepository.deleteById(compId);
    }

    @Override
    @Transactional
    public CompilationDto update(Long compId, UpdateCompilationRequestDto compilationDto) {
        Compilation compilation = getCompilation(compId, compilationRepository);
        if (compilationDto.getEvents() != null) {
            compilation.setEvents(eventRepository.findAllByIdIn(compilationDto.getEvents()));
        }
        if (compilationDto.getPinned() != null) {
            compilation.setPinned(compilationDto.getPinned());
        }
        if (compilationDto.getTitle() != null) {
            compilation.setTitle(compilationDto.getTitle());
        }
        compilationRepository.save(compilation);
        log.info("PATCH /admin/compilations/{}", compId);
        return CompilationMapper.toCompilationDto(compilation, statsClient);
    }
}
