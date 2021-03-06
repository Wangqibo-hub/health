package com.itheima.service;

import com.itheima.entity.PageResult;
import com.itheima.entity.QueryPageBean;
import com.itheima.entity.Result;
import com.itheima.pojo.Permission;
import com.itheima.pojo.Role;

import java.util.List;

/**
 * 权限管理业务接口
 */
public interface PermissionService {
    /**
     * 权限表分页查询
     * @param queryPageBean
     * @return
     */
    PageResult findPage(QueryPageBean queryPageBean);

    /**
     * 新增权限
     * @param permission
     */
    void add(Permission permission,Integer[] roleIds);

    /**
     * 编辑权限
     * @param permission
     */
    void edit(Permission permission,Integer[] roleIds);

    /**
     * 编辑-回显权限
     * @param id
     * @return
     */
    Permission findById(Integer id);

    /**
     * 这里修改了
     * 删除权限
     * @param id
     */
    Result deleteById(Integer id);

    List<Permission> findAll();

    List<Role> findRoleByPermissionId(Integer permissionId);

    /**
     * 新增、编辑权限时异步校验权限名
     * @param permission
     * @return
     */
    Result verifyPermissionName(Permission permission);

    /**
     * 新增、编辑权限时异步校验权限码
     * @param permission
     * @return
     */
    Result verifyPermissionKeyword(Permission permission);

    /**
    * @Description: 删除权限及其关联的角色关系
    * @Param: [id]
    * @Return: void
    * @Author: Wangqibo
    * @Date: 2020/7/25/0025
    */
    void deletePermissionAndRel(Integer id);
}
