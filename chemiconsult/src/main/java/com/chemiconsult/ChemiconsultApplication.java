package com.chemiconsult;

import com.chemiconsult.entity.UserDE;
import com.chemiconsult.repository.UserRepository;
import com.chemiconsult.service.UserService;
import com.chemiconsult.to.UserTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@SpringBootApplication
public class ChemiconsultApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChemiconsultApplication.class, args);
    }

}
