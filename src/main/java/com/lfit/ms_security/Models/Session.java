package com.lfit.ms_security.Models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document
public class Session {
    @Id
    private String id;
    private String token;
    private Date expiration;
    private String code2FA;
    private String type;
    private Integer attempts;
    private Boolean active;

    @DBRef
    private User user;

    public Session(){

    }

    public Session(String token, Date expiration, String code2FA) {
        this.token = token;
        this.expiration = expiration;
        this.code2FA = code2FA;
        this.attempts = 0;
        this.active = true;
    }
}
