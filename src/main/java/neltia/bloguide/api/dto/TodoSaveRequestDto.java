package neltia.bloguide.api.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class TodoSaveRequestDto {
    @NotNull
    String task;

    @NotNull
    String priority;
}
