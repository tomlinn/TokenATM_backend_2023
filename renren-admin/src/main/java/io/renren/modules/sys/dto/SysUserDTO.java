/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.sys.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.renren.common.validator.group.AddGroup;
import io.renren.common.validator.group.DefaultGroup;
import io.renren.common.validator.group.UpdateGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 用户管理
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0
 */
@Data
@ApiModel(value = "User_mgt")
public class SysUserDTO implements Serializable {
    private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "id")
	@Null(message="{id.null}", groups = AddGroup.class)
	@NotNull(message="{id.require}", groups = UpdateGroup.class)
	private Long id;

	@ApiModelProperty(value = "Username", required = true)
	@NotBlank(message="{sysuser.username.require}", groups = DefaultGroup.class)
	private String username;

	@ApiModelProperty(value = "Password")
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@NotBlank(message="{sysuser.password.require}", groups = AddGroup.class)
	private String password;

	@ApiModelProperty(value = "Name", required = true)
	@NotBlank(message="{sysuser.realname.require}", groups = DefaultGroup.class)
	private String realName;

	@ApiModelProperty(value = "Head")
	private String headUrl;

	@ApiModelProperty(value = "Gender   0：Male   1：Female    2：X", required = true)
	@Range(min=0, max=2, message = "{sysuser.gender.range}", groups = DefaultGroup.class)
	private Integer gender;

	@ApiModelProperty(value = "Email")
	@Email(message="{sysuser.email.error}", groups = DefaultGroup.class)
	private String email;

	@ApiModelProperty(value = "Mobile")
	private String mobile;

	@ApiModelProperty(value = "GroupID", required = true)
	@NotNull(message="{sysuser.deptId.require}", groups = DefaultGroup.class)
	private Long deptId;

	@ApiModelProperty(value = "Status  0：Disabled    1：Enabled", required = true)
	@Range(min=0, max=1, message = "{sysuser.status.range}", groups = DefaultGroup.class)
	private Integer status;

	@ApiModelProperty(value = "Create Date")
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Date createDate;

	@ApiModelProperty(value = "SuperAdmin   0：No   1：Yes")
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Integer superAdmin;

	@ApiModelProperty(value = "RoleID List")
	private List<Long> roleIdList;

	@ApiModelProperty(value = "Group Name")
	private String deptName;

}