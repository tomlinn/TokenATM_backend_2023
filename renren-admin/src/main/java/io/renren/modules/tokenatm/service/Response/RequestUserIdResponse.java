package io.renren.modules.tokenatm.service.Response;

public class RequestUserIdResponse {

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public RequestUserIdResponse(String id) {
        this.id = id;
    }
}
