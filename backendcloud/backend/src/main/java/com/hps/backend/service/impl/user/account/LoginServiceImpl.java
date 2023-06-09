package com.hps.backend.service.impl.user.account;

import com.hps.backend.pojo.User;
import com.hps.backend.service.impl.utils.UserDetailsImpl;
import com.hps.backend.service.user.account.LoginService;
import com.hps.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Override
    public Map<String, String> login(String username, String password) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);

        //会去UserDetailsService的实现类中loadUserByUsername中完成数据库校验
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);   //登录失败自动处理

        UserDetailsImpl loginUser = (UserDetailsImpl)authenticate.getPrincipal();
        User user = loginUser.getUser();
        String jwt = JwtUtil.createJWT(user.getId().toString());

        Map<String, String> map = new HashMap<>();
        map.put("error_message","success");
        map.put("token", jwt);

        return map;
    }
}
