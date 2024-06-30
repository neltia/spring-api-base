package neltia.bloguide.api.dto;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class TodoUpdateRequestDto {
    String task;

    String priority;

    Boolean done;
}
