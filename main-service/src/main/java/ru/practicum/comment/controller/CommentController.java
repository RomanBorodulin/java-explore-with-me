package ru.practicum.comment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;
import ru.practicum.comment.service.CommentService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/users/{userId}/events/{eventId}/comment")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto add(@PathVariable Long userId, @PathVariable Long eventId,
                          @Valid @RequestBody NewCommentDto commentDto) {
        return commentService.add(userId, eventId, commentDto);
    }

    @PatchMapping("/users/{userId}/comments/{comId}")
    public CommentDto update(@PathVariable Long userId, @PathVariable Long comId,
                             @Valid @RequestBody UpdateCommentDto commentDto) {
        return commentService.update(userId, comId, commentDto);
    }

    @DeleteMapping("/users/{userId}/comments/{comId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByUser(@PathVariable Long userId, @PathVariable Long comId) {
        commentService.deleteByUser(userId, comId);
    }

    @GetMapping("/admin/comments")
    public List<CommentDto> getCommentsByAdmin(@RequestParam(required = false) List<Long> users,
                                               @RequestParam(required = false) List<Long> events,
                                               @RequestParam(required = false)
                                               @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                               @RequestParam(required = false)
                                               @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                               @RequestParam(defaultValue = "0") int from,
                                               @RequestParam(defaultValue = "10") int size) {
        return commentService.getCommentsByAdmin(users, events, rangeStart, rangeEnd, from, size);
    }

    @DeleteMapping("/admin/comments/{comId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByAdmin(@PathVariable Long comId) {
        commentService.deleteByAdmin(comId);
    }

}
