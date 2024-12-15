package neltia.bloguide.api.domain;


import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "user_tbl")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "user_pw", nullable = false, length = 500)
    private String userPw;

    @Column(name = "user_name", nullable = false, length = 100)
    private String userName;

    @Column(name = "user_email", nullable = false, length = 300)
    private String userEmail;

    @Column(name = "supervisor", nullable = false)
    private int supervisor;

    @Column(name = "is_use", nullable = false)
    private int isUse;

    @Column(name = "user_before_pw", nullable = false, length = 500)
    private String userBeforePw;

    @Column(name = "reg_time", nullable = true)
    private LocalDateTime regTime;

    @Column(name = "login_time", nullable = true)
    private LocalDateTime loginTime;

    @Column(name = "pw_change_time", nullable = true)
    private LocalDateTime pwdChangeTime;
}
