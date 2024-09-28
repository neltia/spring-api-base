package neltia.bloguide.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TodoSearchListRequest {
    private Integer priority;

    private String keyword;

    private Boolean done;
}
