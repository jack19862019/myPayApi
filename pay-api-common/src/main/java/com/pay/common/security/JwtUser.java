package com.pay.common.security;

import com.pay.common.enums.IsDelete;
import com.pay.common.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

@Data
@AllArgsConstructor
public class JwtUser implements UserDetails, Serializable {

    public JwtUser() {
    }

    private Long id;

    private String username;

    private String password;

    private RoleType roleType;

    private IsDelete isDelete;

    private Date lastPasswordResetDate;

    private Collection<GrantedAuthority> authorities;

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        if (isDelete.equals(IsDelete.DELETE)) {
            return false;
        }
        return true;
    }
}
