package com.gw.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import com.gw.database.UserRepository;
import com.gw.jpa.GWUser;
import com.gw.utils.RandomString;
import com.gw.utils.UserStatus;
@RestController
public class UserController {
    
    @Autowired
    UserRepository userRepository;

    @PostMapping("/users/register")
    public UserStatus registerUser(@Validated @RequestBody GWUser newUser) {
        Iterable<GWUser> users = userRepository.findAll();
        System.out.println("New user: " + newUser.toString());
        for (GWUser user : users) {
            System.out.println("Registered user: " + newUser.toString());
            if (user.getEmail().equals(newUser.getEmail())) {
                System.out.println("User Already exists!");
                return UserStatus.USER_ALREADY_EXISTS;
            }
        }
        newUser.setId(new RandomString(10).nextString());
        userRepository.save(newUser);
        return UserStatus.SUCCESS;
    }
    @PostMapping("/users/login")
    public UserStatus loginUser(@Validated @RequestBody GWUser user) {
        Iterable<GWUser> users = userRepository.findAll();
        for (GWUser other : users) {
            if (other.equals(user)) {
                user.setLoggedIn(true);
                userRepository.save(user);
                return UserStatus.SUCCESS;
            }
        }
        return UserStatus.FAILURE;
    }
    @PostMapping("/users/logout")
    public UserStatus logUserOut(@Validated @RequestBody GWUser user) {
        Iterable<GWUser> users = userRepository.findAll();
        for (GWUser other : users) {
            if (other.equals(user)) {
                user.setLoggedIn(false);
                userRepository.save(user);
                return UserStatus.SUCCESS;
            }
        }
        return UserStatus.FAILURE;
    }
    // @DeleteMapping("/users/all")
    // public UserStatus deleteUsers() {
    //     userRepository.deleteAll();
    //     return UserStatus.SUCCESS;
    // }
}