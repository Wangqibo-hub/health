package com.itheima.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.itheima.dao.UserDao;
import com.itheima.pojo.User;
import com.itheima.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户服务接口实现类
 */
@Service(interfaceClass = UserService.class)
@Transactional
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;

    /**
     * 根据用户查询用户对象
     * @param username
     * @return
     */
    @Override
    public User findUserByUsername(String username) {
        return userDao.findUserByUsername(username);
    }

    /**
    * @Description: 根据角色id查询用户列表
    * @Param: [id]
    * @Return: java.util.List<com.itheima.pojo.User>
    * @Author: Wangqibo
    * @Date: 2020/7/22/0022
    */
    @Override
    public List<User> findUserByRoleId(Integer id) {
        return userDao.findUserByRoleId(id);
    }
}
