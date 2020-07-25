package com.itheima.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.constant.MessageConstant;
import com.itheima.dao.UserDao;
import com.itheima.entity.PageResult;
import com.itheima.pojo.User;
import com.itheima.service.UserService;
import com.itheima.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    * @Return: java.util.List<com.itheima.pojo.user>
    * @Author: Wangqibo
    * @Date: 2020/7/22/0022
    */
    @Override
    public List<User> findUserByRoleId(Integer id) {
        return userDao.findUserByRoleId(id);
    }

    /**
     * 新增用户
     * @param user
     * @param roleIds
     */
    @Override
    public void add(User user, Integer[] roleIds) {
        //第一步：设置新建用户默认状态是否禁用为否,并保存用户表
        user.setStation("1");
        userDao.add(user);
        //第二步：获取用户id
        Integer userId = user.getId();
        //第三步：往用户角色中间表 遍历插入关系数据
        setUserAndRole(userId, roleIds);
    }

    /**
     * 用户分页查询
     * @param currentPage
     * @param pageSize
     * @param queryString
     * @return
     */
    @Override
    public PageResult findPage(Integer currentPage, Integer pageSize, String queryString) {
        //第一步：设置分页参数
        PageHelper.startPage(currentPage, pageSize);
        //第二步：查询数据库（代码一定要紧跟设置分页代码）
        Page<User> userPage = userDao.selectPageByCondition(queryString);
        Page<Map> maps = new Page<>();
        for (User user : userPage) {
            Map<String, Object> map = new HashMap<>();
            map.put("station",user.getStation());
            try {
                map.put("birthday",DateUtils.parseDate2String(user.getBirthday()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            map.put("id",user.getId());
            map.put("gender",user.getGender());
            map.put("username",user.getUsername());
            map.put("remark",user.getRemark());
            map.put("telephone",user.getTelephone());
            maps.add(map);
        }
        return new PageResult(maps.getTotal(), maps.getResult());
    }
    /**
     * 根据用户id查询用户
     */
    @Override
    public User findById(Integer userId) {
        return userDao.findById(userId);
    }
    /**
     * 根据用户id 查询角色ids
     */
    @Override
    public List<Integer> findRoleIdsByUserId(Integer userId) {
        return userDao.findRoleIdsByUserId(userId);
    }
    /**
     * 编辑用户
     */
    @Override
    public void edit(User user, Integer[] roleIds) {
        //1.先根据用户id从用户角色中间表 删除关系数据
        userDao.deleteRelByUserId(user.getId());
        //2.根据页面传入的角色ids 和 用户重新建立关系
        setUserAndRole(user.getId(), roleIds);
        //3根据用户id 更新用户数据
        userDao.edit(user);
    }
    /**
     * 根据用户id删除用户
     * @param id
     * @return
     */
    @Override
    public void deleteById(Integer id) {
        //1.根据用户id查询用户角色中间表（count(*)）
        int count = userDao.findCountRoleByUserId(id);
        if (count > 0) {
            throw new RuntimeException(MessageConstant.DELETE_CHECKITEM_FAIL2);
        }
        //2.根据用户id删除用户记录
        userDao.deleteById(id);
    }

    /**
    * @Description: 根据菜单id查询关联的用户信息
    * @Param: [id]
    * @Return: java.util.List<com.itheima.pojo.User>
    * @Author: Wangqibo
    * @Date: 2020/7/23/0023
    */
    @Override
    public List<User> findUserListByMenuId(Integer id) {
        return userDao.findUserListByMenuId(id);
    }

    /**
     * 查询所有用户
     */
    @Override
    public List<User> findAll() {
        return userDao.findAll();
    }

    /**
     * 设置用户和角色中间表
     * @param userId
     * @param roleIds
     */
    private void setUserAndRole(Integer userId, Integer[] roleIds) {
        if(roleIds != null && roleIds.length>0){
            for (Integer roleId : roleIds) {
                Map<String,Object> map =new HashMap<>();
                map.put("roleId",roleId);
                map.put("userId",userId);
                userDao.setUserAndRole(map);
            }
        }
    }
}
