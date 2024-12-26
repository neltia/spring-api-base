package neltia.bloguide.api.infrastructure.utils;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.GetIndexRequest;
import co.elastic.clients.elasticsearch.indices.GetIndexResponse;
import com.google.gson.*;
import neltia.bloguide.api.domain.Todo;
import neltia.bloguide.api.share.ResponseCodeEnum;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

@Component
public class ElasticsearchUtils {
    // Check if an index exists
    public boolean isIndexExists(ElasticsearchClient client, String index) {
        try {
            return client.indices().exists(e -> e.index(index)).value();
        } catch (Exception e) {
            System.out.println("Error checking index existence: " + e.getMessage());
            return false;
        }
    }

    // Get list of indices matching a pattern
    public ArrayList<String> getIndicesList(ElasticsearchClient client, String indexPattern) {
        GetIndexResponse response;
        try {
            response = client.indices().get(GetIndexRequest.of(r -> r.index(indexPattern)));
        } catch (Exception e) {
            System.out.println("Error fetching indices list: " + e.getMessage());
            return null;
        }

        List<String> indices = response.result().keySet().stream()
                .filter(index -> !index.startsWith("."))
                .toList();
        return new ArrayList<>(indices);
    }
    // Get list of indices matching a pattern
    public ArrayList<String> getIndicesSortedList(ElasticsearchClient client, String indexPattern, String sortIndex, boolean sortOrderDesc) {
        GetIndexResponse response;
        try {
            response = client.indices().get(GetIndexRequest.of(r -> r.index(indexPattern)));
        } catch (Exception e) {
            System.out.println("Error fetching indices list: " + e.getMessage());
            return null;
        }

        List<String> indices = response.result().keySet().stream()
                .filter(index -> !index.startsWith("."))
                .toList();
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
            filteredIndices.sort(Collections.reverseOrder());
        }

        return new ArrayList<>(indices);
    }

    // Insert a document into an index
    public JsonObject insertTodoItem(ElasticsearchClient client, String index, JsonObject source) {
        Map<String, Object> sourceMap = new Gson().fromJson(source, Map.class);
        try {
            IndexResponse response = client.index(i -> i
                    .index(index)
                    .document(sourceMap)
            );

            JsonObject result = new JsonObject();
            result.addProperty("id", response.id());
            result.addProperty("result", response.result().name());
            return result;
        } catch (Exception e) {
            System.out.println("Error inserting document: " + e.getMessage());
            return null;
        }
    }

    // Get a document by ID
    public JsonObject getTodoItem(ElasticsearchClient client, String index, String todoItemId) {
        GetResponse<Todo> response;
        try {
            response = client.get(g -> g
                            .index(index)
                            .id(todoItemId),
                    Todo.class
            );
        } catch (Exception e) {
            System.out.println("Error fetching document: " + e.getMessage());
            return null;
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        if (response.found()) {
            return gson.toJsonTree(response.source()).getAsJsonObject();
        } else {
            System.out.println("Document not found for ID: " + todoItemId);
            return null;
        }
    }

    // Search for documents
    public JsonObject searchTodoList(ElasticsearchClient client, String index, Map<String, Object> queryFilters) {
        SearchResponse<Todo> response;
        try {
            response = client.search(s -> {
                s.index(index);
                if (queryFilters != null) {
                    for (Map.Entry<String, Object> filter : queryFilters.entrySet()) {
                        s.query(q -> q.match(t -> t.field(filter.getKey()).query(filter.getValue().toString())));
                    }
                }
                return s.sort(so -> so.field(f -> f.field("created_at").order(SortOrder.Desc)));
            }, Todo.class);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonArray hits = new JsonArray();
            for (Hit<Todo> hit : response.hits().hits()) {
                JsonObject source = gson.toJsonTree(hit.source()).getAsJsonObject();
                if (source == null) {continue;}
                source.addProperty("_id", hit.id());
                hits.add(source);
            }

            JsonObject result = new JsonObject();
            result.add("hits", hits);
            return result;
        } catch (Exception e) {
            System.out.println("Error searching documents: " + e.getMessage());
            return null;
        }
    }

    // update by doc id
    public JsonObject updateTodoItem(ElasticsearchClient client, String index, String docId, JsonObject source) {
        Map<String, Object> sourceMap = new Gson().fromJson(source, Map.class);
        UpdateResponse<JsonObject> response;
        try {
            response = client.update(u -> u
                            .index(index)
                            .id(docId)
                            .doc(sourceMap),
                    JsonObject.class
            );
        } catch (Exception e) {
            System.out.println("Error updating document: " + e.getMessage());
            return null;
        }

        JsonObject result = new JsonObject();
        result.addProperty("result", response.result().name());
        result.addProperty("_id", response.id());
        return result;
    }

    // Delete a document by ID
    public JsonObject deleteTodoItem(ElasticsearchClient client, String index, String todoItemId) {
        JsonObject result = new JsonObject();
        DeleteResponse response;
        try {
            response = client.delete(d -> d
                    .index(index)
                    .id(todoItemId)
            );
        } catch (Exception e) {
            System.out.println("Error deleting document: " + e.getMessage());
            result.addProperty("status", ResponseCodeEnum.INTERNAL_SERVER_ERROR.toString());
            result.addProperty("error_msg", e.getMessage());
            return result;
        }

        if (Objects.equals(response.result().toString(), "NotFound")) {
            result.addProperty("status", ResponseCodeEnum.NOT_FOUND.toString());
        } else {
            result.addProperty("status", ResponseCodeEnum.DELETE_OK.toString());
        }

        return result;
    }

    // get es doc with multi get
    public JsonObject getTodoListWithMultiGet(ElasticsearchClient client, String index, List<String> todoIds, String key) {
        JsonObject result = new JsonObject();
        MgetResponse<Todo> response;

        try {
            response = client.mget(m -> m
                            .index(index)
                            .ids(todoIds),
                    Todo.class
            );
        } catch (Exception e) {
            System.out.println("Error retrieving multiple documents: " + e.getMessage());
            return null;
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        for (var doc : response.docs()) {
            if (doc.isFailure()) {
                continue;
            }
            JsonObject source = gson.toJsonTree(doc.result().source()).getAsJsonObject();
            if (source == null) {continue;}

            if (key == null || key.isEmpty()) {
                result.add(doc.result().id(), source);
            } else {
                String id = source.get(key).getAsString();
                result.add(id, source);
            }
        }

        return result;
    }

    // search item list
    public JsonObject aggTodoList(ElasticsearchClient client, String index, Map<String, Object> filters) {
        JsonObject resultObj = new JsonObject();

        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
        for (Map.Entry<String, Object> filter : filters.entrySet()) {
            boolQuery.filter(f -> f.term(t -> t.field(filter.getKey()).value((FieldValue) filter.getValue())));
        }

        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(index)
                .size(0) // Exclude document results
                .query(boolQuery.build()._toQuery())
                .aggregations("done_count", a -> a.terms(t -> t.field("done")))
                .aggregations("priority_count", a -> a.terms(t -> t.field("priority")))
        );

        SearchResponse<JsonObject> response;

        try {
            response = client.search(searchRequest, JsonObject.class);
        } catch (Exception e) {
            System.out.println("Error searching documents: " + e.getMessage());
            return null;
        }

        // Parse aggregation results
        for (Map.Entry<String, Aggregate> entry : response.aggregations().entrySet()) {
            String name = entry.getKey();
            Aggregate aggregation = entry.getValue();

            if (!aggregation.isSterms()) {
                System.out.println("Unsupported aggregation type: " + aggregation._kind());
                continue;
            }

            JsonArray results = new JsonArray();
            for (StringTermsBucket bucket : aggregation.sterms().buckets().array()) {
                JsonObject bucketJson = new JsonObject();
                bucketJson.addProperty("key", bucket.key().stringValue());
                bucketJson.addProperty("count", bucket.docCount());
                results.add(bucketJson);
            }
            resultObj.add(name, results);
        }
        return resultObj;
    }
}
