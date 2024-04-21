package neltia.bloguide.api.service;

import com.google.gson.*;
import lombok.RequiredArgsConstructor;
import neltia.bloguide.api.domain.User;
import neltia.bloguide.api.repository.UserRepository;
import neltia.bloguide.api.utils.GsonUtils;
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
    public JsonObject getUserList() {
        JsonObject jsonObject = new JsonObject();
        JsonArray data = new JsonArray();

        List<User> userList = userRepository.findAll();
        for (User user : userList) {
            String json = GsonUtils.toJson(user);
            JsonObject obj = (JsonObject) JsonParser.parseString(json);
            data.add(obj);
        }
        jsonObject.add("listData", data);
        return jsonObject;
    }

    @Transactional(readOnly = true)
    public JsonObject getUserDetail(int idx) {
        JsonObject jsonObject = new JsonObject();

        User userInfo = userRepository.findUserByIdx(idx);
        String json = GsonUtils.toJson(userInfo);
        JsonObject obj = (JsonObject) JsonParser.parseString(json);

        jsonObject.add("userData", obj);
        return jsonObject;
    }

    @Transactional
    public JsonObject deleteUser(JsonObject userDeleteRequest) {
        JsonObject jsonObject = new JsonObject();

        // 사용자 정보 확인
        String userId = userDeleteRequest.get("userId").getAsString();
        String userPw = userDeleteRequest.get("userPw").getAsString();
        User userInfo = userRepository.findUserByUserId(userId);
        if (userInfo == null) {
            jsonObject.addProperty("msg", "존재하지 않는 사용자입니다.");
            return jsonObject;
        }
        if (!Objects.equals(userInfo.getUserPw(), userPw)) {
            jsonObject.addProperty("msg", "아이디 혹은 비밀번호가 유효하지 않습니다.");
            return jsonObject;
        }

        // 사용자 정보 삭제
        // userRepository.delete(userInfo);

        // 사용자 정보 비활성화
        userInfo.setIsUse(0);
        userRepository.save(userInfo);

        // 결과 반환
        jsonObject.addProperty("msg", "서비스에서 탈퇴하였습니다.");
        return jsonObject;
    }

    @Transactional
    public JsonObject saveUser(JsonObject userInfoRequest) {
        JsonObject jsonObject = new JsonObject();

        // 필수 값이 없는 경우: 개별 파라미터 전체 확인은 DTO 적용시 반영
        // 대표 값 하나를 제외한 나머지 값은 있다고 가정
        if (!userInfoRequest.has("userId")) {
            jsonObject.addProperty("msg", "이미 있는 사용자입니다.");
            return jsonObject;
        }

        // 이미 있는 사용자인지 확인
        String userId = userInfoRequest.get("userId").getAsString();
        User userInfo = userRepository.findUserByUserId(userId);
        if (userInfo != null) {
            jsonObject.addProperty("msg", "이미 있는 사용자입니다.");
            return jsonObject;
        }

        // 받은 사용자 데이터 저장
        String userPw = userInfoRequest.get("userPw").getAsString();
        String userName = userInfoRequest.get("userName").getAsString();
        String userEmail = userInfoRequest.get("userEmail").getAsString();
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

        jsonObject.addProperty("msg", "신규 사용자가 등록되었습니다.");

        return jsonObject;
    }

    @Transactional
    public JsonObject updateUser(JsonObject userUpdateRequest) {
        JsonObject jsonObject = new JsonObject();

        // 필수 값이 없는 경우: 개별 파라미터 전체 확인은 DTO 적용시 반영
        // 대표 값 하나를 제외한 나머지 값은 있다고 가정
        if (!userUpdateRequest.has("userId")) {
            jsonObject.addProperty("msg", "이미 있는 사용자입니다.");
            return jsonObject;
        }
        String userId = userUpdateRequest.get("userId").getAsString();
        String userPw = userUpdateRequest.get("userPw").getAsString();
        String userChangePw = userUpdateRequest.get("userChangePw").getAsString();
        String userName = userUpdateRequest.get("userName").getAsString();
        String userEmail = userUpdateRequest.get("userEmail").getAsString();

        // 이미 있는 사용자인지 확인
        User userInfo = userRepository.findUserByUserId(userId);
        if (userInfo == null) {
            jsonObject.addProperty("msg", "존재하지 않는 사용자입니다.");
            return jsonObject;
        }

        // 인증 정보(비밀번호)가 일치하는지 확인
        if (!userPw.equals(userInfo.getUserPw())) {
            jsonObject.addProperty("msg", "아이디 혹은 비밀번호가 유효하지 않습니다.");
            return jsonObject;
        }

        // 받은 사용자 데이터로 기존 데이터 갱신
        userInfo.setUserName(userName);
        userInfo.setUserEmail(userEmail);
        // - userChangePw 데이터가 있다면 기존 비밀번호의 변경 요청이 있다고 가정
        if (userUpdateRequest.has("userChangePw")) {
            if (Objects.equals(userChangePw, userInfo.getUserPw())) {
                jsonObject.addProperty("msg", "기존 비밀번호와 같은 비밀번호로 변경할 수 없습니다.");
                return jsonObject;
            }

            userInfo.setUserPw(userChangePw);
            userInfo.setPwdChangeTime(LocalDateTime.now());
        }

        User save = userRepository.save(userInfo);
        System.out.println("update: " + save.getUserId());

        jsonObject.addProperty("msg", "입력한 정보로 갱신되었습니다.");

        return jsonObject;
    }
}
