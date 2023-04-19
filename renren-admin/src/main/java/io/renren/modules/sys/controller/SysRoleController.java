/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package io.renren.modules.sys.controller;

import io.renren.common.annotation.LogOperation;
import io.renren.common.constant.Constant;
import io.renren.common.page.PageData;
import io.renren.common.utils.Result;
import io.renren.common.validator.AssertUtils;
import io.renren.common.validator.ValidatorUtils;
import io.renren.common.validator.group.AddGroup;
import io.renren.common.validator.group.DefaultGroup;
import io.renren.common.validator.group.UpdateGroup;
import io.renren.modules.sys.dto.SysRoleDTO;
import io.renren.modules.sys.service.SysRoleDataScopeService;
import io.renren.modules.sys.service.SysRoleMenuService;
import io.renren.modules.sys.service.SysRoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 角色管理
 * 
 * @author Mark sunlightcs@gmail.com
 */
@RestController
@RequestMapping("/sys/role")
@Api(tags="角色管理")
public class SysRoleController {
	@Autowired
	private SysRoleService sysRoleService;
	@Autowired
	private SysRoleMenuService sysRoleMenuService;
	@Autowired
	private SysRoleDataScopeService sysRoleDataScopeService;

	@GetMapping("page")
	@ApiOperation("Page")
	@ApiImplicitParams({
		@ApiImplicitParam(name = Constant.PAGE, value = "Current page, starting from 1", paramType = "query", required = true, dataType="int") ,
		@ApiImplicitParam(name = Constant.LIMIT, value = "Records per page", paramType = "query",required = true, dataType="int") ,
		@ApiImplicitParam(name = Constant.ORDER_FIELD, value = "Order Field", paramType = "query", dataType="String") ,
		@ApiImplicitParam(name = Constant.ORDER, value = "SortBy(asc、desc)", paramType = "query", dataType="String") ,
		@ApiImplicitParam(name = "name", value = "Role Name", paramType = "query", dataType="String")
	})
	@RequiresPermissions("sys:role:page")
	public Result<PageData<SysRoleDTO>> page(@ApiIgnore @RequestParam Map<String, Object> params){
		PageData<SysRoleDTO> page = sysRoleService.page(params);

		return new Result<PageData<SysRoleDTO>>().ok(page);
	}

	@GetMapping("list")
	@ApiOperation("list")
	@RequiresPermissions("sys:role:list")
	public Result<List<SysRoleDTO>> list(){
		List<SysRoleDTO> data = sysRoleService.list(new HashMap<>(1));

		return new Result<List<SysRoleDTO>>().ok(data);
	}

	@GetMapping("{id}")
	@ApiOperation("info")
	@RequiresPermissions("sys:role:info")
	public Result<SysRoleDTO> get(@PathVariable("id") Long id){
		SysRoleDTO data = sysRoleService.get(id);

		//查询角色对应的菜单
		List<Long> menuIdList = sysRoleMenuService.getMenuIdList(id);
		data.setMenuIdList(menuIdList);

		//查询角色对应的数据权限
		List<Long> deptIdList = sysRoleDataScopeService.getDeptIdList(id);
		data.setDeptIdList(deptIdList);

		return new Result<SysRoleDTO>().ok(data);
	}

	@PostMapping
	@ApiOperation("save")
	@LogOperation("save")
	@RequiresPermissions("sys:role:save")
	public Result save(@RequestBody SysRoleDTO dto){
		//效验数据
		ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

		sysRoleService.save(dto);

		return new Result();
	}

	@PutMapping
	@ApiOperation("update")
	@LogOperation("update")
	@RequiresPermissions("sys:role:update")
	public Result update(@RequestBody SysRoleDTO dto){
		//效验数据
		ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

		sysRoleService.update(dto);

		return new Result();
	}

	@DeleteMapping
	@ApiOperation("delete")
	@LogOperation("delete")
	@RequiresPermissions("sys:role:delete")
	public Result delete(@RequestBody Long[] ids){
		//效验数据
		AssertUtils.isArrayEmpty(ids, "id");

		sysRoleService.delete(ids);

		return new Result();
	}
}