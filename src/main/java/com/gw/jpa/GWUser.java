package com.gw.jpa;

import java.sql.Date;

import javax.annotation.Generated;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;

@Entity
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
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
    @NonNull
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
