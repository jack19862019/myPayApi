package com.pay.gateway.controller.sys;


import com.alibaba.fastjson.JSON;
import com.pay.common.enums.IsDelete;
import com.pay.common.exception.Assert;
import com.pay.common.security.JwtUser;
import com.pay.common.security.SecurityUtils;
import com.pay.common.utils.Result;
import com.pay.gateway.auth.JwtTokenUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

/**
 * 登录相关
 */
@Api(tags = {"系统-登录登出接口"})
@RestController
@Slf4j
@RequestMapping("/sys")
public class SysLoginController {

    @Value("${jwt.expiration}")
    private Long expiration;

    @ApiOperation(value = "登录")
    @PostMapping(value = "/login")
    public Result login(@RequestParam String username, @RequestParam String password) {
        final JwtUser jwtUser = (JwtUser) userDetailsService.loadUserByUsername(username);
        Assert.mustBeTrue(passwordEncoder.matches(password, jwtUser.getPassword()), "密码错误");
        Assert.mustBeTrue(jwtUser.getIsDelete().equals(IsDelete.NORMAL), "账号被停用");
        // 生成令牌
        final String token = jwtTokenUtil.generateToken(jwtUser);
        redisTemplate.opsForValue().set("login:user:token"+username, token, expiration, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set("login:user:" + username, JSON.toJSONString(jwtUser), expiration, TimeUnit.SECONDS);
        return Result.success(token);
    }

    @ApiOperation(value = "登出")
    @GetMapping(value = "/logout")
    public Result logout() {
        redisTemplate.delete("login:user:" + SecurityUtils.getUsername());
        return Result.success();
    }

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    @Qualifier("jwtUserDetailsService")
    private UserDetailsService userDetailsService;

}
