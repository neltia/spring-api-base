package neltia.bloguide.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TodoUpdateRequestDto {
    String task;

    String priority;

    Boolean done;
}
