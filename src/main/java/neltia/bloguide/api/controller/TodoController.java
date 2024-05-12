package neltia.bloguide.api.controller;

import lombok.RequiredArgsConstructor;
import neltia.bloguide.api.service.TodoService;
import neltia.bloguide.api.share.ResponseResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
