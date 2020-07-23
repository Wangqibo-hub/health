package com.itheima.dao;

import com.github.pagehelper.Page;
import com.itheima.pojo.CheckItem;
import com.itheima.pojo.Menu;


import java.util.List;
import java.util.Map;


public interface MenuDao {
    /**
     * 保存检查组表
     *
     */
    void add(Menu menu);

    /**
     * 分页
     * @param queryString
     * @return
     */
    Page<Menu> selectByCondition(String queryString);


    /**
     * 根据检查组id 更新检查组数据
     */
    void edit(Menu menu);

    /**
     * 根据检查组id删除检查组记录
     * @param id
     */
    void deleteById(Integer id);
    /**
     * 查询所有检查组
     */
    List<Menu> findAll();


    Menu findById(Integer id);

    List<Integer> findRoleIdsByMenuId(Integer menuId);



    /**
     * 设置菜单和权限中间表

     */
    void setMenuAndRole(Map<String, Object> map);


    int findCountRoleByMenuId(Integer id);

    void deleteRoleMenuIdByMenuId(Integer id);


    /**
    * @Description: 根据用户名查询菜单列表
    * @Param: [username]
    * @Return: java.util.List<com.itheima.pojo.Menu>
    * @Author: Wangqibo
    * @Date: 2020/7/22/0022
    */
    List<Menu> getMenuListByUsername(String username);

    /**
    * @Description: 根据父菜单id查询子菜单
    * @Param: [id]
    * @Return: java.util.List<com.itheima.pojo.Menu>
    * @Author: Wangqibo
    * @Date: 2020/7/22/0022
    */
    List<Menu> findChildrenByParentId(Integer id);
}
