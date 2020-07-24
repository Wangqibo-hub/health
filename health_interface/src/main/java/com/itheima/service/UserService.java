package com.itheima.service;

import com.itheima.entity.PageResult;
import com.itheima.pojo.User;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {
    /**
     * 根据用户查询用户对象
     * @param username
     * @return
     */
    User findUserByUsername(String username);

    /**
    * @Description: 根据角色id查询用户列表
    * @Param: [id]
    * @Return: java.util.List<com.itheima.pojo.User>
    * @Author: Wangqibo
    * @Date: 2020/7/22/0022
    */
    List<User> findUserByRoleId(Integer id);

    /**
     * 用户分页查询
     * @param currentPage
     * @param pageSize
     * @param queryString
     * @return
     */
    PageResult findPage(Integer currentPage, Integer pageSize, String queryString);

    /**
     * 查询所有用户
     */
    List<User> findAll();


    /**
     * 新增用户
     * @param user
     * @param roleIds
     */
    void add(User user, Integer[] roleIds);


    /**
     * 根据用户id查询用户
     */
    User findById(Integer userId);
    /**
     * 根据用户id 查询角色ids
     */
    List<Integer> findRoleIdsByUserId(Integer userId);
    /**
     * 编辑用户
     */
    void edit(User user, Integer[] roleIds);

    /**
     * 根据用户id删除用户
     * @param id
     * @return
     */
    void deleteById(Integer id);

    /**
    * @Description: 根据菜单id查询关联的用户信息
    * @Param: [id]
    * @Return: java.util.List<com.itheima.pojo.User>
    * @Author: Wangqibo
    * @Date: 2020/7/23/0023
    */
    List<User> findUserListByMenuId(Integer id);
}
