package neltia.bloguide.api.service;

import com.google.gson.JsonArray;
import neltia.bloguide.api.domain.User;
import neltia.bloguide.api.dto.UserDeleteRequest;
import neltia.bloguide.api.dto.UserSaveRequestDto;
import neltia.bloguide.api.dto.UserUpdateRequestDto;
import neltia.bloguide.api.repository.UserRepository;
import neltia.bloguide.api.share.ResponseCodeEnum;
import neltia.bloguide.api.share.ResponseResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .idx(1L)
                .userId("testUser")
                .userPw("password")
                .userName("Test User")
                .userEmail("test@example.com")
                .supervisor(0)
                .isUse(1)
                .userBeforePw("oldPassword")
                .regTime(LocalDateTime.now())
                .loginTime(LocalDateTime.now())
                .pwdChangeTime(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("getUserList - 사용자 목록 조회 시 OK 응답을 반환한다.")
    void getUserListTest() {
        // given
        // 테스트용 사용자 리스트 준비
        List<User> userList = new ArrayList<>();
        userList.add(testUser);

        when(userRepository.findAll()).thenReturn(userList);

        // when
        ResponseResult result = userService.getUserList();

        // then
        // - 응답 확인
        assertThat(result.getResultCode()).isEqualTo(ResponseCodeEnum.OK.getCode());
        // - null 값이 아닌지 먼저 확인
        assertThat(result.getData()).isNotNull();
        // - JsonArray 자료형으로 캐스팅 후 검증
        JsonArray dataArray = result.getData().getAsJsonObject().get("list").getAsJsonArray();
        assertThat(dataArray).isNotNull();
        assertThat(dataArray.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("getUserDetail - 존재하지 않는 idx 값으로 조회 시 NO_DATA 응답을 반환한다.")
    void getUserDetailNotFoundTest() {
        // given
        when(userRepository.findUserByIdx(999L)).thenReturn(null);
        // when
        ResponseResult result = userService.getUserDetail(999L);
        // then
        assertThat(result.getResultCode()).isEqualTo(ResponseCodeEnum.NO_DATA.getCode());
        assertThat(result.getErrorMsg()).contains("존재하지 않는 사용자입니다.");
    }

    @Test
    @DisplayName("saveUser - 이미 존재하는 userId로 등록 시 DATA_EXISTS 반환")
    void saveUserAlreadyExistsTest() {
        // given
        UserSaveRequestDto dto = new UserSaveRequestDto("testUser", "password", "Test User", "test@example.com");
        when(userRepository.findUserByUserId("testUser")).thenReturn(testUser);

        // when
        ResponseResult result = userService.saveUser(dto);

        // then
        assertThat(result.getResultCode()).isEqualTo(ResponseCodeEnum.DATA_EXISTS.getCode());
        assertThat(result.getResultMsg()).contains("이미 있는 사용자");
    }

    @Test
    @DisplayName("saveUser - 새로운 userId로 등록 시 새로운 사용자 등록 후 DATA_EXISTS 코드 반환 (코드 확인 필요)")
    void saveUserNewUserTest() {
        // given
        UserSaveRequestDto dto = new UserSaveRequestDto("newUser", "pass", "New User", "new@example.com");
        when(userRepository.findUserByUserId("newUser")).thenReturn(null);
        when(userRepository.save(ArgumentMatchers.<User>any())).thenAnswer(invocation -> {
            User u = invocation.getArgument(0, User.class);
            u.setIdx(2L); // 여기서는 엔티티의 PK 타입에 맞춰 Long 또는 int 사용
            return u;
        });

        // when
        ResponseResult result = userService.saveUser(dto);

        // then
        // 일단 원 코드 대로라면 아래와 같이 테스트 진행
        assertThat(result.getResultCode()).isEqualTo(ResponseCodeEnum.DATA_EXISTS.getCode());
        assertThat(result.getResultMsg()).contains("신규 사용자가 등록되었습니다.");
    }

    @Test
    @DisplayName("updateUser - 존재하지 않는 사용자 업데이트 시 NO_DATA 반환")
    void updateUserNotFoundTest() {
        // given
        UserUpdateRequestDto dto = new UserUpdateRequestDto("unknownUser", "pw", null, "New Name", "new@example.com");
        when(userRepository.findUserByUserId("unknownUser")).thenReturn(null);

        // when
        ResponseResult result = userService.updateUser(dto);

        // then
        assertThat(result.getResultCode()).isEqualTo(ResponseCodeEnum.NO_DATA.getCode());
        assertThat(result.getErrorMsg()).contains("존재하지 않는 사용자");
    }

    @Test
    @DisplayName("updateUser - 비밀번호 불일치 시 ACCOUNT_NOT_FOUND 반환")
    void updateUserInvalidPwTest() {
        // given
        UserUpdateRequestDto dto = new UserUpdateRequestDto("testUser", "wrongPw", "newPw", "New Name", "new@example.com");
        when(userRepository.findUserByUserId("testUser")).thenReturn(testUser);

        // when
        ResponseResult result = userService.updateUser(dto);

        // then
        assertThat(result.getResultCode()).isEqualTo(ResponseCodeEnum.ACCOUNT_NOT_FOUND.getCode());
        assertThat(result.getErrorMsg()).contains("아이디 혹은 비밀번호가 유효하지 않습니다.");
    }

    @Test
    @DisplayName("updateUser - 기존 비밀번호와 같은 비밀번호로 변경 시 INVALID_PARAMETER 반환")
    void updateUserSamePwTest() {
        // given
        UserUpdateRequestDto dto = new UserUpdateRequestDto("testUser", "password", "password", "New Name", "new@example.com");
        when(userRepository.findUserByUserId("testUser")).thenReturn(testUser);

        // when
        ResponseResult result = userService.updateUser(dto);

        // then
        assertThat(result.getResultCode()).isEqualTo(ResponseCodeEnum.INVALID_PARAMETER.getCode());
        assertThat(result.getErrorMsg()).contains("기존 비밀번호와 같은 비밀번호로 변경할 수 없습니다.");
    }

    @Test
    @DisplayName("updateUser - 정상적인 업데이트 시 ACCOUNT_NOT_FOUND 코드 반환 (코드 확인 필요)")
    void updateUserSuccessTest() {
        // given
        UserUpdateRequestDto dto = new UserUpdateRequestDto("testUser", "password", "newPw", "New Name", "new@example.com");
        when(userRepository.findUserByUserId("testUser")).thenReturn(testUser);
        when(userRepository.save(testUser)).thenReturn(testUser);

        // when
        ResponseResult result = userService.updateUser(dto);

        // then
        // 주의: 현재 코드 상에서는 정상 업데이트 시 ACCOUNT_NOT_FOUND 코드를 반환하고 있음.
        // 실제로는 OK 같은 코드가 나와야 할 것으로 예상되나, 원코드에 따라 검증.
        assertThat(result.getResultCode()).isEqualTo(ResponseCodeEnum.ACCOUNT_NOT_FOUND.getCode());
        assertThat(result.getResultMsg()).contains("입력한 정보로 갱신되었습니다.");
        assertThat(testUser.getUserPw()).isEqualTo("newPw");
        assertThat(testUser.getUserName()).isEqualTo("New Name");
    }
    @Test
    @DisplayName("deleteUser - 올바른 아이디/비밀번호로 요청 시 isUse=0 으로 비활성화 후 OK 반환")
    void deleteUserTest() {
        // given
        UserDeleteRequest req = new UserDeleteRequest("testUser", "password");
        when(userRepository.findUserByUserId("testUser")).thenReturn(testUser);

        // when
        ResponseResult result = userService.deleteUser(req);

        // then
        assertThat(result.getResultCode()).isEqualTo(ResponseCodeEnum.OK.getCode());
        assertThat(result.getResultMsg()).contains("탈퇴하였습니다.");
        // isUse 값이 0으로 변경되는지 확인할 수 있다면 verify
        assertThat(testUser.getIsUse()).isEqualTo(0);
    }

    @Test
    @DisplayName("deleteUser - 존재하지 않는 사용자 ID로 요청 시 NO_DATA 반환")
    void deleteUserNotFoundTest() {
        // given
        UserDeleteRequest req = new UserDeleteRequest("unknownUser", "password");
        when(userRepository.findUserByUserId("unknownUser")).thenReturn(null);

        // when
        ResponseResult result = userService.deleteUser(req);

        // then
        assertThat(result.getResultCode()).isEqualTo(ResponseCodeEnum.NO_DATA.getCode());
        assertThat(result.getErrorMsg()).contains("존재하지 않는 사용자");
    }

    @Test
    @DisplayName("deleteUser - 비밀번호 불일치 시 ACCOUNT_NOT_FOUND 반환")
    void deleteUserInvalidPwTest() {
        // given
        UserDeleteRequest req = new UserDeleteRequest("testUser", "wrongPassword");
        when(userRepository.findUserByUserId("testUser")).thenReturn(testUser);

        // when
        ResponseResult result = userService.deleteUser(req);

        // then
        assertThat(result.getResultCode()).isEqualTo(ResponseCodeEnum.ACCOUNT_NOT_FOUND.getCode());
        assertThat(result.getErrorMsg()).contains("아이디 혹은 비밀번호가 유효하지 않습니다.");
    }
}