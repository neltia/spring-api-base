package neltia.bloguide.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
public class UserDeleteRequest {
    @NotNull
    String userId;

    @NotNull
    String userPw;
}
