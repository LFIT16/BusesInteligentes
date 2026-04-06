package com.lfit.ms_security.Models;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class User {
    @Id
    private String id;

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @Email(message = "Debe ser un email válido")
    @NotBlank
    private String email;

    private String password;

    private String githubUsername;

    private String googleId;

    private String microsoftId;

    public User() {}

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
}