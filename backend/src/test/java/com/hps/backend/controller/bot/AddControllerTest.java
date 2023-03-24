package com.hps.backend.controller.bot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AddControllerTest {

    @Autowired
    private AddController addController;

    @Test
    void add() {
        Map map = new HashMap();
        map.put("title", "王牌Bot");
        map.put("description","");
        map.put("content","int main(){}");

        Map result = addController.add(map);
        System.out.println("结果：" + result);
    }
}