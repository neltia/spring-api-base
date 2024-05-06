package neltia.bloguide.api.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class UserDeleteRequest {
    @NotNull
    String userId;

    @NotNull
    String userPw;
}
