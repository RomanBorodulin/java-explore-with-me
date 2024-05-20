package ru.practicum.comment.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.comment.model.Comment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Optional<List<Comment>> findAllByEventId(Long id);

    @Query("SELECT c " +
            "FROM Comment c " +
            "WHERE (:users IS NULL OR c.author.id IN :users) " +
            "AND (:events IS NULL OR c.event.id IN :events) " +
            "AND (CAST(:rangeStart AS date) IS NULL OR CAST(:rangeStart AS date) < c.created) " +
            "AND (CAST(:rangeEnd AS date) IS NULL OR CAST(:rangeEnd AS date) > c.created)")
    List<Comment> findCommentsByAdmin(
            @Param("users") List<Long> users,
            @Param("events") List<Long> events,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable);
}
