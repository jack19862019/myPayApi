package com.pay.data.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedBy;

import javax.persistence.*;


@Getter
@Setter
@Entity
@Where(clause = "is_delete=1")
@Table(name = "sys_user",
        indexes = {
                @Index(columnList = "username")
        }
)
public class SysUserEntity extends BaseEntity {
    @CreatedBy
    private String createUser;

    private String username;

    private String password;

    private String email;

    private String phone;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private SysRoleEntity role;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "user", orphanRemoval = true)
    private MerchantEntity merchant;
}
