package neltia.bloguide.api.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Todo {
    private String task;

    private String priority;

    private Boolean done;

    private String created_at;

    private String updated_at;
}
