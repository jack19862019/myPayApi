package com.pay.gateway.service;

import com.pay.gateway.service.dto.UserResp;

public interface UserService {

    UserResp findByName(String userName);

}
