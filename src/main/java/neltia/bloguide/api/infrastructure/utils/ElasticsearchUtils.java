package neltia.bloguide.api.infrastructure.utils;

import org.elasticsearch.ElasticsearchStatusException;
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
}
