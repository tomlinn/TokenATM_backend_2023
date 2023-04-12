package io.renren.modules.tokenatm.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "Tokens")
public class TokenCountEntity {
    @Id
    private String user_id;

    private Integer token_count;

    private String user_name;

    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }

    private String user_email;

    private Date timestamp;

    public String getUser_name() {
        return user_name;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public Integer getToken_count() {
        return token_count;
    }


    public void setTimestamp(Date current_time) {
        this.timestamp = current_time;
    }
    public Date getTimestamp() {
        return timestamp;
    }

    public void setToken_count(Integer token_count) {
        this.token_count = token_count;
    }
}
