package neltia.bloguide.api.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class UserSaveRequestDto {
    @NotNull
    String userId;

    @NotNull
    String userPw;

    @NotNull
    String userName;

    @NotNull
    String userEmail;
}
