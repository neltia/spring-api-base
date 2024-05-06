package neltia.bloguide.api.controller;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import neltia.bloguide.api.dto.UserSaveRequestDto;
import neltia.bloguide.api.dto.UserUpdateRequestDto;
import neltia.bloguide.api.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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
    public ResponseEntity<?> saveUserInfo(@Valid @RequestBody UserSaveRequestDto userSaveRequestDto) {
        JsonObject result = userService.saveUser(userSaveRequestDto);
        return ResponseEntity.ok().body(result);
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateUserInfo(@Valid @RequestBody UserUpdateRequestDto userUpdateRequestDto) {
        JsonObject result = userService.updateUser(userUpdateRequestDto);
        return ResponseEntity.ok().body(result);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(@RequestBody JsonObject userDeleteRequest) {
        JsonObject result = userService.deleteUser(userDeleteRequest);
        return ResponseEntity.ok().body(result);
    }
}
