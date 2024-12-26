package neltia.bloguide.api.domain;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

// @Document(indexName = "todo_index")
@Getter
@Setter
public class Todo {
    private String task;

    private String priority;

    private Boolean done;

    private String created_at;

    private String updated_at;
}
