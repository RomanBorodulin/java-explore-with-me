package ru.practicum.event.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByInitiatorId(Long userId, Pageable pageable);

    List<Event> findAllByCategoryId(Long catId);

    @Query("SELECT e " +
            "FROM Event e " +
            "WHERE (:users IS NULL OR e.initiator.id IN :users) " +
            "AND (:states IS NULL OR e.state IN :states) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND (CAST(:rangeStart AS date) IS NULL OR CAST(:rangeStart AS date) < e.eventDate) " +
            "AND (CAST(:rangeEnd AS date) IS NULL OR CAST(:rangeEnd AS date) > e.eventDate)")
    List<Event> findEventsByAdmin(
            @Param("users") List<Long> users,
            @Param("states") List<EventState> states,
            @Param("categories") List<Long> categories,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable);

    @Query("SELECT e " +
            "FROM Event as e " +
            "WHERE ((?1 IS NULL) OR " +
            "(lower(e.annotation) LIKE lower(CONCAT('%', ?1, '%') ) OR " +
            "lower(e.description) LIKE lower(CONCAT('%', ?1, '%') )) ) " +
            "AND ((?2) IS NULL OR  e.category.id IN (?2)) " +
            "AND (?3 IS NULL OR e.paid = ?3) " +
            "AND (CAST(?4 AS date) IS NULL OR CAST(?4 AS date)  < e.eventDate) " +
            "AND (CAST(?5 AS date) IS NULL OR CAST(?5 AS date)  > e.eventDate) " +
            "AND (?6 = FALSE OR (?6 = TRUE AND e.participantLimit > (SELECT count(*) FROM ParticipationRequest as r WHERE e.id = r.event.id) ) " +
            "OR (e.participantLimit = 0)) " +
            "AND (e.state IN (?7)) ")
    List<Event> findPublicEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                 LocalDateTime rangeEnd, Boolean onlyAvailable, EventState state, Pageable pageable);

    Set<Event> findAllByIdIn(Set<Long> events);
}
