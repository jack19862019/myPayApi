package com.pay.common.page;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PageReqParams {

    @NotNull(message = "分页数据不能为空")
    private int pageSize;

    @NotNull(message = "分页数据不能为空")
    private int pageNumber;
}
