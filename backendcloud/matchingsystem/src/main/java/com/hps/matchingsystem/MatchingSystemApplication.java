package com.hps.matchingsystem;


import com.hps.matchingsystem.service.impl.MatchingServiceImpl;
import com.hps.matchingsystem.service.impl.utils.MatchingPool;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MatchingSystemApplication {
    public static void main(String[] args) {
        MatchingServiceImpl.matchingpool.start();
        SpringApplication.run(MatchingSystemApplication.class, args);
        System.out.println("=====MatchingSystem start successful======");
    }
}
