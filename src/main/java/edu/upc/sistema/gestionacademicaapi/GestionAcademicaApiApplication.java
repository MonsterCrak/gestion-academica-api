package edu.upc.sistema.gestionacademicaapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GestionAcademicaApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(GestionAcademicaApiApplication.class, args);
    }

}
