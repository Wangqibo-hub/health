package com.itheima.service;

import com.itheima.entity.PageResult;
import com.itheima.pojo.Role;

import java.util.List;

public interface RoleService {


    PageResult findPage(Integer currentPage, Integer pageSize, String queryString);

    Role findById(Integer roleId);


    List<Integer> findMenuIdsByRoleId(Integer roleId);


    void deleteById(Integer id);

    List<Role> findAll();

    void add(Role role, Integer[] menuIds, Integer[] permissionIds);

    void edit(Role role, Integer[] menuIds, Integer[] permissionIds);

    List<Integer> findPermissionIdsByRoleId(Integer roleId);

    /**
    * @Description: 删除角色及其关联关系
    * @Param: [id]
    * @Return: void
    * @Author: Wangqibo
    * @Date: 2020/7/24/0024
    */
    void deleteRoleAndRel(Integer id);
}
