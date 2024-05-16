package ru.practicum.event.service;

import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventRequestStatusUpdateRequestDto;
import ru.practicum.event.dto.EventRequestStatusUpdateResultDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventAdminRequestDto;
import ru.practicum.event.dto.UpdateEventUserRequestDto;
import ru.practicum.event.model.EventSort;
import ru.practicum.event.model.EventState;
import ru.practicum.request.dto.ParticipationRequestDto;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    List<EventShortDto> getEventsByUserId(Long userId, int from, int size);

    EventFullDto add(Long userId, NewEventDto eventDto);

    EventFullDto getUserEventByEventId(Long userId, Long eventId);

    EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequestDto eventDto);

    List<ParticipationRequestDto> getRequestsByEventId(Long userId, Long eventId);

    EventRequestStatusUpdateResultDto updateStatusRequests(Long userId, Long eventId,
                                                           EventRequestStatusUpdateRequestDto statusUpdateDto);

    List<EventFullDto> getEventsByAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequestDto eventDto);

    List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                        LocalDateTime rangeEnd, Boolean onlyAvailable, EventSort sort, int from, int size,
                                        HttpServletRequest request);

    EventFullDto getPublicEventById(Long id, HttpServletRequest request);
}
