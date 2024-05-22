package ru.practicum.utility;

import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.DataNotFoundException;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

public final class ValidationUtils {
    private ValidationUtils() {
    }

    public static User getUser(Long userId, UserRepository userRepository) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User with id=" + userId + " was not found"));
    }

    public static Category getCategory(Long catId, CategoryRepository categoryRepository) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new DataNotFoundException("Category with id=" + catId + " was not found"));
    }

    public static Event getEvent(Long eventId, EventRepository eventRepository) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new DataNotFoundException("Event with id=" + eventId + " was not found"));
    }

    public static ParticipationRequest getRequest(Long requestId, RequestRepository requestRepository) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new DataNotFoundException("Request with id=" + requestId + " was not found"));
    }

    public static Compilation getCompilation(Long compId, CompilationRepository compilationRepository) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new DataNotFoundException("Compilation with id=" + compId + " was not found"));
    }

    public static Comment getComment(Long comId, CommentRepository commentRepository) {
        return commentRepository.findById(comId)
                .orElseThrow(() -> new DataNotFoundException("Comment with id=" + comId + " was not found"));
    }
}
