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
 * 异常日志
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0
 */
@Data
@ApiModel(value = "Error Log")
public class SysLogErrorDTO implements Serializable {
    private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "id")
	private Long id;
	@ApiModelProperty(value = "Request URI")
	private String requestUri;
	@ApiModelProperty(value = "Request Method")
	private String requestMethod;
	@ApiModelProperty(value = "Request Params")
	private String requestParams;
	@ApiModelProperty(value = "User Agent")
	private String userAgent;
	@ApiModelProperty(value = "IP")
	private String ip;
	@ApiModelProperty(value = "Error Info")
	private String errorInfo;
	@ApiModelProperty(value = "Create Date")
	private Date createDate;

}