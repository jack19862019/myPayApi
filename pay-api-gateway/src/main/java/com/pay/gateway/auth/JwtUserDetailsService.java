package com.pay.gateway.auth;

import com.pay.common.security.JwtUser;
import com.pay.gateway.service.UserService;
import com.pay.gateway.service.dto.UserResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class JwtUserDetailsService implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) {
        UserResp user = userService.findByName(username);
        return createJwtUser(user);
    }

    private UserDetails createJwtUser(UserResp user) {
        log.info("{},用户请求获取JWT TOKEN",user.getUsername());
        return new JwtUser(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getRole().getRoleType(),
                user.getIsDelete(),
                user.getCreateTime(),
                null
        );
    }
}
