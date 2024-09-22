package neltia.bloguide.api.dto;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
public class TodoGetItemListRequest {
    @Nullable
    private String keyName;

    @NotNull
    private List<String> idList;
}
