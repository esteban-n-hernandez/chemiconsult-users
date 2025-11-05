package com.chemiconsult;

import com.chemiconsult.entity.UserDE;
import com.chemiconsult.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class ChemiconsultApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(ChemiconsultApplication.class, args);
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            UserDE u = new UserDE();
            u.setEmail("test@example.com");
            u.setUsername("test");
            u.setPassword(passwordEncoder.encode("password"));
            // set other required fields if your entity requires them, e.g. enabled/roles
            // u.setEnabled(true);
            u.setRol("USER");
            userRepository.save(u);
            System.out.println("Inserted test user: test@example.com / password");
        }


        System.out.println("User count: " + userRepository.count());
        for (UserDE user : userRepository.findAll()) {
            System.out.println("DB user -> email: " + user.getEmail() + ", password(hash): " + user.getPassword());
        }
    }
}
