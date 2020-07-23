package com.itheima.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.constant.MessageConstant;
import com.itheima.dao.MenuDao;
import com.itheima.entity.PageResult;
import com.itheima.pojo.Menu;
import com.itheima.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Wangqibo
 * @version 1.0
 * @date 2020-07-22 09:25
 * @description 菜单服务实现类
 */
@Service(interfaceClass = MenuService.class)
@Transactional
public class MenuServiceImpl implements MenuService {

    //注入MenuDao
    @Autowired
    private MenuDao menuDao;
    //注入JedisPool
    @Autowired
    private JedisPool jedisPool;

    /**
    * @Description: 根据用户名生成菜单列表，写入redis
    * @Param: [username]
    * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
    * @Author: Wangqibo
    * @Date: 2020/7/22/0022
    */
    public String generateMenuListInRedis(String username) {
        //调用MenuDao查询菜单列表
        List<Menu> menuList = menuDao.getMenuListByUsername(username);
        //拼接数据
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Menu menu : menuList) {
            //拼接一级菜单
            Map<String, Object> map1 = new HashMap<>();
            map1.put("path", menu.getPath());
            map1.put("title", menu.getName());
            map1.put("icon", menu.getIcon());
            //拼接二级菜单
            List<Map<String, Object>> childrenList = new ArrayList<>();
            List<Menu> children = menu.getChildren();
            for (Menu child : children) {
                Map<String, Object> map2 = new HashMap<>();
                map2.put("path", child.getPath());
                map2.put("title", child.getName());
                map2.put("linkUrl", child.getLinkUrl());
                map2.put("children", child.getChildren());
                childrenList.add(map2);
            }
            map1.put("children", childrenList);
            resultList.add(map1);
        }
        //将结果集存入redis
        Object json = JSONArray.toJSON(resultList);
        String jsonStr = json.toString();
        jedisPool.getResource().set(username, jsonStr);
        return jsonStr;
    }

    @Override
    public void add(Menu menu ,Integer roleId) {
        //第一步：保存检查组表
        menuDao.add(menu);
        //第二步：获取检查组id
        Integer menuId = menu.getId();
        //第三步：往检查组检查项中间表 遍历插入关系数据
        setMenuAndRole(menuId,roleId);
    }

    @Override
    public PageResult findPage(Integer currentPage, Integer pageSize, String queryString) {
        //第一步：设置分页参数
        PageHelper.startPage(currentPage,pageSize);
        //第二步：查询数据库（代码一定要紧跟设置分页代码  没有手动分页 select * from table where name = 'xx'  ）
        Page<Menu> menuPage = menuDao.selectByCondition(queryString);
        return new PageResult(menuPage.getTotal(),menuPage.getResult());
    }

    /**
     * 根据菜单id查询菜单
     */
    @Override
    public Menu findById(Integer id) {
        return menuDao.findById(id);
    }
    /**
     * 根据菜单id 查询权限id
     */
    @Override
    public List<Integer> findRoleIdsByMenuId(Integer menuId) {
        return menuDao.findRoleIdsByMenuId(menuId);


    }
    @Override
    public void edit(Menu menu,Integer roleId) {

    //1.先根据菜单id从菜单权限中间表 删除关系数据
        menuDao.deleteRoleMenuIdByMenuId(menu.getId());
    //2.根据页面传入的权限id 和 菜单重新建立关系
         setMenuAndRole(menu.getId(),roleId);
    //3根据菜单id 更新检查组数据
        menuDao.edit(menu);
}

    /**
     * 根据检查组id删除检查组
     */
    @Override
    public void deleteById(Integer id) {
        //1.根据检查组id查询检查组检查项中间表（count(*)）
        int count1 = menuDao.findCountRoleByMenuId(id);
        if(count1>0){
            throw new RuntimeException(MessageConstant.DELETE_MENU_FAIL2);
        }
        //2.根据菜单id删除记录

        menuDao.deleteById(id);
    }

    @Override
    public List<Menu> findAll() {
        return menuDao.findAll();
    }





    /**
     * 设置菜单和权限中间表

     */
    private void setMenuAndRole(Integer groupId, Integer roleId) {
        if(roleId != null ){
                Map<String,Object> map = new HashMap<>();
                map.put("roleId",roleId);
                map.put("groupId",groupId);
                menuDao.setMenuAndRole(map);
            }
        }
    }




