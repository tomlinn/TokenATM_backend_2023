package io.renren.modules.tokenatm.service.Response;

public class RequestVerificationResponse {

    String status;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    String message;

    public RequestVerificationResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }
}
