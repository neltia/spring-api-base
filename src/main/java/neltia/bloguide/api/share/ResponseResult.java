package neltia.bloguide.api.share;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpHeaders;

@Getter
@Setter
public class ResponseResult {
    private int resultCode;
    private String resultMsg;
    private String errorMsg;

    private JsonElement data;

    private byte[] fileData;

    private JsonObject requestParameter;

    private HttpHeaders httpHeaders;


    public ResponseResult(int resultCode) {
        this.setResultCode(resultCode);
    }

    public JsonObject getResponseResult() {
        JsonObject result = new JsonObject();
        result.addProperty("result_code", this.resultCode);

        if(this.errorMsg != null) {
            result.addProperty("error_msg", this.errorMsg);
            return result;
        }

        result.addProperty("result_msg", this.resultMsg);
        if(this.requestParameter != null) {
            result.add("request", this.requestParameter);
        }
        if(this.data != null) {
            result.add("data", this.data);
        }
        return result;
    }

    public JsonObject getData(){
        if(this.data != null && this.data.getClass().getName().contains("JsonObject")) {
            return (JsonObject) this.data;
        } else {
            return null;
        }
    }
}
