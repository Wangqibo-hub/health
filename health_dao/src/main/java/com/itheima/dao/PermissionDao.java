package com.itheima.dao;

import com.github.pagehelper.Page;
import com.itheima.pojo.Permission;
import com.itheima.pojo.Role;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * 权限持久层接口
 */
public interface PermissionDao {

    /**
     * 根据角色id关联查询权限集合
     * @param roleId
     * @return
     */
    public Set<Permission> findPermissionsByRoleId(Integer roleId);
    /**
     * 分页查询
     * @param queryString
     * @return
     */
    Page<Permission> findPage(@Param("queryString") String queryString);

    /**
     * 新增权限
     * @param permission
     */
    void add(Permission permission);

    /**
     * 编辑权限
     * @param permission
     */
    void edit(Permission permission);

    /**
     * 编辑-回显权限
     * 根据id查询权限
     * @param id
     * @return
     */
    Permission findById(Integer id);

    /**
     * 删除权限
     * @param id
     */
    void deleteById(Integer id);

    /**
     * 这里修改了，新增查询角色表
     * @param id
     * @return
     */
    List<Role> findRoleByPermissionId(Integer id);
    /**
     * 删除权限
     * @param
     */
    List<Permission> findAll();
    /**
     * 根据权限码查询权限
     * @param keyword
     * @return
     */
    int findByKeyword(String keyword);

    /**
     * 根据权限名查询权限
     * @param permissionName
     * @return
     */
    int findByName(String permissionName);
}
