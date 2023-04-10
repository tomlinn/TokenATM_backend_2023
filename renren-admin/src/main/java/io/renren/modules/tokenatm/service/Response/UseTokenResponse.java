package io.renren.modules.tokenatm.service.Response;

public class UseTokenResponse {

    private String assignment_id;
    private Integer token_amount;//remaining tokens

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private String message;

    public String getAssignment_id() {
        return assignment_id;
    }

    public void setAssignment_id(String assignment_id) {
        this.assignment_id = assignment_id;
    }

    public Integer getToken_amount() {
        return token_amount;
    }

    public void setToken_amount(Integer token_amount) {
        this.token_amount = token_amount;
    }

    public UseTokenResponse(String assignment_id, String message, Integer token_amount) {
        this.assignment_id = assignment_id;
        this.message = message;
        this.token_amount = token_amount;
    }
}
