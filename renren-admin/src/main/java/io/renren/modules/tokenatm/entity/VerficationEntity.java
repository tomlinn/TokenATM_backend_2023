package io.renren.modules.tokenatm.entity;

import javax.persistence.*;
@Entity
@Table(name = "Verification")
public class VerficationEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Integer id;

    private String email;

    private String code;

    public VerficationEntity(String email, String code) {
        this.email = email;
        this.code = code;
    }

    public VerficationEntity() {

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
