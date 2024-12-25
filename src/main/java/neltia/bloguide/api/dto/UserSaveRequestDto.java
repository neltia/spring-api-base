package neltia.bloguide.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
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
