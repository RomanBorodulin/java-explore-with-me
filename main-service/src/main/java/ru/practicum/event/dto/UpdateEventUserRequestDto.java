package ru.practicum.event.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.event.model.UserStateAction;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@ToString
public class UpdateEventUserRequestDto extends UpdateEventDto {
    private UserStateAction stateAction;

    public UpdateEventUserRequestDto() {
        super();
    }
}
