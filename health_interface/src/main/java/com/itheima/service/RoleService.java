package com.itheima.service;

import com.itheima.entity.PageResult;
import com.itheima.pojo.Role;

import java.util.List;

public interface RoleService {
    void add(Role role, Integer[] menuIds);

    PageResult findPage(Integer currentPage, Integer pageSize, String queryString);

    Role findById(Integer roleId);


    List<Integer> findMenuIdsByRoleId(Integer roleId);

    void edit(Role role, Integer[] menuIds);

    void deleteById(Integer id);

    List<Role> findAll();
}
