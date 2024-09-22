package neltia.bloguide.api.service;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import neltia.bloguide.api.dto.TodoGetItemListRequest;
import neltia.bloguide.api.dto.TodoSaveRequestDto;
import neltia.bloguide.api.dto.TodoUpdateRequestDto;
import neltia.bloguide.api.infrastructure.utils.ElasticsearchUtils;
import neltia.bloguide.api.share.ResponseCodeEnum;
import neltia.bloguide.api.share.ResponseResult;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TodoService {
    @Autowired
    @Qualifier("localElasticClient")
    private RestHighLevelClient client;

    private final String todoIndex = "todo_index";

    private final ElasticsearchUtils esUtils;

    public ResponseResult isEsIndexExists(String indexName) {
        ResponseResult result = new ResponseResult(0);
        JsonObject data = new JsonObject();

        boolean isExists = esUtils.isIndexExists(client, indexName);
        data.addProperty("isExists", isExists);

        result.setResultCode(ResponseCodeEnum.OK.getCode());
        result.setData(data);
        return result;
    }

    public ResponseResult insertTodoItem(TodoSaveRequestDto todoSaveRequestDto) {
        ResponseResult result = new ResponseResult(0);

        JsonObject source = new JsonObject();
        String task = todoSaveRequestDto.getTask();
        String priority = todoSaveRequestDto.getPriority();
        LocalDateTime createDateTime = LocalDateTime.now();

        source.addProperty("task", task);
        source.addProperty("priority", priority);
        source.addProperty("done", false);
        source.addProperty("created_at", createDateTime.toString());

        JsonObject data = esUtils.insertTodoItem(client, todoIndex, source);
        if (data == null) {
            result.setResultCode(ResponseCodeEnum.INTERNAL_SERVER_ERROR.getCode());
            return result;
        }

        result.setResultCode(ResponseCodeEnum.OK.getCode());
        result.setData(data);
        return result;
    }

    public ResponseResult getTodoItem(String todoItemId) {
        ResponseResult result = new ResponseResult(0);

        JsonObject data = esUtils.getTodoItem(client, todoIndex, todoItemId);
        if (data == null) {
            result.setResultCode(ResponseCodeEnum.NOT_FOUND.getCode());
            return result;
        }

        result.setResultCode(ResponseCodeEnum.OK.getCode());
        result.setData(data);
        return result;
    }

    public ResponseResult getTodoList() {
        ResponseResult result = new ResponseResult(0);

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.sort("created_at", SortOrder.DESC);
        sourceBuilder.trackTotalHits(true); // document 수가 10000개 이상인 경우 필수

        JsonObject data = esUtils.getTodoList(client, todoIndex, sourceBuilder);
        if (data == null) {
            result.setResultCode(ResponseCodeEnum.INTERNAL_SERVER_ERROR.getCode());
            return result;
        }

        result.setResultCode(ResponseCodeEnum.OK.getCode());
        result.setData(data);
        return result;
    }

    public ResponseResult updateTodoItem(String todoId, TodoUpdateRequestDto todoUpdateRequestDto) {
        ResponseResult result = new ResponseResult(0);

        JsonObject todoItem = esUtils.getTodoItem(client, todoIndex, todoId);
        if (todoItem == null) {
            result.setResultCode(ResponseCodeEnum.NOT_FOUND.getCode());
            return result;
        }

        JsonObject source = new JsonObject();
        String task = todoUpdateRequestDto.getTask() == null ? todoItem.get("task").getAsString() : todoUpdateRequestDto.getTask();
        String priority = todoUpdateRequestDto.getPriority() == null ? todoItem.get("priority").getAsString() : todoUpdateRequestDto.getPriority();
        Boolean done = todoUpdateRequestDto.getDone() == null ? todoItem.get("done").getAsBoolean() : todoUpdateRequestDto.getDone();
        LocalDateTime updateDateTime = LocalDateTime.now();

        source.addProperty("task", task);
        source.addProperty("priority", priority);
        source.addProperty("done", done);
        source.addProperty("updated_at", updateDateTime.toString());

        JsonObject data = esUtils.updateTodoItem(client, todoIndex, todoId, source);
        if (data == null) {
            result.setResultCode(ResponseCodeEnum.INTERNAL_SERVER_ERROR.getCode());
            return result;
        }

        result.setResultCode(ResponseCodeEnum.OK.getCode());
        result.setData(data);
        return result;
    }

    public ResponseResult deleteTodoItem(String todoItemId) {
        ResponseResult result = new ResponseResult(0);

        JsonObject data = esUtils.deleteTodoItem(client, todoIndex, todoItemId);
        if (data == null) {
            result.setResultCode(ResponseCodeEnum.NOT_FOUND.getCode());
            return result;
        }

        result.setResultCode(ResponseCodeEnum.OK.getCode());
        result.setData(data);
        return result;
    }

    // get item list with es multi get api
    public ResponseResult getTodoItemWithMultiGet(TodoGetItemListRequest todoGetItemListRequest) {
        ResponseResult result = new ResponseResult(0);

        List<String> todoIdList = todoGetItemListRequest.getIdList();
        String retKeyId = null;
        JsonObject data = esUtils.getTodoListWithMultiGet(client, todoIndex, todoIdList, retKeyId);
        if (data == null) {
            result.setResultCode(ResponseCodeEnum.NOT_FOUND.getCode());
            return result;
        }

        result.setResultCode(ResponseCodeEnum.OK.getCode());
        result.setData(data);
        return result;
    }
}
