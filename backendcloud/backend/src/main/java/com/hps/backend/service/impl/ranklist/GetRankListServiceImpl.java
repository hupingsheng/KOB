package com.hps.backend.service.impl.ranklist;


import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hps.backend.mapper.UserMapper;
import com.hps.backend.pojo.User;
import com.hps.backend.service.ranklist.GetRankListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetRankListServiceImpl implements GetRankListService {

    @Autowired
    private UserMapper userMapper;


    @Override
    public JSONObject getList(Integer page) {
        IPage<User> iPage = new Page<>(page, 4);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("rating");
        List<User> users = userMapper.selectPage(iPage, queryWrapper).getRecords();
        for(User user : users){
            user.setPassword("");
        }
        JSONObject resp = new JSONObject();
        resp.put("users",users);
        resp.put("user_count",userMapper.selectCount(null));
        return resp;
    }
}
