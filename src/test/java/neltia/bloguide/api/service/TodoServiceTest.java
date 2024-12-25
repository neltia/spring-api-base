package neltia.bloguide.api.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import neltia.bloguide.api.dto.TodoSaveRequestDto;
import neltia.bloguide.api.dto.TodoSearchListRequest;
import neltia.bloguide.api.dto.TodoUpdateRequestDto;
import neltia.bloguide.api.infrastructure.utils.ElasticsearchUtils;
import neltia.bloguide.api.share.ResponseCodeEnum;
import neltia.bloguide.api.share.ResponseResult;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    @Qualifier("localElasticClient")
    private RestHighLevelClient client;  // 모킹된 ES Client

    @Mock
    private ElasticsearchUtils esUtils;

    @InjectMocks
    private TodoService todoService;

    @Test
    @DisplayName("isEsIndexExists - 인덱스 존재 시 OK & isExists:true")
    void getIndicesList() {
        // given
        String indexName = "test_index";
        when(esUtils.isIndexExists(client, indexName)).thenReturn(true);

        // when
        ResponseResult result = todoService.isEsIndexExists(indexName);

        // then
        assertThat(result.getResultCode()).isEqualTo(ResponseCodeEnum.OK.getCode());
        assertThat(result.getData()).isNotNull();
        JsonObject data = result.getData().getAsJsonObject();
        assertThat(data.get("isExists").getAsBoolean()).isTrue();
    }
    @Test
    @DisplayName("isEsIndexExists - 인덱스 미존재 시 OK & isExists:false")
    void testIsEsIndexExistsFalse() {
        // given
        String indexName = "not_exists";
        when(esUtils.isIndexExists(client, indexName)).thenReturn(false);

        // when
        ResponseResult result = todoService.isEsIndexExists(indexName);

        // then
        assertThat(result.getResultCode()).isEqualTo(ResponseCodeEnum.OK.getCode());
        JsonObject data = result.getData().getAsJsonObject();
        assertThat(data.get("isExists").getAsBoolean()).isFalse();
    }

    @Test
    @DisplayName("getIndicesList - 기본(파라미터 없는) 호출 시 OK & indexList 반환")
    void testGetIndicesListNoParam() {
        // given
        ArrayList<String> mockIndexList = new ArrayList<>();
        mockIndexList.add("todo_index");
        mockIndexList.add("another_index");

        when(esUtils.getIndicesList(client, "*")).thenReturn(mockIndexList);

        // when
        ResponseResult result = todoService.getIndicesList();

        // then
        assertThat(result.getResultCode()).isEqualTo(ResponseCodeEnum.OK.getCode());
        JsonObject data = result.getData().getAsJsonObject();
        JsonArray indexListArray = data.getAsJsonArray("indexList");
        assertThat(indexListArray.size()).isEqualTo(2);
        assertThat(indexListArray.get(0).getAsString()).isEqualTo("todo_index");
    }

    @Test
    @DisplayName("getIndicesList - indexPattern 파라미터로 호출 시 OK & indexList 반환")
    void testGetIndicesListWithParam() {
        // given
        String indexPattern = "test*";
        ArrayList<String> mockIndexList = new ArrayList<>();
        mockIndexList.add("test_index_1");
        mockIndexList.add("test_index_2");

        when(esUtils.getIndicesList(client, indexPattern)).thenReturn(mockIndexList);

        // when
        ResponseResult result = todoService.getIndicesList(indexPattern);

        // then
        assertThat(result.getResultCode()).isEqualTo(ResponseCodeEnum.OK.getCode());
        JsonArray indexListArray = result.getData().getAsJsonObject().getAsJsonArray("indexList");
        assertThat(indexListArray.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("insertTodoItem - 아이템 삽입 성공 시 OK & 데이터 반환")
    void testInsertTodoItemSuccess() {
        // given
        TodoSaveRequestDto dto = new TodoSaveRequestDto();
        dto.setTask("Test task");
        dto.setPriority("HIGH");

        // esUtils에서 반환할 가짜 JsonObject 준비
        JsonObject mockResponse = new JsonObject();
        mockResponse.addProperty("result", "created");
        when(esUtils.insertTodoItem(eq(client), eq("todo_index"), any(JsonObject.class)))
                .thenReturn(mockResponse);

        // when
        ResponseResult result = todoService.insertTodoItem(dto);

        // then
        assertThat(result.getResultCode()).isEqualTo(ResponseCodeEnum.OK.getCode());
        JsonObject data = result.getData().getAsJsonObject();
        assertThat(data.get("result").getAsString()).isEqualTo("created");
    }

    @Test
    @DisplayName("insertTodoItem - 아이템 삽입 실패 시 INTERNAL_SERVER_ERROR")
    void testInsertTodoItemFail() {
        // given
        TodoSaveRequestDto dto = new TodoSaveRequestDto();
        dto.setTask("Task fail");
        dto.setPriority("LOW");

        // insertTodoItem이 null 반환한다고 가정
        when(esUtils.insertTodoItem(eq(client), eq("todo_index"), any(JsonObject.class)))
                .thenReturn(null);

        // when
        ResponseResult result = todoService.insertTodoItem(dto);

        // then
        assertThat(result.getResultCode()).isEqualTo(ResponseCodeEnum.INTERNAL_SERVER_ERROR.getCode());
        assertThat(result.getData()).isNull(); // setData 호출 안 됨
    }

    @Test
    @DisplayName("getTodoItem - 존재하는 ID 시 OK & 데이터 반환")
    void testGetTodoItemFound() {
        // given
        String itemId = "docId_123";
        JsonObject mockTodo = new JsonObject();
        mockTodo.addProperty("task", "Buy milk");
        mockTodo.addProperty("priority", "LOW");

        when(esUtils.getTodoItem(client, "todo_index", itemId)).thenReturn(mockTodo);

        // when
        ResponseResult result = todoService.getTodoItem(itemId);

        // then
        assertThat(result.getResultCode()).isEqualTo(ResponseCodeEnum.OK.getCode());
        JsonObject data = result.getData().getAsJsonObject();
        assertThat(data.get("task").getAsString()).isEqualTo("Buy milk");
    }

    @Test
    @DisplayName("getTodoItem - 존재하지 않는 ID 시 NOT_FOUND")
    void testGetTodoItemNotFound() {
        // given
        String itemId = "docId_notfound";
        when(esUtils.getTodoItem(client, "todo_index", itemId)).thenReturn(null);

        // when
        ResponseResult result = todoService.getTodoItem(itemId);

        // then
        assertThat(result.getResultCode()).isEqualTo(ResponseCodeEnum.NOT_FOUND.getCode());
        assertThat(result.getData()).isNull();
    }

    @Test
    @DisplayName("getTodoList - 검색 성공 시 OK & 데이터 반환")
    void testGetTodoListSuccess() {
        // given
        JsonObject mockData = new JsonObject();
        mockData.addProperty("total", 5);
        when(esUtils.searchTodoList(any(), eq("todo_index"), any(SearchSourceBuilder.class)))
                .thenReturn(mockData);

        // when
        ResponseResult result = todoService.getTodoList();

        // then
        assertThat(result.getResultCode()).isEqualTo(ResponseCodeEnum.OK.getCode());
        assertThat(result.getData().getAsJsonObject().get("total").getAsInt()).isEqualTo(5);
    }

    @Test
    @DisplayName("getTodoList - null 반환 시 INTERNAL_SERVER_ERROR")
    void testGetTodoListFail() {
        // given
        when(esUtils.searchTodoList(any(), eq("todo_index"), any(SearchSourceBuilder.class)))
                .thenReturn(null);

        // when
        ResponseResult result = todoService.getTodoList();

        // then
        assertThat(result.getResultCode()).isEqualTo(ResponseCodeEnum.INTERNAL_SERVER_ERROR.getCode());
        assertThat(result.getData()).isNull();
    }

    @Test
    @DisplayName("deleteTodoItem - 삭제 성공 시 OK & 데이터 반환")
    void testDeleteTodoItemSuccess() {
        // given
        String todoItemId = "docId_delete";
        JsonObject mockResponse = new JsonObject();
        mockResponse.addProperty("result", "deleted");

        when(esUtils.deleteTodoItem(client, "todo_index", todoItemId)).thenReturn(mockResponse);

        // when
        ResponseResult result = todoService.deleteTodoItem(todoItemId);

        // then
        assertThat(result.getResultCode()).isEqualTo(ResponseCodeEnum.OK.getCode());
        assertThat(result.getData().getAsJsonObject().get("result").getAsString()).isEqualTo("deleted");
    }

    @Test
    @DisplayName("deleteTodoItem - 해당 문서 없으면 NOT_FOUND")
    void testDeleteTodoItemNotFound() {
        // given
        String todoItemId = "docId_notfound";
        when(esUtils.deleteTodoItem(client, "todo_index", todoItemId)).thenReturn(null);

        // when
        ResponseResult result = todoService.deleteTodoItem(todoItemId);

        // then
        assertThat(result.getResultCode()).isEqualTo(ResponseCodeEnum.NOT_FOUND.getCode());
        assertThat(result.getData()).isNull();
    }

    // 나머지 메서드(searchTodoList, updateTodoItem, getTodoItemWithMultiGet, statTodoList 등)도
    // 같은 방식으로 테스트 코드 작성
    @Test
    @DisplayName("searchTodoList - keyword, priority, done 값이 들어오는 경우")
    void testSearchTodoList() {
        // given
        TodoSearchListRequest request = new TodoSearchListRequest();
        request.setKeyword("milk");
        request.setPriority(1);
        request.setDone(false);

        JsonObject mockData = new JsonObject();
        mockData.addProperty("resultCount", 2);

        when(esUtils.searchTodoList(any(), eq("todo_index"), any(SearchSourceBuilder.class)))
                .thenReturn(mockData);

        // when
        ResponseResult result = todoService.searchTodoList(request);

        // then
        assertThat(result.getResultCode()).isEqualTo(ResponseCodeEnum.OK.getCode());
        assertThat(result.getData().getAsJsonObject().get("resultCount").getAsInt()).isEqualTo(2);
    }

    @Test
    @DisplayName("updateTodoItem - 항목 존재 시 OK & 데이터 반환")
    void testUpdateTodoItem() {
        // given
        String todoId = "docId_update";
        JsonObject existingData = new JsonObject();
        existingData.addProperty("task", "Old Task");
        existingData.addProperty("priority", "HIGH");
        existingData.addProperty("done", false);

        when(esUtils.getTodoItem(client, "todo_index", todoId)).thenReturn(existingData);

        TodoUpdateRequestDto updateDto = new TodoUpdateRequestDto();
        updateDto.setTask("New Task");
        updateDto.setPriority("LOW");
        updateDto.setDone(true);

        JsonObject updatedData = new JsonObject();
        updatedData.addProperty("result", "updated");

        when(esUtils.updateTodoItem(eq(client), eq("todo_index"), eq(todoId), any(JsonObject.class)))
                .thenReturn(updatedData);

        // when
        ResponseResult result = todoService.updateTodoItem(todoId, updateDto);

        // then
        assertThat(result.getResultCode()).isEqualTo(ResponseCodeEnum.OK.getCode());
        assertThat(result.getData().getAsJsonObject().get("result").getAsString()).isEqualTo("updated");
    }

    @Test
    @DisplayName("updateTodoItem - 해당 ID 없음 시 NOT_FOUND")
    void testUpdateTodoItemNotFound() {
        // given
        String todoId = "docId_notfound";
        when(esUtils.getTodoItem(client, "todo_index", todoId)).thenReturn(null);

        TodoUpdateRequestDto updateDto = new TodoUpdateRequestDto();
        updateDto.setTask("Should fail");

        // when
        ResponseResult result = todoService.updateTodoItem(todoId, updateDto);

        // then
        assertThat(result.getResultCode()).isEqualTo(ResponseCodeEnum.NOT_FOUND.getCode());
        assertThat(result.getData()).isNull();
    }
}