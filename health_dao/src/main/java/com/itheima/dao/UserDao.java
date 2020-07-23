package com.itheima.dao;

import com.itheima.pojo.User;

import java.util.List;

/**
 * 用户持久层接口
 */
public interface UserDao {
    /**
     * 根据用户名查询用户对象
     * @param username
     * @return
     */
    User findUserByUsername(String username);

    /**
    * @Description: 根据角色查询用户列表
    * @Param: [id]
    * @Return: java.util.List<com.itheima.pojo.User>
    * @Author: Wangqibo
    * @Date: 2020/7/22/0022
    */
    List<User> findUserByRoleId(Integer id);
}
