package com.pay.data.entity;


import com.pay.common.enums.MenuLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@Table(name = "sys_menu",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "alias", "path"})},
        indexes = {
                @Index(columnList = "name"),
                @Index(columnList = "alias"),
                @Index(columnList = "path"),
        }
)
@NoArgsConstructor
public class SysMenuEntity extends BaseEntity implements Serializable {
    @CreatedBy
    private String createUser;

    private String name;

    private int sort;

    private String path;

    private String alias;

    private String icon;

    @Convert(converter = MenuLevel.Convert.class)
    private MenuLevel menuLevel;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "parent_id")
    private SysMenuEntity parent;

    @ManyToMany(mappedBy = "menus")
    private Set<SysRoleEntity> roles;


    public SysMenuEntity(String name, int sort, String path, String alias, String icon, MenuLevel menuLevel) {
        this.name = name;
        this.sort = sort;
        this.path = path;
        this.alias = alias;
        this.icon = icon;
        this.menuLevel = menuLevel;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return this.name.equals(o);
    }
}
