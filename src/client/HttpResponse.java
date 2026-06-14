package client;

import java.util.*;
import java.nio.charset.StandardCharsets;

public class HttpResponse {
    private String statusLine;
    private Map<String, String> headers = new HashMap<>();
    private byte[] body;

    public String getStatusLine() {
        return statusLine;
    }

    public void setStatusLine(String statusLine) {
        this.statusLine = statusLine;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public String getBodyAsString() {
        if(body == null){
            return "";
        }
        return new String(body, StandardCharsets.UTF_8);
    }
}
