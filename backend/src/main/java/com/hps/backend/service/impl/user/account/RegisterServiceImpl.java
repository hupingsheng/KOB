package com.hps.backend.service.impl.user.account;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hps.backend.mapper.UserMapper;
import com.hps.backend.pojo.User;
import com.hps.backend.service.user.account.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RegisterServiceImpl implements RegisterService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Map<String, String> register(String username, String password, String conformPassword) {
        Map<String, String> map = new HashMap<>();
        if(username == null){
            map.put("error_message", "用户名不为空");
            return map;
        }
        if(password == null || conformPassword == null){
            map.put("error_message", "密码不能为空");
            return map;
        }

        username = username.trim();
        if(username.length() > 100){
            map.put("error_message", "用户名长度不能大于100");
            return map;
        }
        if(password.length() == 0 || conformPassword.length() == 0) {
            map.put("error_message", "用户名不为空");
            return map;
        }

        if(!password.equals(conformPassword)){
            map.put("error_message", "两次输入的密码不一致");
            return map;
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username",username);
        List<User> users = userMapper.selectList(queryWrapper);
        if(!users.isEmpty()){
            map.put("error_message", "用户名已存在");
            return map;
        }

        String encodePassword = passwordEncoder.encode(password);
        String photo = "https://cdn.acwing.com/media/user/profile/photo/70467_lg_1d01b55658.jpg";
        User user = new User(null,username, encodePassword, photo);
        userMapper.insert(user);


        map.put("error_message", "操作成功");
        return map;
    }


}
