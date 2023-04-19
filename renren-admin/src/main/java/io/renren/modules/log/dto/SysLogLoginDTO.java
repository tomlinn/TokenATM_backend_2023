/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.log.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 登录日志
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0
 */
@Data
@ApiModel(value = "Login Log")
public class SysLogLoginDTO implements Serializable {
    private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "id")
	private Long id;

	@ApiModelProperty(value = "UserOperation  0：UserLogin   1：UserLogout")
	private Integer operation;

	@ApiModelProperty(value = "Status  0：Failed    1：Success    2：Account Lock")
	private Integer status;

	@ApiModelProperty(value = "User Agent")
	private String userAgent;

	@ApiModelProperty(value = "IP")
	private String ip;

	@ApiModelProperty(value = "Username")
	private String creatorName;

	@ApiModelProperty(value = "Create Date")
	private Date createDate;

}
