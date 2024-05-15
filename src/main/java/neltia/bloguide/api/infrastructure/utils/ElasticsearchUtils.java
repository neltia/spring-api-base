package neltia.bloguide.api.infrastructure.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

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

    public JsonObject getTodoItem(RestHighLevelClient client, String index, String todoItemId) {
        JsonObject resultObj = null;

        GetRequest request = new GetRequest(index, todoItemId);
        GetResponse response;
        try {
            response = client.get(request, RequestOptions.DEFAULT);
        } catch (ElasticsearchStatusException | IOException e) {
            System.out.println("e.getMessage() = " + e.getMessage());
            return resultObj;
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        resultObj = gson.fromJson(response.getSourceAsString(), JsonObject.class);

        return resultObj;
    }
}
