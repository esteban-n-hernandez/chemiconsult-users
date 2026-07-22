package com.chemiconsult;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class ChemiconsultApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(ChemiconsultApplication.class, args);
    }


    @Override
    public void run(String... args) throws Exception {
        System.out.println("App inicializada");
    }
}
