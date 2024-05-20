package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.request.model.ParticipationRequest;

import java.util.List;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {
    List<ParticipationRequest> findAllByRequesterId(Long userId);

    List<ParticipationRequest> findAllByRequesterIdAndEventId(Long userId, Long eventId);

    List<ParticipationRequest> findByIdIn(List<Long> requestIds);

    List<ParticipationRequest> findAllByEventId(Long eventId);
}
