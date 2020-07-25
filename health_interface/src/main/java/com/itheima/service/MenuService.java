package com.itheima.service;

import com.itheima.entity.PageResult;
import com.itheima.pojo.Menu;

import java.util.List;

public interface MenuService {

    /**
     * 分页
     */
    PageResult findPage(Integer currentPage, Integer pageSize, String queryString);

    /**
     * 编辑菜单
     */
    void edit(Menu menu,Integer[] roleIds);
    /**
     * 根据菜单id删除菜单
     */
    void deleteById(Integer id);

    /**
     * 查询所有菜单
     */
    List<Menu> findAll();

    /**
     * 根据id查询菜单
     */
    Menu findById(Integer id);
    /**
     * 根据菜单id 查询角色id
     */
    List<Integer> findRoleIdsByMenuId(Integer menuId);

    /**
    * @Description: 将菜单列表写入redis
    * @Param: [username]
    * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
    * @Author: Wangqibo
    * @Date: 2020/7/22/0022
    */
    String generateMenuListInRedis(String username);

    /**
    * @Description: 新增菜单
    * @Param: [menu, roleIds]
    * @Return: void
    * @Author: Wangqibo
    * @Date: 2020/7/24/0024
    */
    void add(Menu menu, Integer[] roleIds);

    void deleteMenuAndRelWithRole(Integer id);
}
