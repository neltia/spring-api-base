package neltia.bloguide.api.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.google.gson.*;
import lombok.RequiredArgsConstructor;
import neltia.bloguide.api.dto.TodoGetItemListRequest;
import neltia.bloguide.api.dto.TodoSaveRequestDto;
import neltia.bloguide.api.dto.TodoSearchListRequest;
import neltia.bloguide.api.dto.TodoUpdateRequestDto;
import neltia.bloguide.api.infrastructure.utils.CommonUtils;
import neltia.bloguide.api.infrastructure.utils.ElasticsearchUtils;
import neltia.bloguide.api.share.ResponseCodeEnum;
import neltia.bloguide.api.share.ResponseResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TodoService {
    private final ElasticsearchClient localElasticClient;

    private final String todoIndex = "todo_index";

    private final ElasticsearchUtils esUtils;

    // index exists check
    public ResponseResult isEsIndexExists(String indexName) {
        ResponseResult result = new ResponseResult(0);
        JsonObject data = new JsonObject();

        boolean isExists = esUtils.isIndexExists(localElasticClient, indexName);
        data.addProperty("isExists", isExists);

        result.setResultCode(ResponseCodeEnum.OK.getCode());
        result.setData(data);
        return result;
    }
    // get index list
    public ResponseResult getIndicesList() {
        ResponseResult result = new ResponseResult(0);
        JsonObject data = new JsonObject();

        ArrayList<String> indexList = esUtils.getIndicesList(localElasticClient, "*");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        data.add("indexList", gson.toJsonTree(indexList));

        result.setResultCode(ResponseCodeEnum.OK.getCode());
        result.setData(data);
        return result;
    }
    public ResponseResult getIndicesList(String indexPattern) {
        ResponseResult result = new ResponseResult(0);
        JsonObject data = new JsonObject();

        ArrayList<String> indexList = esUtils.getIndicesList(localElasticClient, indexPattern);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        data.add("indexList", gson.toJsonTree(indexList));

        result.setResultCode(ResponseCodeEnum.OK.getCode());
        result.setData(data);
        return result;
    }

    // new item insert
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

        JsonObject data = esUtils.insertTodoItem(localElasticClient, todoIndex, source);
        if (data == null) {
            result.setResultCode(ResponseCodeEnum.INTERNAL_SERVER_ERROR.getCode());
            return result;
        }

        result.setResultCode(ResponseCodeEnum.OK.getCode());
        result.setData(data);
        return result;
    }

    // get item one by doc id
    public ResponseResult getTodoItem(String todoItemId) {
        ResponseResult result = new ResponseResult(0);

        JsonObject data = esUtils.getTodoItem(localElasticClient, todoIndex, todoItemId);
        if (data == null) {
            result.setResultCode(ResponseCodeEnum.NOT_FOUND.getCode());
            return result;
        }

        result.setResultCode(ResponseCodeEnum.OK.getCode());
        result.setData(data);
        return result;
    }

    // get all
    public ResponseResult getTodoList() {
        ResponseResult result = new ResponseResult(0);

        JsonObject data = esUtils.searchTodoList(localElasticClient, todoIndex, null);
        if (data == null) {
            result.setResultCode(ResponseCodeEnum.INTERNAL_SERVER_ERROR.getCode());
            return result;
        }

        result.setResultCode(ResponseCodeEnum.OK.getCode());
        result.setData(data);
        return result;
    }
    // search item list
    public ResponseResult searchTodoList(TodoSearchListRequest todoSearchListRequest) {
        ResponseResult result = new ResponseResult(0);

        Map<String, Object> filters = new HashMap<>();
        if (todoSearchListRequest.getKeyword() != null) {
            filters.put("task", todoSearchListRequest.getKeyword());
        }
        if (todoSearchListRequest.getPriority() != null) {
            filters.put("priority", CommonUtils.convertGbn2Priority(todoSearchListRequest.getPriority()));
        }
        if (todoSearchListRequest.getDone() != null) {
            filters.put("done", todoSearchListRequest.getDone());
        }

        JsonObject data = esUtils.searchTodoList(localElasticClient, todoIndex, filters);
        if (data == null) {
            result.setResultCode(ResponseCodeEnum.NOT_FOUND.getCode());
            return result;
        }

        result.setResultCode(ResponseCodeEnum.OK.getCode());
        result.setData(data);
        return result;
    }

    public ResponseResult updateTodoItem(String todoId, TodoUpdateRequestDto todoUpdateRequestDto) {
        ResponseResult result = new ResponseResult(0);

        JsonObject todoItem = esUtils.getTodoItem(localElasticClient, todoIndex, todoId);
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

        JsonObject data = esUtils.updateTodoItem(localElasticClient, todoIndex, todoId, source);
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

        JsonObject data = esUtils.deleteTodoItem(localElasticClient, todoIndex, todoItemId);
        String deleteStatus = data.get("status").getAsString();

        if (Objects.equals(deleteStatus, ResponseCodeEnum.NOT_FOUND.toString())) {
            result.setResultCode(ResponseCodeEnum.NOT_FOUND.getCode());
            result.setData(data);
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
        JsonObject data = esUtils.getTodoListWithMultiGet(localElasticClient, todoIndex, todoIdList, retKeyId);
        if (data == null) {
            result.setResultCode(ResponseCodeEnum.NOT_FOUND.getCode());
            return result;
        }

        result.setResultCode(ResponseCodeEnum.OK.getCode());
        result.setData(data);
        return result;
    }

    // stat item list
    public ResponseResult statTodoList(TodoSearchListRequest todoSearchListRequest) {
        ResponseResult result = new ResponseResult(0);

        // - filter query
        Map<String, Object> filters = new HashMap<>();
        if (todoSearchListRequest.getKeyword() != null) {
            filters.put("task", todoSearchListRequest.getKeyword());
        }
        if (todoSearchListRequest.getPriority() != null) {
            filters.put("priority", CommonUtils.convertGbn2Priority(todoSearchListRequest.getPriority()));
        }
        if (todoSearchListRequest.getDone() != null) {
            filters.put("done", todoSearchListRequest.getDone());
        }

        // - get stat data
        JsonObject data = esUtils.aggTodoList(localElasticClient, todoIndex, filters);
        if (data == null) {
            result.setResultCode(ResponseCodeEnum.NOT_FOUND.getCode());
            return result;
        }
        else if (data.has("error_status")) {
            result.setResultCode(data.get("error_status").getAsInt());
            return result;
        }

        result.setResultCode(ResponseCodeEnum.OK.getCode());
        result.setData(data);
        return result;
    }
}
