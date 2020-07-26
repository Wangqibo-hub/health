package com.itheima.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.constant.MessageConstant;
import com.itheima.entity.PageResult;
import com.itheima.entity.QueryPageBean;
import com.itheima.entity.Result;
import com.itheima.pojo.Permission;
import com.itheima.pojo.Role;
import com.itheima.service.PermissionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 权限管理控制层
 */
@RestController
@RequestMapping("/permission")
public class PermissionController {
    @Reference
    private PermissionService permissionService;

    /**
     * 权限表分页查询
     * @param queryPageBean
     * @return
     */
    @RequestMapping("/findPage")
    public PageResult findPage(@RequestBody QueryPageBean queryPageBean){
        PageResult pageResult = permissionService.findPage(queryPageBean);
        return pageResult;
    }


    /**
     * 新增权限
     * @param permission
     * @return
     */
    @RequestMapping("/add")
    @PreAuthorize("hasAnyAuthority('PERMISSION_CONTROLLER')")
    public Result add(@RequestBody Permission permission,Integer[] roleIds){
        try {
            permissionService.add(permission,roleIds);
            return new Result(true,MessageConstant.ADD_PERMISSION_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,MessageConstant.ADD_PERMISSION_FAIL);
        }
    }

    /**
     * 编辑权限
     * @param permission
     * @return
     */
    @RequestMapping("/edit")
    @PreAuthorize("hasAnyAuthority('PERMISSION_CONTROLLER')")
    public Result edit(@RequestBody Permission permission,Integer[] roleIds){
        try {
            permissionService.edit(permission,roleIds);
            return new Result(true,MessageConstant.EDIT_PERMISSION_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,MessageConstant.EDIT_PERMISSION_FAIL);
        }
    }

    /**
     * 编辑-回显权限
     * @param id
     * @return
     */
    @RequestMapping(value = "/findById",method = RequestMethod.GET)
    public Result findById(Integer id){
        try {
            Permission permission = permissionService.findById(id);
            return new Result(true,MessageConstant.QUERY_PERMISSION_SUCCESS,permission);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,MessageConstant.QUERY_PERMISSION_FAIL);
        }
    }

    /**
     * 这里修改了
     * 删除权限
     * @param id
     * @return
     */
    @RequestMapping("/deleteById")
    @PreAuthorize("hasAnyAuthority('PERMISSION_CONTROLLER')")
    public Result deleteById(Integer id){
        Result result = permissionService.deleteById(id);
        return result;
    }

    /**
    * @Description: 直接删除权限及其关联的角色关系
    * @Param: [id]
    * @Return: com.itheima.entity.Result
    * @Author: Wangqibo
    * @Date: 2020/7/25/0025
    */
    @RequestMapping(value = "/deletePermissionAndRel",method = RequestMethod.POST)
    @PreAuthorize("hasAnyAuthority('PERMISSION_CONTROLLER')")
    public Result deletePermissionAndRel(Integer id){
        try {
            permissionService.deletePermissionAndRel(id);
            return new Result(true, MessageConstant.DELETE_PERMISSION_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, MessageConstant.DELETE_PERMISSION_FAIL2);
        }
    }

    /**
     * 查询所有权限
     */
    @RequestMapping(value = "/findAll", method = RequestMethod.POST)
    public Result findAll() {
        try {
            List<Permission> permissionList = permissionService.findAll();
            return new Result(true, MessageConstant.QUERY_PERMISSION_SUCCESS,permissionList);
        }catch (Exception e) {
            e.printStackTrace();
            return new Result(false, MessageConstant.QUERY_PERMISSION_FAIL);
        }
    }

    @RequestMapping(value = "findRoleIdsByPermissionId", method = RequestMethod.GET)
    public List<Integer> findRoleIdsByPermissionId(Integer permissionId) {
        List<Role> roleList = permissionService.findRoleByPermissionId(permissionId);
        List<Integer> roleIds = new ArrayList<>();
        if (roleList != null && roleList.size() > 0) {
            for (Role role : roleList) {
                roleIds.add(role.getId());
            }
        }
        return roleIds;
    }

    /**
     * 新增、编辑权限时异步校验权限名
     * @param permission
     * @return
     */
    @RequestMapping("/verifyPermissionName")
    @PreAuthorize("hasAnyAuthority('PERMISSION_CONTROLLER')")
    public Result verifyPermissionName(@RequestBody Permission permission){
        Result result = permissionService.verifyPermissionName(permission);
        return result;
    }

    /**
     * 新增、编辑权限时异步校验权限码
     * @param permission
     * @return
     */
    @RequestMapping("/verifyPermissionKeyword")
    @PreAuthorize("hasAnyAuthority('PERMISSION_CONTROLLER')")
    public Result verifyPermissionKeyword(@RequestBody Permission permission){
        Result result = permissionService.verifyPermissionKeyword(permission);
        return result;
    }
}
