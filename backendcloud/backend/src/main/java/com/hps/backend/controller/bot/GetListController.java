package com.hps.backend.controller.bot;

import com.hps.backend.pojo.Bot;
import com.hps.backend.service.bot.GetListService;
import jdk.nashorn.internal.codegen.types.BooleanType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GetListController {
    @Autowired
    private GetListService getListService;

    @GetMapping("/user/bot/getlist/")
    public List<Bot> getList(){
        return getListService.getList();
    }

}
