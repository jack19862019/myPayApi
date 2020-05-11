package com.pay.data.entity;


import com.pay.common.enums.RoleType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedBy;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
@Entity
@Where(clause = "is_delete=1")
@Table(name = "sys_role",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "num"})},
        indexes = {
                @Index(columnList = "name"),
                @Index(columnList = "num"),
        }
)
public class SysRoleEntity extends BaseEntity implements Serializable {
    @CreatedBy
    private String createUser;

    private String name;

    private String num;

    private String remark;

    @Convert(converter = RoleType.Convert.class)
    private RoleType roleType;

    @OneToMany(mappedBy = "role", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private Set<SysUserEntity> users;

    @ManyToMany
    @JoinTable(name = "z_sys_role_menu",
            joinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "menu_id", referencedColumnName = "id")}
    )
    private Set<SysMenuEntity> menus;
}
