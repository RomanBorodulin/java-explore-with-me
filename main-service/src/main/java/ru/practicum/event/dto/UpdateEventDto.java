package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class UpdateEventDto {
    @Size(min = 20, max = 2000)
    protected String annotation;
    protected Long category;
    @Size(min = 20, max = 7000)
    protected String description;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    protected LocalDateTime eventDate;
    protected LocationDto location;
    protected Boolean paid;
    @PositiveOrZero
    protected Integer participantLimit;
    protected Boolean requestModeration;
    @Size(min = 3, max = 120)
    protected String title;
}
