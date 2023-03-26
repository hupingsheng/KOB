package com.hps.botrunningsysytem.controller;

import com.hps.botrunningsysytem.service.BotRunningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BotRunningController {

    @Autowired
    private BotRunningService botRunningService;

    @PostMapping("/bot/add/")
    public String addBot(@RequestParam MultiValueMap<String, String> data){
        Integer userId = Integer.parseInt(data.getFirst("user_id"));
        String botCode = data.getFirst("bot_code");
        String input = data.getFirst("input");

        return botRunningService.addBot(userId, botCode, input);

    }
}
