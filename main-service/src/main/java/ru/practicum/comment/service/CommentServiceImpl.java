package ru.practicum.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConditionsAreNotMetException;
import ru.practicum.exception.ValidationException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.utility.PageUtils;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.utility.ValidationUtils.getComment;
import static ru.practicum.utility.ValidationUtils.getEvent;
import static ru.practicum.utility.ValidationUtils.getUser;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CommentDto add(Long userId, Long eventId, NewCommentDto commentDto) {
        User user = getUser(userId, userRepository);
        Event event = getEvent(eventId, eventRepository);
        if (event.getState() != EventState.PUBLISHED) {
            throw new ValidationException("Event should be published");
        }
        Comment comment = CommentMapper.toComment(commentDto, user, event);
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public CommentDto update(Long userId, Long comId, UpdateCommentDto commentDto) {
        User user = getUser(userId, userRepository);
        Comment comment = getComment(comId, commentRepository);
        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new ConditionsAreNotMetException("Only the comment author can update it");
        }
        if (comment.getCreated().isBefore(LocalDateTime.now().minusHours(2))) {
            throw new ValidationException("Comment cannot be updated more than two hours after it is posted.");
        }
        comment.setText(commentDto.getText());
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteByUser(Long userId, Long comId) {
        User user = getUser(userId, userRepository);
        Comment comment = getComment(comId, commentRepository);
        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new ConditionsAreNotMetException("Only the comment author can delete it");
        }
        commentRepository.deleteById(comId);
    }

    @Override
    public List<CommentDto> getCommentsByAdmin(List<Long> users, List<Long> events, LocalDateTime rangeStart,
                                               LocalDateTime rangeEnd, int from, int size) {
        if (rangeStart != null && rangeEnd != null) {
            if (rangeStart.isAfter(rangeEnd) || rangeStart.isEqual(rangeEnd)) {
                throw new ValidationException("The end date cannot be earlier than or equal to the start date");
            }
        }
        Pageable pageable = PageUtils.getPageable(from, size);
        return CommentMapper.toCommentDtoList(commentRepository.findCommentsByAdmin(users, events,
                rangeStart, rangeEnd, pageable));
    }

    @Override
    @Transactional
    public void deleteByAdmin(Long comId) {
        getComment(comId, commentRepository);
        commentRepository.deleteById(comId);
    }
}
