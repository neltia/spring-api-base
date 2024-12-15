package neltia.bloguide.api.service;

import com.google.gson.*;
import lombok.RequiredArgsConstructor;
import neltia.bloguide.api.dto.UserDeleteRequest;
import neltia.bloguide.api.dto.UserDetailResponse;
import neltia.bloguide.api.dto.UserSaveRequestDto;
import neltia.bloguide.api.dto.UserUpdateRequestDto;
import neltia.bloguide.api.domain.User;
import neltia.bloguide.api.share.ResponseCodeEnum;
import neltia.bloguide.api.share.ResponseResult;
import neltia.bloguide.api.repository.UserRepository;
import neltia.bloguide.api.infrastructure.utils.GsonUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public ResponseResult getUserList() {
        ResponseResult result = new ResponseResult(0);
        JsonArray data = new JsonArray();

        List<User> userList = userRepository.findAll();
        for (User user : userList) {
            String json = GsonUtils.toJson(user);
            if (json == null) {
                continue;
            }
            JsonObject obj = (JsonObject) JsonParser.parseString(json);
            data.add(obj);
        }

        result.setResultCode(ResponseCodeEnum.OK.getCode());
        result.setData(data);
        return result;
    }

    @Transactional(readOnly = true)
    public ResponseResult getUserDetail(Long idx) {
        ResponseResult result = new ResponseResult(0);

        User userInfo = userRepository.findUserByIdx(idx);
        if (userInfo == null) {
            result.setResultCode(ResponseCodeEnum.NO_DATA.getCode());
            String errMsg = "존재하지 않는 사용자입니다.";
            result.setErrorMsg(errMsg);
            return result;
        }
        UserDetailResponse userDetailResponse = new UserDetailResponse(userInfo);
        String json = GsonUtils.toJson(userDetailResponse);
        if (json == null) {
            result.setResultCode(ResponseCodeEnum.NO_DATA.getCode());
            String errMsg = "존재하지 않는 사용자입니다.";
            result.setErrorMsg(errMsg);
            return result;
        }
        JsonObject obj = (JsonObject) JsonParser.parseString(json);

        result.setResultCode(ResponseCodeEnum.OK.getCode());
        result.setData(obj);
        return result;
    }

    @Transactional
    public ResponseResult deleteUser(UserDeleteRequest userDeleteRequest) {
        ResponseResult result = new ResponseResult(0);

        // 사용자 정보 확인
        String userId = userDeleteRequest.getUserId();
        String userPw = userDeleteRequest.getUserPw();
        if (userId == null || userId.isEmpty()) {
            result.setResultCode(ResponseCodeEnum.INVALID_PARAMETER.getCode());
            return result;
        }
        User userInfo = userRepository.findUserByUserId(userId);
        if (userInfo == null) {
            result.setResultCode(ResponseCodeEnum.NO_DATA.getCode());
            String errMsg = "존재하지 않는 사용자입니다.";
            result.setErrorMsg(errMsg);
            return result;
        }
        if (!Objects.equals(userInfo.getUserPw(), userPw)) {
            result.setResultCode(ResponseCodeEnum.ACCOUNT_NOT_FOUND.getCode());
            String errMsg = "아이디 혹은 비밀번호가 유효하지 않습니다.";
            result.setErrorMsg(errMsg);
            return result;
        }

        // 사용자 정보 삭제
        // userRepository.delete(userInfo);

        // 사용자 정보 비활성화
        userInfo.setIsUse(0);
        userRepository.save(userInfo);

        // 결과 반환
        result.setResultCode(ResponseCodeEnum.OK.getCode());
        String msg = "서비스에서 탈퇴하였습니다.";
        result.setResultMsg(msg);
        return result;
    }

    @Transactional
    public ResponseResult saveUser(UserSaveRequestDto userSaveRequestDto) {
        ResponseResult result = new ResponseResult(0);

        // 이미 있는 사용자인지 확인
        String userId = userSaveRequestDto.getUserId();
        User userInfo = userRepository.findUserByUserId(userId);
        if (userInfo != null) {
            result.setResultCode(ResponseCodeEnum.DATA_EXISTS.getCode());
            String msg = "이미 있는 사용자입니다.";
            result.setResultMsg(msg);
            return result;
        }

        // 받은 사용자 데이터 저장
        String userPw = userSaveRequestDto.getUserPw();
        String userName = userSaveRequestDto.getUserName();
        String userEmail = userSaveRequestDto.getUserEmail();
        User user = User.builder()
                .userId(userId)
                .userPw(userPw)
                .userName(userName)
                .userEmail(userEmail)
                .supervisor(0)
                .isUse(1)
                .userBeforePw(userPw)
                .build();
        User save = userRepository.save(user);
        System.out.println("save: " + save.getUserId());

        result.setResultCode(ResponseCodeEnum.DATA_EXISTS.getCode());
        String msg = "신규 사용자가 등록되었습니다.";
        result.setResultMsg(msg);
        return result;
    }

    @Transactional
    public ResponseResult updateUser(UserUpdateRequestDto userUpdateRequestDto) {
        ResponseResult result = new ResponseResult(0);

        String userId = userUpdateRequestDto.getUserId();
        String userPw = userUpdateRequestDto.getUserPw();
        String userChangePw = userUpdateRequestDto.getUserChangePw();
        String userName = userUpdateRequestDto.getUserName();
        String userEmail = userUpdateRequestDto.getUserEmail();

        // param check
        if (userId == null || userId.isEmpty()) {
            result.setResultCode(ResponseCodeEnum.INVALID_PARAMETER.getCode());
            return result;
        }

        // 이미 있는 사용자인지 확인
        User userInfo = userRepository.findUserByUserId(userId);
        if (userInfo == null) {
            result.setResultCode(ResponseCodeEnum.NO_DATA.getCode());
            String errMsg = "존재하지 않는 사용자입니다.";
            result.setErrorMsg(errMsg);
            return result;
        }

        // 인증 정보(비밀번호)가 일치하는지 확인
        if (!userPw.equals(userInfo.getUserPw())) {
            result.setResultCode(ResponseCodeEnum.ACCOUNT_NOT_FOUND.getCode());
            String errMsg = "아이디 혹은 비밀번호가 유효하지 않습니다.";
            result.setErrorMsg(errMsg);
            return result;
        }

        // 받은 사용자 데이터로 기존 데이터 갱신
        userInfo.setUserName(userName);
        userInfo.setUserEmail(userEmail);
        // - userChangePw 데이터가 있다면 기존 비밀번호의 변경 요청이 있다고 가정
        if (userUpdateRequestDto.getUserChangePw() != null) {
            if (Objects.equals(userChangePw, userInfo.getUserPw())) {
                result.setResultCode(ResponseCodeEnum.INVALID_PARAMETER.getCode());
                String errMsg = "기존 비밀번호와 같은 비밀번호로 변경할 수 없습니다.";
                result.setErrorMsg(errMsg);
                return result;
            }

            userInfo.setUserPw(userChangePw);
            userInfo.setPwdChangeTime(LocalDateTime.now());
        }

        User save = userRepository.save(userInfo);
        System.out.println("update: " + save.getUserId());

        result.setResultCode(ResponseCodeEnum.ACCOUNT_NOT_FOUND.getCode());
        String msg = "입력한 정보로 갱신되었습니다.";
        result.setResultMsg(msg);
        return result;
    }
}
