package neltia.bloguide.api.controller;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import neltia.bloguide.api.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @GetMapping("list")
    public ResponseEntity<?> getUserList() {
        JsonObject userList = userService.getUserList();
        return ResponseEntity.ok().body(userList);
    }

    @GetMapping("{idx}")
    public ResponseEntity<?> getUserDetail(@PathVariable("idx") int idx) {
        JsonObject userDetail = userService.getUserDetail(idx);
        return ResponseEntity.ok().body(userDetail);
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveUserInfo(@RequestBody JsonObject userSaveRequest) {
        JsonObject result = userService.saveUser(userSaveRequest);
        return ResponseEntity.ok().body(result);
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateUserInfo(@RequestBody JsonObject userUpdateRequest) {
        JsonObject result = userService.updateUser(userUpdateRequest);
        return ResponseEntity.ok().body(result);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(@RequestBody JsonObject userDeleteRequest) {
        JsonObject result = userService.deleteUser(userDeleteRequest);
        return ResponseEntity.ok().body(result);
    }
}
