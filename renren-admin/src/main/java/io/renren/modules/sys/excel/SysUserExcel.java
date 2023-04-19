/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.sys.excel;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import java.util.Date;

/**
 * 用户管理
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0
 */
@Data
public class SysUserExcel {
    @Excel(name = "Username")
    private String username;
    @Excel(name = "Name")
    private String realName;
    @Excel(name = "Gender", replace = {"Male_0", "Female_1", "X_2"})
    private Integer gender;
    @Excel(name = "Email")
    private String email;
    @Excel(name = "Mobile")
    private String mobile;
    @Excel(name = "Group Name")
    private String deptName;
    @Excel(name = "Status", replace = {"Disable_0", "Enable_1"})
    private Integer status;
    @Excel(name = "Remark")
    private String remark;
    @Excel(name = "Create Date", format = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;

}
