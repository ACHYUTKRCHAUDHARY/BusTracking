package com.dtc.bus_tracker;

import com.dtc.bus_tracker.service.GtfsImportService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableKafka
public class BusTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BusTrackerApplication.class, args);
    }
    @Bean
    CommandLineRunner init(GtfsImportService gtfsImportService) {
        return args -> gtfsImportService.importAll();
    }
}