package neltia.bloguide.api.infrastructure.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import neltia.bloguide.api.share.ResponseCodeEnum;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class ElasticsearchUtils {

    public boolean isIndexExists(RestHighLevelClient client, String index) {
        boolean isExists = false;

        try {
            GetIndexRequest request = new GetIndexRequest(index);
            isExists = client.indices().exists(request, RequestOptions.DEFAULT);
        } catch (ElasticsearchStatusException | IOException e) {
            System.out.println("e.getMessage() = " + e.getMessage());
        }

        return isExists;
    }

    public JsonObject insertTodoItem(RestHighLevelClient client, String index, JsonObject source) {
        Map<String, Object> sourceMap = JsonHelper.toMap(source);

        IndexRequest request = new IndexRequest(index)
                // .id("1") // Optional: Specify a custom ID, or omit to let ES generate one
                .source(sourceMap, XContentType.JSON);
        IndexResponse response;

        try {
            response = client.index(request, RequestOptions.DEFAULT);
        } catch (ElasticsearchStatusException | IOException e) {
            System.out.println("e.getMessage() = " + e.getMessage());
            return null;
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject resultObj = gson.fromJson(response.toString(), JsonObject.class);

        return resultObj;
    }

    public JsonObject getTodoItem(RestHighLevelClient client, String index, String todoItemId) {
        GetRequest request = new GetRequest(index, todoItemId);
        GetResponse response;
        try {
            response = client.get(request, RequestOptions.DEFAULT);
        } catch (ElasticsearchStatusException | IOException e) {
            System.out.println("e.getMessage() = " + e.getMessage());
            return null;
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject resultObj = gson.fromJson(response.getSourceAsString(), JsonObject.class);

        return resultObj;
    }

    public JsonObject getTodoList(RestHighLevelClient client, String index, SearchSourceBuilder sourceBuilder) {
        JsonObject resultObj = new JsonObject();

        SearchRequest request = new SearchRequest(index);
        request.source(sourceBuilder);
        SearchResponse response;
        try {
            response = client.search(request, RequestOptions.DEFAULT);
        } catch (ElasticsearchStatusException | IOException e) {
            System.out.println("e.getMessage() = " + e.getMessage());
            return null;
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonArray dataList = new JsonArray();
        for (SearchHit hit : response.getHits()) {
            JsonObject source = gson.fromJson(hit.getSourceAsString(), JsonObject.class);
            source.addProperty("_id", hit.getId());
            dataList.add(source);
        }
        resultObj.add("list", dataList);

        return resultObj;
    }

    public JsonObject updateTodoItem(RestHighLevelClient client, String index, String docId, JsonObject source) {
        Map<String, Object> sourceMap = JsonHelper.toMap(source);

        UpdateRequest request = new UpdateRequest(index, docId).doc(sourceMap);
        UpdateResponse response;

        try {
            response = client.update(request, RequestOptions.DEFAULT);
        } catch (ElasticsearchStatusException | IOException e) {
            System.out.println("e.getMessage() = " + e.getMessage());
            return null;
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject resultObj = gson.fromJson(response.toString(), JsonObject.class);

        return resultObj;
    }

    public JsonObject deleteTodoItem(RestHighLevelClient client, String index, String todoItemId) {
        JsonObject resultObj = new JsonObject();

        DeleteRequest request = new DeleteRequest(index, todoItemId);
        DeleteResponse response;
        try {
            response = client.delete(request, RequestOptions.DEFAULT);
            System.out.println(response.toString());
        } catch (ElasticsearchStatusException | IOException e) {
            System.out.println("e.getMessage() = " + e.getMessage());
            return null;
        }

        resultObj.addProperty("data", ResponseCodeEnum.DATA_EXISTS.toString());
        return resultObj;
    }
}
