package neltia.bloguide.api.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class UserUpdateRequestDto {
    @NotNull
    String userId;

    @NotNull
    String userPw;

    String userChangePw;

    String userName;

    String userEmail;
}
