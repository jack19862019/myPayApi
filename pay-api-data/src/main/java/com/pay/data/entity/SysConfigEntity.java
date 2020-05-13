package com.pay.data.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedBy;

import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Where(clause = "is_delete=1")
@Table(name = "sys_config")
public class SysConfigEntity extends BaseEntity {

    private String domain;

    private String initPassword;
}
