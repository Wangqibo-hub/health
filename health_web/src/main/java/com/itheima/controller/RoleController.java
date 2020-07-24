package com.itheima.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.constant.MessageConstant;
import com.itheima.entity.PageResult;
import com.itheima.entity.QueryPageBean;
import com.itheima.entity.Result;
import com.itheima.pojo.Role;
import com.itheima.pojo.User;
import com.itheima.service.MenuService;
import com.itheima.service.RoleService;
import com.itheima.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * 包名:com.itheima.controller
 * 作者:Jiaming
 * 日期2020-07-22   11:21
 */
@RestController
@RequestMapping("role")
public class RoleController {

    //注入RoleService
    @Reference
    private RoleService roleService;
    //注入MenuService
    @Reference
    private MenuService menuService;
    //注入UserService
    @Reference
    private UserService userService;

    /**
     * 新增角色
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @PreAuthorize("hasAnyAuthority('ROLE_ADD')")
    public Result add(@RequestBody Role role, Integer[] menuIds,Integer[] permissionIds) {
        try {
            roleService.add(role,menuIds,permissionIds);
            return new Result(true, MessageConstant.ADD_ROLE_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, MessageConstant.ADD_ROLE_FAIL);
        }
    }

    /**
     * 检查分页查询
     */
    @RequestMapping(value = "/findPage", method = RequestMethod.POST)
    @PreAuthorize("hasAnyAuthority('ROLE_QUERY')")
    public PageResult findPage(@RequestBody QueryPageBean queryPageBean) {
        PageResult pageResult = roleService.findPage(queryPageBean.getCurrentPage(), queryPageBean.getPageSize(), queryPageBean.getQueryString());
        return pageResult;
    }


    /**
     * 根据角色id查询角色
     */
    @RequestMapping(value = "/findById", method = RequestMethod.GET)
    public Result findById(Integer roleId) {
        try {
            Role role = roleService.findById(roleId);
            return new Result(true, MessageConstant.QUERY_ROLE_SUCCESS,role);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, MessageConstant.QUERY_ROLE_FAIL);
        }
    }

    /**
     * 根据角色id 查询菜单项ids
     */
    @RequestMapping(value = "/findMenuIdsByRoleId", method = RequestMethod.GET)
    public List<Integer> findMenuIdsByRoleId(Integer roleId) {
        return roleService.findMenuIdsByRoleId(roleId);
    }

    /**
     * 根据角色id 查询权限项ids
     */
    @RequestMapping(value = "/findPermissionIdsByRoleId", method = RequestMethod.GET)
    public List<Integer> findPermissionIdsByRoleId(Integer roleId) {
        return roleService.findPermissionIdsByRoleId(roleId);
    }

    /**
     * 编辑角色
     */
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    @PreAuthorize("hasAnyAuthority('ROLE_EDIT')")
    public Result edit(@RequestBody Role role,Integer[] menuIds,Integer[] permissionIds) {
        try {
            roleService.edit(role,menuIds,permissionIds);
            //根据角色查询用户列表
            List<User> userList = userService.findUserByRoleId(role.getId());
            //更新redis中用户色菜单信息
            if (userList != null && userList.size() > 0) {
                for (User user : userList) {
                    menuService.generateMenuListInRedis(user.getUsername());
                }
            }
            return new Result(true, MessageConstant.EDIT_ROLE_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, MessageConstant.EDIT_ROLE_FAIL);
        }
    }

    /**
     * 根据角色id删除角色
     */
    @RequestMapping(value = "/deleteById", method = RequestMethod.GET)
    @PreAuthorize("hasAnyAuthority('ROLE_DELETE')")
    public Result deleteById(Integer id) {
        try {
            //根据角色查询用户列表
            List<User> userList = userService.findUserByRoleId(id);
            //调用service删除角色
            roleService.deleteById(id);
            //更新redis中用户色菜单信息
            if (userList != null && userList.size() > 0) {
                for (User user : userList) {
                    menuService.generateMenuListInRedis(user.getUsername());
                }
            }
            return new Result(true, MessageConstant.DELETE_ROLE_SUCCESS);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return new Result(false, e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            return new Result(false, MessageConstant.DELETE_ROLE_FAIL);
        }
    }


    /**
     * 查询所有角色
     */
    @RequestMapping(value = "/findAll", method = RequestMethod.GET)
    public Result findAll() {
        try {
            List<Role> roleList = roleService.findAll();
            return new Result(true, MessageConstant.QUERY_ROLE_SUCCESS,roleList);
        }catch (Exception e) {
            e.printStackTrace();
            return new Result(false, MessageConstant.QUERY_ROLE_FAIL);
        }
    }
}
