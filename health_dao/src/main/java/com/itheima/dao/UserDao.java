package com.itheima.dao;

import com.github.pagehelper.Page;
import com.itheima.pojo.Role;
import com.itheima.pojo.User;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * 设置用户和角色中间表
     * @param map
     */
    void setUserAndRole(Map<String, Object> map);

    /**
     * 保存用户表
     * @param user
     */
    void add(User user);

    /**
     * 根据分页条件对用户分页查询
     * @param queryString
     * @return
     */
    Page<User> selectPageByCondition(String queryString);
    /**
     * 根据用户id查询用户
     */
    User findById(Integer userId);
    /**
     * 根据用户id 查询检查项ids
     */
    List<Integer> findRoleIdsByUserId(Integer userId);

    /**
     * 根据用户id 更新用户数据
     * @param user
     */
    void edit(User user);

    /**
     * 根据用户id从用户检查项中间表 删除关系数据
     * @param id
     */
    void deleteRelByUserId(Integer id);
    /**
     * 根据用户id查询用户检查项中间表
     * @param id
     * @return
     */
    int findCountRoleByUserId(Integer id);
    /**
     * 根据用户id删除用户记录
     * @param id
     */
    void deleteById(Integer id);
    /**
     * 查询所有用户
     */
    List<User> findAll();

    /**
    * @Description: 根据菜单id查询用户信息
    * @Param: [id]
    * @Return: java.util.List<com.itheima.pojo.User>
    * @Author: Wangqibo
    * @Date: 2020/7/23/0023
    */
    List<User> findUserListByMenuId(Integer id);


    Role findRoleByUserId(Integer id);

    void setLeaveUserData(HashMap<String, Object> map);

    int findUserExist(@Param("username") String username);
}
