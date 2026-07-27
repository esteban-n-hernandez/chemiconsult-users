package com.chemiconsult;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;
import java.util.TimeZone;

@SpringBootApplication
public class ChemiconsultApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(ChemiconsultApplication.class, args);
    }


    @Override
    public void run(String... args) {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Argentina/Buenos_Aires"));
        System.out.println("App Chemiconsult inicializada a las: " + java.time.LocalDateTime.now() + " en la zona horaria: " + TimeZone.getDefault().getID());
    }
}
