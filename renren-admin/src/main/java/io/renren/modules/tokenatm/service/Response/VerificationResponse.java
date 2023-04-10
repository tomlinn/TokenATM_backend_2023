package io.renren.modules.tokenatm.service.Response;

public class VerificationResponse {

    String status;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    String message;

    public VerificationResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }
}
