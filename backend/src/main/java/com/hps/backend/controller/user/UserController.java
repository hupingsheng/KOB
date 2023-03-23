package com.hps.backend.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hps.backend.mapper.UserMapper;
import com.hps.backend.pojo.User;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "用户管理")
@RestController
public class UserController {

    @Autowired
    UserMapper userMapper;

    @GetMapping("/user/all")
    public List<User> getAll(){
        List<User> users = userMapper.selectList(null);
        return users;
    }

    @GetMapping("/user/{userId}")
    public User getUser(@PathVariable int userId) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", userId);
        return userMapper.selectOne(queryWrapper);
    }
}
