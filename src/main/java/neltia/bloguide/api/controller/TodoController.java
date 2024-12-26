package neltia.bloguide.api.controller;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import neltia.bloguide.api.dto.TodoGetItemListRequest;
import neltia.bloguide.api.dto.TodoSaveRequestDto;
import neltia.bloguide.api.dto.TodoSearchListRequest;
import neltia.bloguide.api.dto.TodoUpdateRequestDto;
import neltia.bloguide.api.service.TodoService;
import neltia.bloguide.api.share.ResponseResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/todo")
public class TodoController {

    private final TodoService todoService;

    // new item insert
    @PostMapping("")
    public ResponseEntity<?> insertTodoItem(@Valid @RequestBody TodoSaveRequestDto todoSaveRequestDto) {
        ResponseResult result;
        result = todoService.insertTodoItem(todoSaveRequestDto);
        return ResponseEntity.ok().body(result.getResponseResult());
    }

    // search list
    @GetMapping("/list")
    public ResponseEntity<?> getTodoList() {
        ResponseResult result;
        result = todoService.getTodoList();
        return ResponseEntity.ok().body(result.getResponseResult());
    }
    @PostMapping("/search")
    public ResponseEntity<?> searchTodoList(@RequestBody TodoSearchListRequest todoSearchListRequest) {
        ResponseResult result;
        result = todoService.searchTodoList(todoSearchListRequest);
        return ResponseEntity.ok().body(result.getResponseResult());
    }

    // get item by doc id
    @GetMapping("/{todoId}")
    public ResponseEntity<?> getTodoItem(@PathVariable("todoId") String todoId) {
        ResponseResult result;
        result = todoService.getTodoItem(todoId);
        return ResponseEntity.ok().body(result.getResponseResult());
    }
    // get item by doc id list
    @PostMapping("/get/item")
    public ResponseEntity<?> getTodoItemListByKey(@RequestBody TodoGetItemListRequest todoGetItemListRequest) {
        ResponseResult result;
        result = todoService.getTodoItemWithMultiGet(todoGetItemListRequest);
        return ResponseEntity.ok().body(result.getResponseResult());
    }

    // update item by doc id
    @PutMapping("/{todoId}")
    public ResponseEntity<?> updateTodoItem(@PathVariable("todoId") String todoId, @Valid @RequestBody TodoUpdateRequestDto todoUpdateRequestDto) {
        ResponseResult result;
        result = todoService.updateTodoItem(todoId, todoUpdateRequestDto);
        return ResponseEntity.ok().body(result.getResponseResult());
    }

    // delete item by doc id
    @DeleteMapping("/{todoId}")
    public ResponseEntity<?> deleteTodoItem(@PathVariable("todoId") String todoId) {
        ResponseResult result;
        result = todoService.deleteTodoItem(todoId);
        return ResponseEntity.ok().body(result.getResponseResult());
    }

    // stat list
    @PostMapping("/stat")
    public ResponseEntity<?> statTodoList(@RequestBody TodoSearchListRequest todoSearchListRequest) {
        ResponseResult result;
        result = todoService.statTodoList(todoSearchListRequest);
        return ResponseEntity.ok().body(result.getResponseResult());
    }
}
