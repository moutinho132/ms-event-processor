package com.dev.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de la aplicación MS-Event-Processor.
 * 
 * Microservicio event-driven para procesamiento de eventos de órdenes
 * consumidos desde Apache Kafka y reenviados a AWS SQS.
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
