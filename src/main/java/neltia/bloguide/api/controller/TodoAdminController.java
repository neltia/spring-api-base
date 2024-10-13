package neltia.bloguide.api.controller;

import lombok.RequiredArgsConstructor;
import neltia.bloguide.api.service.TodoService;
import neltia.bloguide.api.share.ResponseResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/todo/admin")
public class TodoAdminController {

    private final TodoService todoService;

    // exists index (table)
    @GetMapping("/exists/{indexName}")
    public ResponseEntity<?> isEsIndexExists(@PathVariable String indexName) {
        ResponseResult result;
        result = todoService.isEsIndexExists(indexName);
        return ResponseEntity.ok().body(result.getResponseResult());
    }

    // index list
    @GetMapping("/index-list")
    public ResponseEntity<?> getIndexList() {
        ResponseResult result;
        result = todoService.getIndicesList();
        return ResponseEntity.ok().body(result.getResponseResult());
    }

    // index list by index pattern
    @GetMapping("/index-list/{indexPattern}")
    public ResponseEntity<?> getIndexListByPattern(@PathVariable String indexPattern,
                                                   @RequestParam(required = false) String sortField, @RequestParam(required = false) boolean sortOrderDesc) {
        ResponseResult result;
        result = todoService.getIndicesList(indexPattern);
        return ResponseEntity.ok().body(result.getResponseResult());
    }
}
