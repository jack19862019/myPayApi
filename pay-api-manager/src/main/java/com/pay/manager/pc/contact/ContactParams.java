package com.pay.manager.pc.contact;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ContactParams {

    @NotBlank(message = "联系方式类别不能为空")
    private String contactKey;

    @NotBlank(message = "联系方式值不能为空")
    private String contactValue;
}
