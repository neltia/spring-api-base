package neltia.bloguide.api.infrastructure.utils;

import com.google.gson.*;
import neltia.bloguide.api.share.ResponseCodeEnum;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.get.*;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Pattern;

@Component
public class ElasticsearchUtils {

    // exists check
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
    // get index list by index pattern
    public ArrayList<String> getIndicesList(RestHighLevelClient client, String indexPattern) {
        GetIndexRequest request = new GetIndexRequest(indexPattern);
        GetIndexResponse response = null;
        try {
            response = client.indices().get(request, RequestOptions.DEFAULT);
        } catch (ElasticsearchStatusException | IOException e) {
            System.out.println("e.getMessage() = " + e.getMessage());
            return null;
        }
        String[] indices = response.getIndices();
        ArrayList<String> result = new ArrayList<>();
        for (String index : indices) {
            if (!index.startsWith(".")) {
                result.add(index);
            }
        }
        return result;
    }
    public ArrayList<String> getIndicesSortedList(RestHighLevelClient client, String indexPattern, String sortIndex, boolean sortOrderDesc) {
        GetIndexRequest request = new GetIndexRequest(indexPattern);
        GetIndexResponse response = null;
        try {
            response = client.indices().get(request, RequestOptions.DEFAULT);
        } catch (ElasticsearchStatusException | IOException e) {
            System.out.println("e.getMessage() = " + e.getMessage());
            return null;
        }
        String[] indices = response.getIndices();
        ArrayList<String> filteredIndices = new ArrayList<>();
        Pattern monthlyPattern = Pattern.compile(sortIndex + "-\\d{6}"); // 월별로 분리된 인덱스 경우
        Pattern yearlyPattern = Pattern.compile(sortIndex + "-\\d{4}"); // 연별로 분리된 인덱스 경우

        for (String indexName : indices) {
            if (monthlyPattern.matcher(indexName).matches() || yearlyPattern.matcher(indexName).matches()) {
                filteredIndices.add(indexName);
            }
        }

        if (sortOrderDesc) {
            filteredIndices.sort(Comparator.reverseOrder());
        } else {
            filteredIndices.sort((a, b) -> b.compareTo(b));
        }

        return filteredIndices;
    }


    // insert item
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

    // get item by doc id
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
    // search item list
    public JsonObject searchTodoList(RestHighLevelClient client, String index, SearchSourceBuilder sourceBuilder) {
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

    // update by doc id
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

    // delete by doc id
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

    // get es doc with multi get
    public JsonObject getTodoListWithMultiGet(RestHighLevelClient client, String index, List<String> todoIds, String key) {
        JsonObject resultObj = new JsonObject();

        MultiGetRequest multiGetRequest = new MultiGetRequest();
        for (String todoId : todoIds) {
            multiGetRequest.add(new MultiGetRequest.Item(index, todoId));
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        MultiGetResponse response;
        try {
            response = client.mget(multiGetRequest, RequestOptions.DEFAULT);
        } catch (ElasticsearchStatusException | IOException e) {
            System.out.println("e.getCause() = " + e.getCause());
            resultObj.addProperty("error_status", ResponseCodeEnum.INTERNAL_SERVER_ERROR.toString());
            resultObj.addProperty("error_msg", e.getMessage());
            return resultObj;
        }

        String id;
        for (MultiGetItemResponse item : response.getResponses()) {
            JsonObject source = gson.fromJson(item.getResponse().getSourceAsString(), JsonObject.class);
            if (key == null || key.isEmpty()) {
                id = source.get("_id").getAsString();
                resultObj.add(id, source);
            }
            id = source.get(key).toString().replaceAll("\"", "");
            resultObj.add(id, source);
        }

        return resultObj;
    }

    // search item list
    public JsonObject aggsTodoList(RestHighLevelClient client, String index, SearchSourceBuilder sourceBuilder) {
        JsonObject resultObj = new JsonObject();

        SearchRequest request = new SearchRequest(index);
        request.source(sourceBuilder);
        SearchResponse response;
        try {
            response = client.search(request, RequestOptions.DEFAULT);
        } catch (ElasticsearchStatusException | IOException e) {
            System.out.println("e.getMessage() = " + e.getMessage());
            resultObj.addProperty("error_status", ResponseCodeEnum.INTERNAL_SERVER_ERROR.getCode());
            return resultObj;
        }

        List<Aggregation> aggsList = response.getAggregations().asList();
        long totalCount = Objects.requireNonNull(response.getHits().getTotalHits()).value;
        for (Aggregation aggs : aggsList) {
            String aggName = aggs.getName();
            Terms terms = response.getAggregations().get(aggName);

            JsonArray results = new JsonArray();
            for (Terms.Bucket bucket : terms.getBuckets()) {
                JsonObject searchData = new JsonObject();
                searchData.addProperty("key", bucket.getKeyAsString());
                searchData.addProperty("count", bucket.getDocCount());
                long docCount = bucket.getDocCount();
                int count = (int) Math.round((double) docCount / (double) totalCount * 100);
                searchData.addProperty("per", count);
                results.add(searchData);
            }
            resultObj.add(aggName, results);
        }

        return resultObj;
    }

    public JsonObject exampleMultiSearch(RestHighLevelClient client, String index, JsonArray itemList, String key) {
        JsonObject result = new JsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // set query builder
        MultiSearchRequest multiRequest = new MultiSearchRequest();
        for (JsonElement item : itemList) {
            String searchItem = item.getAsJsonObject().get(key).getAsString();

            SearchRequest searchRequest = new SearchRequest(index);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("user_id", searchItem);
            sourceBuilder.query(matchQueryBuilder);
            sourceBuilder.trackTotalHits(true);
            sourceBuilder.size(1);
            sourceBuilder.sort("last_login_date", SortOrder.DESC);

            removeTypesField(searchRequest); // for legacy es client lib version
            multiRequest.add(searchRequest);
        }

        // execute query
        MultiSearchResponse response = null;
        try {
            response = client.msearch(multiRequest, RequestOptions.DEFAULT);
        } catch (ElasticsearchStatusException | IOException e) {
            System.out.println("e.getMessage() = " + e.getMessage());
            result.addProperty("error_status", ResponseCodeEnum.INTERNAL_SERVER_ERROR.getCode());
            return result;
        }
        if (response == null) {
            result.addProperty("error_status", ResponseCodeEnum.NOT_FOUND.getCode());
            return result;
        }

        // query result parsing
        for (MultiSearchResponse.Item item : response.getResponses()) {
            if (item.isFailure()) {
                continue;
            }
            SearchHit[] searchHits = Objects.requireNonNull(item.getResponse()).getInternalResponse().hits().getHits();
            if (searchHits.length == 0) {
                continue;
            }

            String source = searchHits[0].getSourceAsString();
            JsonObject searchData = gson.fromJson(source, JsonObject.class);
            String userId = searchData.get("user_id").getAsString();
            result.add(userId, searchData);
        }
        return result;
    }
    // remove types field at search request (* legacy version es client query dsl)
    public static void removeTypesField(SearchRequest searchRequest) {
        try {
            Field typesField = SearchRequest.class.getDeclaredField("types");
            typesField.setAccessible(true);
            typesField.set(searchRequest, null);  // Set the types field to null
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.out.println("e.getCause() = " + e.getCause());
        }
    }
}
