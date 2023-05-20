package io.renren.modules.tokenatm.entity;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

@Entity
@Table(name = "token_verify")
public class VerficationEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Integer id;

    private String email;

    private String code;
    private Date expired_date;

    public VerficationEntity(String email, String code) {
        this.email = email;
        this.code = code;
    }

    public VerficationEntity() {

    }
    public VerficationEntity(String email) {
        this.email = email;
        this.code = getRandomVerification();
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE, 15);
        this.expired_date = cal.getTime();

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


    public void setExpiredDate(Date expired_date) {
        this.expired_date = expired_date;
    }

    public Date getExpiredDate() {
        return expired_date;
    }

    public static String getRandomVerification() {
        Random random = new Random();
        int number = random.nextInt(999999);
        return String.format("%06d", number);
    }
}
