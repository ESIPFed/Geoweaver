package com.gw.jpa;

import java.util.Date;

import javax.persistence.Column;
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

    @Column
    @NonNull
    private String username;

    @Column
    @NonNull
    private String password;

    @Column
    private String role;

    @Column
    private String email;

    @Column
    private Boolean isactive;

    @Column
    private Date registration_date;

    @Column
    private Date last_login_date;

    @Column
    private Boolean loggedIn;

}
