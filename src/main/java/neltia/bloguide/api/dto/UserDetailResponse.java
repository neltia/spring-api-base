package neltia.bloguide.api.dto;

import lombok.Getter;
import neltia.bloguide.api.domain.User;

@Getter
public class UserDetailResponse {
    String userId;

    String userName;

    String userEmail;

    public UserDetailResponse(User user) {
        this.userId = user.getUserId();
        this.userName = user.getUserName();
        this.userEmail = user.getUserEmail();
    }
}
