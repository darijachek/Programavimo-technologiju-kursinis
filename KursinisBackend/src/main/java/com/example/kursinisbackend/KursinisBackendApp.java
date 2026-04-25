package com.example.kursinisbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KursinisBackendApp {

    public static void main(String[] args) {
        SpringApplication.run(KursinisBackendApp.class, args);

        System.out.println(" SERVERIS SEKMINGAI PALEISTAS ");
        System.out.println(" H2 Console: http://localhost:8080/h2-console ");
    }

}
