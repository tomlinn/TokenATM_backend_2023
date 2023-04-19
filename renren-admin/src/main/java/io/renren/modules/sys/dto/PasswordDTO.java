/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.sys.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 修改密码
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0
 */
@Data
@ApiModel(value = "Change Password")
public class PasswordDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "Original Password")
    @NotBlank(message="{sysuser.password.require}")
    private String password;

    @ApiModelProperty(value = "New Password")
    @NotBlank(message="{sysuser.password.require}")
    private String newPassword;

}