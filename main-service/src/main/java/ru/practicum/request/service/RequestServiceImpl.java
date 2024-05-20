package ru.practicum.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConditionsAreNotMetException;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.model.RequestStatus;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.utility.ValidationUtils.getEvent;
import static ru.practicum.utility.ValidationUtils.getRequest;
import static ru.practicum.utility.ValidationUtils.getUser;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public List<ParticipationRequestDto> getRequests(Long userId) {
        getUser(userId, userRepository);
        log.info("GET /{}/requests", userId);
        return requestRepository.findAllByRequesterId(userId).stream().map(RequestMapper::toRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto add(Long userId, Long eventId) {
        User user = getUser(userId, userRepository);
        Event event = getEvent(eventId, eventRepository);
        List<ParticipationRequest> requests = requestRepository.findAllByRequesterIdAndEventId(userId, eventId);
        if (!requests.isEmpty()) {
            throw new ConditionsAreNotMetException("The request has already been added");
        }
        if (Objects.equals(userId, event.getInitiator().getId())) {
            throw new ConditionsAreNotMetException("The event initiator cannot add a request to participate in his event");
        }
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConditionsAreNotMetException("You cannot participate in an unpublished event");
        }
        if (event.getParticipantLimit() != 0 && Objects.equals(Long.valueOf(event.getParticipantLimit()),
                event.getConfirmedRequests())) {
            throw new ConditionsAreNotMetException("Limit of participation requests reached");
        }
        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(user)
                .status(RequestStatus.PENDING)
                .build();
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }
        log.info("POST /{}/requests", userId);
        return RequestMapper.toRequestDto(requestRepository.save(request));
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        getUser(userId, userRepository);
        ParticipationRequest request = getRequest(requestId, requestRepository);
        request.setStatus(RequestStatus.CANCELED);
        log.info("PATCH /{}/requests/{}/cancel", userId, requestId);
        return RequestMapper.toRequestDto(request);
    }
}
