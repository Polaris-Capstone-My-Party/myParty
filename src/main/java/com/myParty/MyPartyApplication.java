package com.myParty;

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MyPartyApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyPartyApplication.class, args);
    }


    @Bean
    public LayoutDialect layoutDialect() {
        return new LayoutDialect();
    }
}
