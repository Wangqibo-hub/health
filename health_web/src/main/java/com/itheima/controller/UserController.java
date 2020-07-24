package com.itheima.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.constant.MessageConstant;
import com.itheima.entity.PageResult;
import com.itheima.entity.QueryPageBean;
import com.itheima.entity.Result;
import com.itheima.service.MenuService;
import com.itheima.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户管理控制层
 */
@RestController
@RequestMapping("/user")
public class UserController {

    //引用服务
    @Reference
    private UserService userService;
    @Reference
    private MenuService menuService;
    @Autowired
    private JedisPool jedisPool;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    /**
     * 获取用户名 从SecurityContextHolder获取用户数据
     *
     * @return
     */
    @RequestMapping(value = "/getUserName", method = RequestMethod.GET)
    public Result getUserName() {
        try {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return new Result(true, MessageConstant.GET_USERNAME_SUCCESS, user.getUsername());
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, MessageConstant.GET_USERNAME_FAIL);
        }
    }

    /**
     * 新增用户
     *
     * @param user
     * @param roleIds
     * @return
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @PreAuthorize("hasAnyAuthority('USER_ADD')")
    public Result add(@RequestBody com.itheima.pojo.User user, Integer[] roleIds) {
        try {
            String username = user.getUsername();
            com.itheima.pojo.User user1 = userService.findUserByUsername(username);
            if (user1 != null) {
                return new Result(false, MessageConstant.ADD_USER_FAIL2);
            }
            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
            userService.add(user, roleIds);
            return new Result(true, MessageConstant.ADD_USER_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, MessageConstant.ADD_USER_FAIL);
        }
    }

    /**
     * 用户分页查询
     *
     * @param queryPageBean
     * @return
     */
    @RequestMapping(value = "/findPage", method = RequestMethod.POST)
    @PreAuthorize("hasAnyAuthority('USER_QUERY')")
    public PageResult findPage(@RequestBody QueryPageBean queryPageBean) {
        PageResult pageResult = userService.findPage(queryPageBean.getCurrentPage(), queryPageBean.getPageSize(), queryPageBean.getQueryString());
        return pageResult;
    }

    /**
     * 根据用户id查询用户
     */
    @RequestMapping(value = "/findById", method = RequestMethod.GET)
    public Result findById(Integer userId) {
        try {
            com.itheima.pojo.User user = userService.findById(userId);
            return new Result(true, MessageConstant.QUERY_USER_SUCCESS, user);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, MessageConstant.QUERY_USER_FAIL);
        }
    }

    /**
     * 根据用户id 查询角色ids
     */
    @RequestMapping(value = "/findRoleIdsByUserId", method = RequestMethod.GET)
    public List<Integer> findRoleIdsByUserId(Integer userId) {
        return userService.findRoleIdsByUserId(userId);
    }

    /**
     * 编辑用户
     */
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    @PreAuthorize("hasAnyAuthority('USER_EDIT')")
    public Result edit(@RequestBody com.itheima.pojo.User user, Integer[] roleIds) {
        try {
            String password = user.getPassword();
            String username = user.getUsername();
            com.itheima.pojo.User user1 = userService.findById(user.getId());
            if (!user1.getPassword().equals(password)) {
                user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
            }
            com.itheima.pojo.User user2 = userService.findUserByUsername(username);
            if(user2 != null){
                return new Result(false,MessageConstant.EDIT_USER_FAIL2);
            }
            userService.edit(user, roleIds);
            //更新redis中该用户的菜单信息
            menuService.generateMenuListInRedis(user.getUsername());
            return new Result(true, MessageConstant.EDIT_USER_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, MessageConstant.EDIT_USER_FAIL);
        }
    }

    /**
     * 根据用户id删除用户
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/deleteById", method = RequestMethod.GET)
    @PreAuthorize("hasAnyAuthority('USER_DELETE')")
    public Result deleteById(Integer id) {
        try {
            //查询用户名
            com.itheima.pojo.User user = userService.findById(id);
            userService.deleteById(id);
            //删除用户成功后，删除redis中该用户的菜单信息
            jedisPool.getResource().del(user.getUsername());
            return new Result(true, MessageConstant.DELETE_USER_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, MessageConstant.DELETE_USER_FAIL);
        }
    }

    /**
     * 查询所有用户
     */
    @RequestMapping(value = "/findAll", method = RequestMethod.GET)
    public Result findAll() {
        try {
            List<com.itheima.pojo.User> userList = userService.findAll();
            for (com.itheima.pojo.User user : userList) {
                if (user.getStation().equals("1")) {
                    user.setStation("是");
                } else {
                    user.setStation("否");
                }
            }
            return new Result(true, MessageConstant.QUERY_USER_SUCCESS, userList);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, MessageConstant.QUERY_USER_FAIL);
        }
    }
}
