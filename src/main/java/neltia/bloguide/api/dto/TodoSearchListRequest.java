package neltia.bloguide.api.dto;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

@Getter
@Setter
public class TodoSearchListRequest {
    @Nullable
    private Integer priority;

    @Nullable
    private String keyword;

    @Nullable
    private Boolean done;
}
