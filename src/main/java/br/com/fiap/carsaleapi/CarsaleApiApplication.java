package br.com.fiap.carsaleapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CarsaleApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarsaleApiApplication.class, args);
    }

}
