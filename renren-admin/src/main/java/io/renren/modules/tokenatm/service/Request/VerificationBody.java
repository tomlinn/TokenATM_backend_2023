package io.renren.modules.tokenatm.service.Request;

public class VerificationBody {

    private String email;
    private String verification;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getVerification() {
        return verification;
    }

    public void setVerification(String verification) {
        this.verification = verification;
    }
}
