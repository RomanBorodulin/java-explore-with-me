package ru.practicum.comment.service;

import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentService {
    CommentDto add(Long userId, Long eventId, NewCommentDto commentDto);

    CommentDto update(Long userId, Long comId, UpdateCommentDto commentDto);

    void deleteByUser(Long userId, Long comId);

    List<CommentDto> getCommentsByAdmin(List<Long> users, List<Long> events, LocalDateTime rangeStart,
                                        LocalDateTime rangeEnd, int from, int size);

    void deleteByAdmin(Long comId);
}
