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
public class SysLogLoginExcel {
    @Excel(name = "Operation")
    private String operation;
    @Excel(name = "Status", replace = {"Failed_0", "Success_1", "Account Lock_1"})
    private Integer status;
    @Excel(name = "User-Agent")
    private String userAgent;
    @Excel(name = "IP")
    private String ip;
    @Excel(name = "Username")
    private String creatorName;
    @Excel(name = "Create Date", format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;

}
