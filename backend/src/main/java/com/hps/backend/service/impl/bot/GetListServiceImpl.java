package com.hps.backend.service.impl.bot;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hps.backend.mapper.BotMapper;
import com.hps.backend.pojo.Bot;
import com.hps.backend.pojo.User;
import com.hps.backend.service.bot.GetListService;
import com.hps.backend.service.impl.utils.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetListServiceImpl implements GetListService {
    @Autowired
    private BotMapper botMapper;

    @Override
    public List<Bot> getList() {
        Authentication authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        UserDetailsImpl loginUser = (UserDetailsImpl) authentication.getPrincipal();
        User user = loginUser.getUser();

        QueryWrapper<Bot> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",user.getId());

        return botMapper.selectList(queryWrapper);
    }
}
