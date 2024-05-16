package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.DuplicateException;
import ru.practicum.user.dto.NewUserRequestDto;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.utility.PageUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.practicum.utility.ValidationUtils.getUser;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        Pageable pageable = PageUtils.getPageable(from, size);
        List<User> users = ids != null ? userRepository.findAllByIdIn(ids, pageable) :
                userRepository.findAll(pageable).toList();
        log.info("GET /admin/users");
        return users.stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDto add(NewUserRequestDto userDto) {
        User user = UserMapper.toUser(userDto);
        validateUniqueEmail(user);
        log.info("POST /admin/users");
        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        getUser(userId, userRepository);
        log.info("DELETE /admin/users/{}", userId);
        userRepository.deleteById(userId);
    }

    private void validateUniqueEmail(User user) {
        Set<String> emails = userRepository.findAll().stream().map(User::getEmail).collect(Collectors.toSet());
        if (emails.contains(user.getEmail())) {
            log.warn("Пользователь с таким email {} уже существует", user.getEmail());
            throw new DuplicateException("User with the same email already exists");
        }
    }
}
