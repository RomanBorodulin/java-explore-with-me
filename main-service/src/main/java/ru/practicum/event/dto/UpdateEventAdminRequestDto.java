package ru.practicum.event.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.event.model.AdminStateAction;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@ToString
public class UpdateEventAdminRequestDto extends UpdateEventDto {
    private AdminStateAction stateAction;

    public UpdateEventAdminRequestDto() {
        super();
    }
}
