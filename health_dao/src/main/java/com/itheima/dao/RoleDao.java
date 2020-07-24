package com.itheima.dao;

import com.github.pagehelper.Page;
import com.itheima.pojo.Role;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 角色持久层接口
 */
public interface RoleDao {

    /**
     * 根据用户id关联查询角色集合
     * @param userId
     * @return
     */
    public Set<Role> findRolesByUserId(Integer userId);
    //添加角色
    void add(Role role);
    //设置关联中间表
    void setRoleAndMenu(Map<String, Object> map);

    Page<Role> selectByCondition(String queryString);
    /**
     * 根据角色id查询角色
     */
    Role findById(Integer roleId);
    /**
     * 根据角色id 查询菜单项ids
     */
    List<Integer> findMenuIdsByRoleId(Integer roleId);
    /*删除关系数据 菜单/角色*/
    void deleteRelByRoleById(Integer id);
    /**
     * 编辑角色
     */
    void edit(Role role);
    //根据角色id查询角色/菜单中间表（count(*)）
    int findCountMenuByMenuId(Integer id);
    //根据角色id查询权限数据（count(*)）
    int findCountPermissionByRoleId(Integer id);
    /**
     * 根据角色id删除菜单
     */
    void deleteById(Integer id);
    //查询所有角色
    List<Role> findAll();

    int findCountUserByRoleId(Integer id);
    /*设置添加关联中间表(角色/权限)*/
    void setRoleAndPermission(Map<String, Object> map);
    /*删除关系数据 权限/角色*/
    void deletePermissionRelByRoleById(Integer id);

    List<Integer> findPermissionIdsByRoleId(Integer roleId);

    int findRoleExist(Role role);

    /**
    * @Description: 根据角色id删除用户与角色的关联关系
    * @Param: [id]
    * @Return: void
    * @Author: Wangqibo
    * @Date: 2020/7/24/0024
    */
    void deleteUserRelByRoleById(Integer id);


    void setRoleAndUser(Map<String, Object> map);

    List<Integer> findUserIdsByRoleId(Integer roleId);

    int findRoleExist(@Param("roleName") String roleName);
}
