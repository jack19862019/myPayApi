package com.pay.common.security;

import com.pay.common.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserInfo {

    private Long id;

    private String username;

    private RoleType roleType;
}
