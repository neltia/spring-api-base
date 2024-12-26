package neltia.bloguide.api.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.google.gson.JsonObject;
import neltia.bloguide.api.dto.TodoSaveRequestDto;
import neltia.bloguide.api.dto.TodoSearchListRequest;
import neltia.bloguide.api.infrastructure.utils.ElasticsearchUtils;
import neltia.bloguide.api.share.ResponseCodeEnum;
import neltia.bloguide.api.share.ResponseResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class TodoServiceTest {

    private TodoService todoService;
    private ElasticsearchClient mockElasticsearchClient;
    private ElasticsearchUtils mockElasticsearchUtils;

    @BeforeEach
    void setUp() {
        mockElasticsearchClient = Mockito.mock(ElasticsearchClient.class);
        mockElasticsearchUtils = Mockito.mock(ElasticsearchUtils.class);
        todoService = new TodoService(mockElasticsearchClient, mockElasticsearchUtils);
    }

    @Test
    void isEsIndexExists() {
        String indexName = "test_index";
        when(mockElasticsearchUtils.isIndexExists(mockElasticsearchClient, indexName)).thenReturn(true);

        ResponseResult result = todoService.isEsIndexExists(indexName);

        assertEquals(ResponseCodeEnum.OK.getCode(), result.getResultCode());
        assertTrue(result.getData().getAsJsonObject().get("isExists").getAsBoolean());
    }

    @Test
    void testGetIndicesList() {
        ArrayList<String> indices = new ArrayList<>();
        indices.add("index1");
        indices.add("index2");

        when(mockElasticsearchUtils.getIndicesList(mockElasticsearchClient, "*")).thenReturn(indices);

        ResponseResult result = todoService.getIndicesList();

        assertEquals(ResponseCodeEnum.OK.getCode(), result.getResultCode());
        assertEquals(2, result.getData().getAsJsonObject().get("indexList").getAsJsonArray().size());
    }

    @Test
    void insertTodoItem() {
        TodoSaveRequestDto saveRequest = new TodoSaveRequestDto();
        saveRequest.setTask("Test Task");
        saveRequest.setPriority("High");

        JsonObject mockResponse = new JsonObject();
        mockResponse.addProperty("id", "1");
        mockResponse.addProperty("result", "created");

        when(mockElasticsearchUtils.insertTodoItem(any(), eq("todo_index"), any())).thenReturn(mockResponse);

        ResponseResult result = todoService.insertTodoItem(saveRequest);

        assertEquals(ResponseCodeEnum.OK.getCode(), result.getResultCode());
        assertNotNull(result.getData());
        assertEquals("created", result.getData().getAsJsonObject().get("result").getAsString());
    }

    @Test
    void searchTodoList() {
        TodoSearchListRequest searchRequest = new TodoSearchListRequest();
        searchRequest.setKeyword("Test");
        searchRequest.setPriority(1);
        searchRequest.setDone(false);

        JsonObject mockResponse = new JsonObject();
        mockResponse.addProperty("total", 1);

        Map<String, Object> filters = new HashMap<>();
        filters.put("task", "Test");
        filters.put("priority", "High");
        filters.put("done", false);

        when(mockElasticsearchUtils.searchTodoList(eq(mockElasticsearchClient), eq("todo_index"), eq(filters)))
                .thenReturn(mockResponse);

        ResponseResult result = todoService.searchTodoList(searchRequest);

        assertEquals(ResponseCodeEnum.OK.getCode(), result.getResultCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getAsJsonObject().get("total").getAsInt());
    }

    @Test
    void updateTodoItem() {
    }

    @Test
    void deleteTodoItem() {
        String todoId = "testId";

        JsonObject mockResponse = new JsonObject();
        mockResponse.addProperty("status", ResponseCodeEnum.OK.toString());

        when(mockElasticsearchUtils.deleteTodoItem(mockElasticsearchClient, "todo_index", todoId))
                .thenReturn(mockResponse);

        ResponseResult result = todoService.deleteTodoItem(todoId);

        assertEquals(ResponseCodeEnum.OK.getCode(), result.getResultCode());
        assertEquals(ResponseCodeEnum.OK.toString(), result.getData().getAsJsonObject().get("status").getAsString());
    }
}