package neltia.bloguide.api.controller;

import lombok.RequiredArgsConstructor;
import neltia.bloguide.api.dto.TodoGetItemListRequest;
import neltia.bloguide.api.dto.TodoSaveRequestDto;
import neltia.bloguide.api.dto.TodoSearchListRequest;
import neltia.bloguide.api.dto.TodoUpdateRequestDto;
import neltia.bloguide.api.service.TodoService;
import neltia.bloguide.api.share.ResponseResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/todo")
public class TodoController {

    private final TodoService todoService;

    @GetMapping("/admin/exists/{indexName}")
    public ResponseEntity<?> isEsIndexExists(@PathVariable String indexName) {
        ResponseResult result;
        result = todoService.isEsIndexExists(indexName);
        return ResponseEntity.ok().body(result.getResponseResult());
    }

    @PostMapping("")
    public ResponseEntity<?> insertTodoItem(@Valid @RequestBody TodoSaveRequestDto todoSaveRequestDto) {
        ResponseResult result;
        result = todoService.insertTodoItem(todoSaveRequestDto);
        return ResponseEntity.ok().body(result.getResponseResult());
    }

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

    @GetMapping("/{todoId}")
    public ResponseEntity<?> getTodoItem(@PathVariable String todoId) {
        ResponseResult result;
        result = todoService.getTodoItem(todoId);
        return ResponseEntity.ok().body(result.getResponseResult());
    }
    @PostMapping("/get/item")
    public ResponseEntity<?> getTodoItemListByKey(@RequestBody TodoGetItemListRequest todoGetItemListRequest) {
        ResponseResult result;
        result = todoService.getTodoItemWithMultiGet(todoGetItemListRequest);
        return ResponseEntity.ok().body(result.getResponseResult());
    }

    @PutMapping("/{todoId}")
    public ResponseEntity<?> updateTodoItem(@PathVariable String todoId, @Valid @RequestBody TodoUpdateRequestDto todoUpdateRequestDto) {
        ResponseResult result;
        result = todoService.updateTodoItem(todoId, todoUpdateRequestDto);
        return ResponseEntity.ok().body(result.getResponseResult());
    }

    @DeleteMapping("/{todoId}")
    public ResponseEntity<?> deleteTodoItem(@PathVariable String todoId) {
        ResponseResult result;
        result = todoService.deleteTodoItem(todoId);
        return ResponseEntity.ok().body(result.getResponseResult());
    }
}
