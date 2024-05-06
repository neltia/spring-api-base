package neltia.bloguide.api.controller;

import lombok.RequiredArgsConstructor;
import neltia.bloguide.api.dto.UserDeleteRequest;
import neltia.bloguide.api.dto.UserSaveRequestDto;
import neltia.bloguide.api.dto.UserUpdateRequestDto;
import neltia.bloguide.api.share.ResponseResult;
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
        ResponseResult result;
        result = userService.getUserList();
        return ResponseEntity.ok().body(result.getResponseResult());
    }

    @GetMapping("{idx}")
    public ResponseEntity<?> getUserDetail(@PathVariable("idx") int idx) {
        ResponseResult result;
        result = userService.getUserDetail(idx);
        return ResponseEntity.ok().body(result.getResponseResult());
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveUserInfo(@Valid @RequestBody UserSaveRequestDto userSaveRequestDto) {
        ResponseResult result;
        result = userService.saveUser(userSaveRequestDto);
        return ResponseEntity.ok().body(result.getResponseResult());
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateUserInfo(@Valid @RequestBody UserUpdateRequestDto userUpdateRequestDto) {
        ResponseResult result;
        result = userService.updateUser(userUpdateRequestDto);
        return ResponseEntity.ok().body(result.getResponseResult());
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(@RequestBody UserDeleteRequest userDeleteRequest) {
        ResponseResult result;
        result = userService.deleteUser(userDeleteRequest);
        return ResponseEntity.ok().body(result.getResponseResult());
    }
}
