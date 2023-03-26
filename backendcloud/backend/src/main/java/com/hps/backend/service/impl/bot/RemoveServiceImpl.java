package com.hps.backend.service.impl.bot;

import com.hps.backend.mapper.BotMapper;
import com.hps.backend.pojo.Bot;
import com.hps.backend.pojo.User;
import com.hps.backend.service.bot.RemoveService;
import com.hps.backend.service.impl.utils.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RemoveServiceImpl implements RemoveService {

    @Autowired
    private BotMapper botMapper;

    @Override
    public Map<String, String> remove(Map<String, String> data) {
        Authentication authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        UserDetailsImpl loginUser = (UserDetailsImpl) authentication.getPrincipal();
        User user = loginUser.getUser();

        int bot_id = Integer.parseInt(data.get("bot_id"));

        Bot bot = botMapper.selectById(bot_id);
        Map<String, String> map = new HashMap<>();

        if(bot == null){
            map.put("error_message","该Bot不存在");
            return map;
        }
        if(!bot.getUserId().equals(user.getId())){
            map.put("error_message","没有权限删除该Bot");
            return map;
        }

        botMapper.deleteById(bot_id);
        map.put("error_message","success");
        return map;
    }
}
