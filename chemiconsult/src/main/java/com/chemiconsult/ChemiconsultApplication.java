package com.chemiconsult;

import com.chemiconsult.entity.UserDE;
import com.chemiconsult.repository.UserRepository;
import com.chemiconsult.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@SpringBootApplication
public class ChemiconsultApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(ChemiconsultApplication.class, args);
    }

    @Autowired
    UserService userService;

    @Override
    public void run(String... args) {

        List<UserDE> users = userService.getUsers();
        System.out.println(users.size() + " users found in the database:");
        for (UserDE user : users) {
            System.out.println("User: " + user.getEmail() + " - Password: " + user.getPassword());
        }
    }
}
