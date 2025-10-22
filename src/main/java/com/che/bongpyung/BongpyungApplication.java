package com.che.bongpyung;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class BongpyungApplication {

    public static void main(String[] args) {
        System.out.println("DEFAULT TZ = " + TimeZone.getDefault().getID());
        SpringApplication.run(BongpyungApplication.class, args);
    }

}
