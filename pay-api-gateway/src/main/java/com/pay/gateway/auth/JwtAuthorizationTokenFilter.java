package com.pay.gateway.auth;

import com.alibaba.fastjson.JSON;
import com.pay.common.exception.Assert;
import com.pay.common.security.JwtUser;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthorizationTokenFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;
    private final String tokenHeader;
    private final RedisTemplate<String, String> redisTemplate;


    public JwtAuthorizationTokenFilter(@Qualifier("jwtUserDetailsService") UserDetailsService userDetailsService, JwtTokenUtil jwtTokenUtil, @Value("${jwt.header}") String tokenHeader, RedisTemplate<String, String> redisTemplate) {
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.tokenHeader = tokenHeader;
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        final String requestHeader = request.getHeader(this.tokenHeader);
        if (!StringUtils.isEmpty(requestHeader)) {
            String username = jwtTokenUtil.getUsernameFromToken(requestHeader);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                //用户登录信息储存
                JwtUser userDetails = tokenUserInfo(requestHeader, username);
                //JwtUser userDetails = (JwtUser)this.userDetailsService.loadUserByUsername(username);
                if (jwtTokenUtil.validateToken(requestHeader, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, null);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        chain.doFilter(request, response);
    }

    private JwtUser tokenUserInfo(String tokenHeader, String username) {
        String token = redisTemplate.opsForValue().get("login:user:token"+username);
        if (!ObjectUtils.isEmpty(token)) {
            Assert.mustBeTrue(token.equals(tokenHeader), "权限认证失败");
            String userStr = redisTemplate.opsForValue().get("login:user:" + username);
            Assert.isEmpty("权限认证失败", userStr);
            return JSON.parseObject(userStr, JwtUser.class);
        }
        return null;
    }
}
