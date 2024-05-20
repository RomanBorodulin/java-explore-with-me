package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventRequestStatusUpdateRequestDto;
import ru.practicum.event.dto.EventRequestStatusUpdateResultDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventAdminRequestDto;
import ru.practicum.event.dto.UpdateEventDto;
import ru.practicum.event.dto.UpdateEventUserRequestDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.mapper.LocationMapper;
import ru.practicum.event.model.AdminStateAction;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventSort;
import ru.practicum.event.model.EventState;
import ru.practicum.event.model.UpdateRequestStatus;
import ru.practicum.event.model.UserStateAction;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConditionsAreNotMetException;
import ru.practicum.exception.DataNotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.utility.PageUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.utility.StatsUtils.getHits;
import static ru.practicum.utility.ValidationUtils.getCategory;
import static ru.practicum.utility.ValidationUtils.getEvent;
import static ru.practicum.utility.ValidationUtils.getUser;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final StatsClient statsClient;

    @Override
    public List<EventShortDto> getEventsByUserId(Long userId, int from, int size) {
        Pageable pageable = PageUtils.getPageable(from, size);
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);
        log.info("GET /users/{}/events", userId);
        return events.stream().map(e -> EventMapper.toEventShortDto(e, getHits(e.getId(), statsClient)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto add(Long userId, NewEventDto eventDto) {
        if (eventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Field: eventDate. " +
                    "Error: должно содержать дату, которая еще не наступила. Value: " + eventDto.getEventDate());
        }
        Event event = EventMapper.toEvent(eventDto,
                getCategory(eventDto.getCategory(), categoryRepository), getUser(userId, userRepository), null);
        event.setConfirmedRequests(0L);
        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());
        log.info("POST /users/{}/events", userId);
        return EventMapper.toEventFullDto(eventRepository.save(event), 0L);
    }

    @Override
    public EventFullDto getUserEventByEventId(Long userId, Long eventId) {
        getUser(userId, userRepository);
        Event event = getEvent(eventId, eventRepository);
        log.info("GET /users/{}/events/{}", userId, eventId);
        return EventMapper.toEventFullDto(event, getHits(eventId, statsClient));
    }

    @Override
    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequestDto eventDto) {
        getUser(userId, userRepository);
        Event event = getEvent(eventId, eventRepository);
        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ConditionsAreNotMetException("Only pending or canceled events can be changed");
        }
        updateEvent(eventDto, event, LocalDateTime.now().plusHours(2));
        if (eventDto.getStateAction() != null) {
            if (eventDto.getStateAction().equals(UserStateAction.SEND_TO_REVIEW)) {
                event.setState(EventState.PENDING);
            } else {
                event.setState(EventState.CANCELED);
            }
        }
        log.info("PATCH /users/{}/events/{}", userId, eventId);
        return EventMapper.toEventFullDto(eventRepository.save(event), 0L);
    }


    @Override
    public List<ParticipationRequestDto> getRequestsByEventId(Long userId, Long eventId) {
        getUser(userId, userRepository);
        getEvent(eventId, eventRepository);
        log.info("GET /users/{}/events/{}/requests", userId, eventId);
        return requestRepository.findAllByEventId(eventId).stream()
                .map(RequestMapper::toRequestDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResultDto updateStatusRequests(Long userId, Long eventId,
                                                                  EventRequestStatusUpdateRequestDto statusUpdateDto) {
        getUser(userId, userRepository);
        Event event = getEvent(eventId, eventRepository);
        List<ParticipationRequest> requests = requestRepository.findByIdIn(statusUpdateDto.getRequestIds());
        requests.stream().filter(request -> !request.getStatus().equals(RequestStatus.PENDING)).forEach(request -> {
            throw new ConditionsAreNotMetException("Request must have status PENDING");
        });
        if (statusUpdateDto.getStatus().equals(UpdateRequestStatus.REJECTED)) {
            requestRepository.saveAll(requests.stream().peek(request -> request.setStatus(RequestStatus.REJECTED))
                    .collect(Collectors.toList()));
        } else {
            if (Objects.equals(event.getConfirmedRequests(), Long.valueOf(event.getParticipantLimit()))) {
                throw new ConditionsAreNotMetException("The participant limit has been reached");
            }
            event.setConfirmedRequests(event.getConfirmedRequests() + statusUpdateDto.getRequestIds().size());
            event = eventRepository.save(event);
            requestRepository.saveAll(requests.stream().peek(request -> request.setStatus(RequestStatus.CONFIRMED))
                    .collect(Collectors.toList()));
            if (event.getConfirmedRequests() + statusUpdateDto.getRequestIds().size() > event.getParticipantLimit()) {
                requestRepository.saveAll(requests.stream().peek(request -> request.setStatus(RequestStatus.REJECTED))
                        .collect(Collectors.toList()));
            }
        }
        List<ParticipationRequest> resultRequests = requestRepository.findAllByEventId(eventId);
        log.info("PATCH /users/{}/events/{}/requests", userId, eventId);
        return EventRequestStatusUpdateResultDto.builder()
                .confirmedRequests(resultRequests.stream()
                        .filter(request -> request.getStatus().equals(RequestStatus.CONFIRMED))
                        .map(RequestMapper::toRequestDto).collect(Collectors.toList()))
                .rejectedRequests(resultRequests.stream()
                        .filter(request -> request.getStatus().equals(RequestStatus.REJECTED))
                        .map(RequestMapper::toRequestDto).collect(Collectors.toList()))
                .build();
    }

    @Override
    public List<EventFullDto> getEventsByAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        if (rangeStart != null && rangeEnd != null) {
            validateDate(rangeStart, rangeEnd);
        }
        Pageable pageable = PageUtils.getPageable(from, size);
        log.info("users={},states={},categories={},rangeStart={},rangeEnd={}", users, states, categories, rangeStart, rangeEnd);
        log.info("GET /admin/events");
        return eventRepository.findEventsByAdmin(users, states, categories, rangeStart, rangeEnd, pageable).stream()
                .map(e -> EventMapper.toEventFullDto(e, getHits(e.getId(), statsClient))).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequestDto eventDto) {
        Event event = getEvent(eventId, eventRepository);
        if (eventDto.getStateAction() != null) {
            if (eventDto.getStateAction().equals(AdminStateAction.PUBLISH_EVENT)) {
                if (!event.getState().equals(EventState.PENDING)) {
                    throw new ConditionsAreNotMetException("Cannot publish the event because " +
                            "it's not in the right state: " + event.getState());
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else {
                if (event.getState().equals(EventState.PUBLISHED)) {
                    throw new ConditionsAreNotMetException("Cannot cancel the event because " +
                            "it's not in the right state: " + event.getState());
                }
                event.setState(EventState.CANCELED);
            }
        }
        updateEvent(eventDto, event, LocalDateTime.now().plusHours(1));
        log.info("PATCH /admin/events/{}", eventId);
        return EventMapper.toEventFullDto(eventRepository.save(event), 0L);
    }

    @Override
    @Transactional
    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable,
                                               EventSort sort, int from, int size, HttpServletRequest request) {
        if (rangeStart != null && rangeEnd != null) {
            validateDate(rangeStart, rangeEnd);
        }
        statsClient.save(getHitDto(request));
        Pageable pageable;
        if (sort == null) {
            pageable = PageUtils.getPageable(from, size, Sort.by("id"));
            return getEventShortDtos(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, pageable);
        }
        if (sort.equals(EventSort.EVENT_DATE)) {
            pageable = PageUtils.getPageable(from, size, Sort.by("eventDate"));
            return getEventShortDtos(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, pageable);
        }
        pageable = PageUtils.getPageable(from, size);
        log.info("GET /events");
        return getEventShortDtos(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, pageable).stream()
                .sorted(Comparator.comparing(EventShortDto::getViews)).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto getPublicEventById(Long id, HttpServletRequest request) {
        statsClient.save(getHitDto(request));
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Event with id=" + id + " was not found"));
        if (!event.getState().equals(EventState.PUBLISHED))
            throw new DataNotFoundException("Event " + id + " is not published yet!");
        log.info("GET /events/{}", id);
        return EventMapper.toEventFullDto(event, getHits(id, statsClient));
    }

    private List<EventShortDto> getEventShortDtos(String text, List<Long> categories, Boolean paid,
                                                  LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                  Boolean onlyAvailable, Pageable pageable) {
        List<Event> events = eventRepository.findPublicEvents(text, categories, paid,
                rangeStart, rangeEnd, onlyAvailable, EventState.PUBLISHED, pageable);
        return events.stream()
                .map(e -> EventMapper.toEventShortDto(e, getHits(e.getId(), statsClient))).collect(Collectors.toList());
    }

    private EndpointHitDto getHitDto(HttpServletRequest request) {
        return EndpointHitDto.builder()
                .app("ewm-main-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build();
    }

    private void updateEvent(UpdateEventDto eventDto, Event event, LocalDateTime nowPlus) {
        if (eventDto.getEventDate() != null) {
            if (eventDto.getEventDate().isBefore(nowPlus)) {
                throw new ValidationException("Field: eventDate. " +
                        "Error: должно содержать дату, которая еще не наступила. Value: " + eventDto.getEventDate());
            } else {
                event.setEventDate(eventDto.getEventDate());
            }
        }
        if (eventDto.getAnnotation() != null) {
            event.setAnnotation(eventDto.getAnnotation());
        }
        if (eventDto.getCategory() != null) {
            event.setCategory(getCategory(eventDto.getCategory(), categoryRepository));
        }
        if (eventDto.getDescription() != null) {
            event.setDescription(eventDto.getDescription());
        }
        if (eventDto.getLocation() != null) {
            event.setLocation(LocationMapper.toLocation(eventDto.getLocation()));
        }
        if (eventDto.getPaid() != null) {
            event.setPaid(eventDto.getPaid());
        }
        if (eventDto.getParticipantLimit() != null) {
            event.setParticipantLimit(eventDto.getParticipantLimit());
        }
        if (eventDto.getRequestModeration() != null) {
            event.setRequestModeration(eventDto.getRequestModeration());
        }
        if (eventDto.getTitle() != null) {
            event.setTitle(eventDto.getTitle());
        }
    }

    private void validateDate(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end) || start.isEqual(end)) {
            throw new ValidationException("Дата окончания не может быть раньше или равна дате начала");
        }
    }
}
