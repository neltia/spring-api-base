package neltia.bloguide.api.service;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import neltia.bloguide.api.infrastructure.utils.ElasticsearchUtils;
import neltia.bloguide.api.share.ResponseCodeEnum;
import neltia.bloguide.api.share.ResponseResult;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TodoService {
    @Autowired
    @Qualifier("localElasticClient")
    private RestHighLevelClient client;

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
}
