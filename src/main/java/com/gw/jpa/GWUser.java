package com.gw.jpa;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class GWUser {

    @Id
    private String id;

    @NonNull
    private String username;

    @NonNull
    private String password;

    private String role;

    private String email;

    private Boolean isactive;

    private Date registration_date;

    private Date last_login_date;

    private Boolean loggedIn;

}
