package com.itheima.dao;

import com.github.pagehelper.Page;
import com.itheima.pojo.CheckItem;
import com.itheima.pojo.Menu;


import java.util.List;
import java.util.Map;


public interface MenuDao {
    /**
     * 保存菜单表
     *
     */
    Integer add(Menu menu);

    /**
     * 分页
     * @param queryString
     * @return
     */
    Page<Menu> selectByCondition(String queryString);


    /**
     * 根据菜单id 更新菜单数据
     */
    void edit(Menu menu);

    /**
     * 根据菜单id删除菜单记录
     * @param id
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
     * 根据菜单id查询角色id
     */
    List<Integer> findRoleIdsByMenuId(Integer menuId);
    /**
     * 设置菜单和角色中间表
     */
    void setMenuAndRole(Map<String, Object> map);
    /**
     * 根据菜单id查询菜单角色中间表
     */

    int findCountRoleByMenuId(Integer id);
    /**
     * 根据菜单id查询菜单角色中间表删除联系
     */
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
    /**
     * 查询菜单名是否重复
     */
    int findByName(String menuName);
    /**
     * 查询菜单linkUrl是否重复
     */
    int findByLinkUrl(String linkUrl);

    Menu findByMenuName(String menuName);

    /**
    * @Description: 按等级查询菜单
    * @Param: [level]
    * @Return: java.util.List<com.itheima.pojo.Menu>
    * @Author: Wangqibo
    * @Date: 2020/7/25/0025
    */
    List<Menu> findMenuByLevel(Integer level);

}
