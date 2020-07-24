package com.itheima.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSONArray;
import com.itheima.constant.MessageConstant;
import com.itheima.entity.PageResult;
import com.itheima.entity.QueryPageBean;
import com.itheima.entity.Result;
import com.itheima.pojo.Menu;
import com.itheima.pojo.User;
import com.itheima.service.MenuService;
import com.itheima.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.JedisPool;
import java.util.List;
import java.util.Map;

/**
 * 菜单控制层 contrl+alt+o 去除多余的包
 */
@RestController
@RequestMapping("/menu")
public class MenuController {

    //引用服务
    @Reference
    private MenuService menuService;
    @Reference
    private UserService userService;
    //注入JedisPool
    @Autowired
    private JedisPool jedisPool;


    /**
     * 新增菜单
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @PreAuthorize("hasAnyAuthority('MENU_ADD')")
    public Result add(@RequestBody Menu menu,Integer[] roleIds) {
        try {
            menuService.add(menu,roleIds);
            return new Result(true, MessageConstant.ADD_MENU_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, MessageConstant.ADD_MENU_FAIL3);
        }
    }

    /**
     * 菜单分页查询
     */
    @RequestMapping(value = "/findPage", method = RequestMethod.POST)
    @PreAuthorize("hasAnyAuthority('MENU_QUERY')")
    public PageResult findPage(@RequestBody QueryPageBean queryPageBean) {
        PageResult pageResult = menuService.findPage(queryPageBean.getCurrentPage(), queryPageBean.getPageSize(), queryPageBean.getQueryString());
        return pageResult;
    }

    /**
     * 根据菜单id删除菜单
     */
    @RequestMapping(value = "/deleteById", method = RequestMethod.GET)
    @PreAuthorize("hasAnyAuthority('MENU_DELETE')")
    public Result deleteById(Integer id) {
        try {
            menuService.deleteById(id);
            //菜单删除成功后，更新redis中用户的菜单信息
            //获取该菜单关联的所有用户
            List<User> userList = userService.findUserListByMenuId(id);
            if (userList != null && userList.size() > 0) {
                //更新redis中这些用户的菜单信息
                for (User user : userList) {
                    menuService.generateMenuListInRedis(user.getUsername());
                }
            }
            return new Result(true, MessageConstant.DELETE_MENU_SUCCESS);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return new Result(false, e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            return new Result(false, MessageConstant.DELETE_MENU_FAIL);
        }
    }

    /**
    * @Description: 删除菜单及其与角色的关联关系
    * @Param: [id]
    * @Return: com.itheima.entity.Result
    * @Author: Wangqibo
    * @Date: 2020/7/24/0024
    */
    @RequestMapping(value = "/deleteMenuAndRelWithRole", method = RequestMethod.POST)
    @PreAuthorize("hasAnyAuthority('MENU_DELETE')")
    public Result deleteMenuAndRelWithRole(Integer id) {
        try {
            menuService.deleteMenuAndRelWithRole(id);
            return new Result(true,MessageConstant.DELETE_MENU_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,MessageConstant.DELETE_MENU_FAIL);
        }
    }

    /**
     * 查询所有菜单
     */
    @RequestMapping(value = "/findAll", method = RequestMethod.POST)
    public Result findAll() {
        try {
            List<Menu> menuList = menuService.findAll();
            return new Result(true,MessageConstant.QUERY_MENU_FAIL,menuList);
        }catch (Exception e) {
            e.printStackTrace();
            return new Result(false, MessageConstant.QUERY_MENU_FAIL);
        }
    }

    /**
     * 编辑菜单
     */
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    @PreAuthorize("hasAnyAuthority('MENU_EDIT')")
    public Result edit(@RequestBody Menu menu,Integer[] roleIds) {
        try {
            menuService.edit(menu,roleIds);
            return new Result(true, MessageConstant.EDIT_MENU_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, MessageConstant.EDIT_MENU_FAIL);
        }
    }

    /**
     * 根据id查询菜单
     */
    @RequestMapping(value = "/findById", method = RequestMethod.POST)
    public Result findById(Integer id) {
        try {
            Menu  menu = menuService.findById(id);
            return new Result(true, MessageConstant.QUERY_MENU_SUCCESS, menu);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, MessageConstant.QUERY_MENU_FAIL);
        }
    }

    /**
     * 根据菜单id 查询角色id
     */
    @RequestMapping(value = "/findRoleIdsByMenuId", method = RequestMethod.GET)
    public List<Integer> findRoleIdsByMenuId(Integer menuId) {
        return menuService.findRoleIdsByMenuId(menuId);
    }


    /**
     * @Description: 获取菜单列表
     * @Param: [username]
     * @Return: com.itheima.entity.Result
     * @Author: Wangqibo
     * @Date: 2020/7/22/0022
     */
    @RequestMapping(value = "getMenuListByUsername", method = RequestMethod.GET)
    public Result getMenuListByUsername(String username) {
        try {
            //从redis获取菜单列表
            String menuListStr = jedisPool.getResource().get(username);
            //判断是否为第一次登陆
            if (menuListStr == null) {
                //第一次登陆调用MenuService将菜单列表写入redis
                menuListStr = menuService.generateMenuListInRedis(username);
            }
            List<Map> menuList = JSONArray.parseArray(menuListStr, Map.class);
            //返回数据给前端页面
            return new Result(true, MessageConstant.GET_MENU_SUCCESS, menuList);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, MessageConstant.GET_MENU_FAIL);
        }
    }
}
