package com.hps.backend.service.impl.bot;

import com.hps.backend.mapper.BotMapper;
import com.hps.backend.pojo.Bot;
import com.hps.backend.pojo.User;
import com.hps.backend.service.bot.AddService;
import com.hps.backend.service.impl.utils.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class AddServiceImpl implements AddService {

    @Autowired
    private BotMapper botMapper;

    @Override
    public Map<String, String> add(Map<String, String> data) {
        Authentication authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        UserDetailsImpl loginUser = (UserDetailsImpl) authentication.getPrincipal();
        User user = loginUser.getUser();

        String title = data.get("title");
        String description = data.get("description");
        String content = data.get("content");

        Map<String, String> map = new HashMap<>();
        if (title == null || title.length() == 0) {
            map.put("error_message", "标题不能为空");
            return map;
        }

        if (title.length() > 100) {
            map.put("error_message", "标题长度不能超过100");
            return map;
        }


        if (description == null || description.length() == 0) {
            description = "这个用户很懒,什么也没留下~~";
        }

        if (title.length() > 100) {
            map.put("error_message", "Bot描述的长度不能超过300");
            return map;
        }

        if (content == null || content.length() == 0) {
            map.put("error_message", "Bot的代码不能为空");
            return map;
        }
        if (content.length() > 100) {
            map.put("error_message", "Bot的代码长度不能超过10000");
            return map;
        }
        Date now = new Date();
        Bot bot = new Bot(null, user.getId(), title, description, content, now, now);
        botMapper.insert(bot);

        map.put("error_message", "success");
        return map;

    }


}
