package com.hps.backend.controller.bot;

import com.hps.backend.service.bot.RemoveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RemoveController {

    @Autowired
    private RemoveService removeService;

    @PostMapping("/user/bot/remove/")
    public Map<String, String> delete(@RequestParam Map<String, String> data){
        return removeService.remove(data);
    }


}
