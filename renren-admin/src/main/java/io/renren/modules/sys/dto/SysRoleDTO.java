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

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 角色管理
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0
 */
@Data
@ApiModel(value = "Role_mgt")
public class SysRoleDTO implements Serializable {
    private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "id")
	@Null(message="{id.null}", groups = AddGroup.class)
	@NotNull(message="{id.require}", groups = UpdateGroup.class)
	private Long id;

	@ApiModelProperty(value = "Role Name")
	@NotBlank(message="{sysrole.name.require}", groups = DefaultGroup.class)
	private String name;

	@ApiModelProperty(value = "Remark")
	private String remark;

	@ApiModelProperty(value = "Create Date")
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Date createDate;

	@ApiModelProperty(value = "MenuID List")
	private List<Long> menuIdList;

	@ApiModelProperty(value = "GroupID List")
	private List<Long> deptIdList;

}