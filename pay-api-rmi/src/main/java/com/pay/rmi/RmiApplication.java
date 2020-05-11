package com.pay.rmi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@EnableJpaRepositories(basePackages = {"com.pay.data.mapper"})
@EntityScan(basePackages = {"com.pay.data.entity"})
public class RmiApplication {
    public static void main(String[] args) {
        SpringApplication.run(RmiApplication.class, args);
    }
}
