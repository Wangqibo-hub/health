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

    void edit(Role role, Integer[] menuIds, Integer[] permissionIds,Integer[] userIds);

    List<Integer> findPermissionIdsByRoleId(Integer roleId);

    void add(Role role, Integer[] menuIds, Integer[] permissionIds, Integer[] userIds);

    List<Integer> findUserIdsByRoleId(Integer roleId);


    int findRoleExist(String roleName);

    Role findRoleByName(String name);
}
