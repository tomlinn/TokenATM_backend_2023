package io.renren.modules.tokenatm.service.Response;

public class UpdateTokenResponse {

    private String status;

    public String getStatus() {
        return status;
    }

    public int getToken_amount() {
        return token_amount;
    }

    private int token_amount;

    public UpdateTokenResponse(String status, int token_amount) {
        this.status = status;
        this.token_amount = token_amount;
    }
}
