package com.pay.gateway.aware;

import com.alibaba.fastjson.JSON;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;


@Component("auditorAware")
public class UserAuditorAware implements AuditorAware<Object> {

    @Override
    public Optional<Object> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.of("admin");
        }
        Object principal = authentication.getPrincipal();
        Map map = JSON.parseObject(JSON.toJSONString(principal), Map.class);
        return Optional.of(map.get("username"));
    }
}
