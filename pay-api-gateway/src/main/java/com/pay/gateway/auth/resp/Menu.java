package com.pay.gateway.auth.resp;

import lombok.Data;

import java.util.List;

@Data
public class Menu {

    private Long id;

    private String name;

    private int sort;

    private List<?> children;
}
