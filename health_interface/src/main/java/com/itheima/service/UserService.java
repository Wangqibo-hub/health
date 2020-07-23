package com.itheima.service;

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
}
