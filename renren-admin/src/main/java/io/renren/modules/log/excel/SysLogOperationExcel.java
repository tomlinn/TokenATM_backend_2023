/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.log.excel;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import java.util.Date;

@Data
public class SysLogOperationExcel {
    @Excel(name = "Operation")
    private String operation;
    @Excel(name = "Request URI")
    private String requestUri;
    @Excel(name = "Request Method")
    private String requestMethod;
    @Excel(name = "Request Params")
    private String requestParams;
    @Excel(name = "Request duration (milliseconds)")
    private Integer requestTime;
    @Excel(name = "User-Agent")
    private String userAgent;
    @Excel(name = "IP")
    private String ip;
    @Excel(name = "Status", replace = {"Failed_0", "Success_1"})
    private Integer status;
    @Excel(name = "Username")
    private String creatorName;
    @Excel(name = "Create Date", format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;

}
