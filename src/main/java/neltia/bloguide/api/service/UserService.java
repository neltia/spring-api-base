package neltia.bloguide.api.service;

import com.google.gson.*;
import lombok.RequiredArgsConstructor;
import neltia.bloguide.api.domain.User;
import neltia.bloguide.api.repository.UserRepository;
import neltia.bloguide.api.utils.GsonUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
}
