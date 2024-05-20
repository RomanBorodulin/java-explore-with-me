package ru.practicum.utility;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.exception.ValidationException;

@UtilityClass
public class PageUtils {
    public Pageable getPageable(int from, int size, Sort sort) {
        if (from < 0) {
            throw new ValidationException("Negative index of the first element was introduced");
        }
        if (size <= 0) {
            throw new ValidationException("The number of elements to display cannot be less than or equal to zero");
        }
        return sort != null ? PageRequest.of(from / size, size, sort)
                : PageRequest.of(from / size, size);
    }

    public Pageable getPageable(int from, int size) {
        return getPageable(from, size, null);
    }
}
