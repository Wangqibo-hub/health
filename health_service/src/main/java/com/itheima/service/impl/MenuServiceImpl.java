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
     * @Return: java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     * @Author: Wangqibo
     * @Date: 2020/7/22/0022
     */
    public String generateMenuListInRedis(String username) {
        //调用MenuDao查询菜单列表
        List<Menu> menuListFromDB = menuDao.getMenuListByUsername(username);
        //定义返回给前端的list集合
        List<Map<String, Object>> menuListForWeb = new ArrayList<>();
        //定义存入redis的字符串
        String menuListStr = null;
        //判断menuList是否有效
        if (menuListFromDB == null || menuListFromDB.size() == 0) {
            //无效，防止缓存穿透，将空值写入redis并设置过期时间60秒
            Object json = JSONArray.toJSON(menuListFromDB);
            menuListStr = json.toString();
            jedisPool.getResource().setex(username, 60, menuListStr);
        } else {
            //有效，拼接数据
            for (Menu menu : menuListFromDB) {
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
                menuListForWeb.add(map1);
            }
            //将结果集存入redis
            Object json = JSONArray.toJSON(menuListForWeb);
            menuListStr = json.toString();
            jedisPool.getResource().set(username, menuListStr);
        }
        return menuListStr;
    }

    /**
     * 新增菜单
     */
    @Override
    public void add(Menu menu, Integer[] roleIds) {
        String menuName = menu.getName();
        Menu byMenuName = menuDao.findByMenuName(menuName);
        if (byMenuName != null) {
            throw new RuntimeException("该菜单名称已经存在");
        }

        //判断添加的是几级菜单
        Integer level = menu.getLevel();
        //新增菜单的优先级
        Integer priority = menu.getPriority();
        if (level == 1) {
            //添加的是一级菜单
            //获取数据库中所有的一级菜单
            List<Menu> firstMenuList = menuDao.findfirstMenu();
            //更新所有优先级大于新增菜单的一级菜单的优先级
            for (Menu firstMenu : firstMenuList) {
                Integer priorityExist = firstMenu.getPriority();
                if (priorityExist >= priority) {
                    firstMenu.setPriority(priorityExist + 1);
                    //更新父menu的优先级
                    menuDao.edit(firstMenu);
                }
            }
            //设置新增菜单的path
            int size = firstMenuList.size();
            String newPath = generatePath(size, firstMenuList);
            menu.setPath(newPath);
            //添加该菜单
            menuDao.add(menu);
            //添加与该菜单关联的roleIds
            if (roleIds != null && roleIds.length > 0) {
                for (Integer roleId : roleIds) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("menuId", menu.getId());
                    map.put("roleId", roleId);
                    menuDao.setMenuAndRole(map);
                }
            }
        } else {
            //添加的是二级菜单
            //新增菜单的父菜单id
            Integer parentMenuId = menu.getParentMenuId();
            //根据父菜单id查询父菜单
            Menu fuMenu = menuDao.findById(parentMenuId);
            //获取父菜单的path和level
            String fuPath = fuMenu.getPath();
            //查询子菜单的集合
            List<Menu> childrenByParentId = menuDao.findChildrenByParentId(parentMenuId);
            //设置新建菜单的priority 和 path
            if (childrenByParentId != null && childrenByParentId.size() > 0) {
                for (Menu ziMenu : childrenByParentId) {
                    //获取子菜单的优先级
                    Integer priorityExist = ziMenu.getPriority();
                    if (priorityExist >= priority) {
                        //更新zimenu的优先级
                        ziMenu.setPriority(priorityExist + 1);
                        //更新子菜单的path
                        String ziPath = "/" + fuPath + "-" + (ziMenu.getPriority());
                        ziMenu.setPath(ziPath);
                        //更新子菜单
                        menuDao.edit(ziMenu);
                    }
                }
            }
            String path = "/" + fuPath + "-" + (menu.getPriority());
            menu.setPath(path);
            //添加该菜单
            menuDao.add(menu);
            //添加与该菜单关联的roleIds
            if (roleIds != null && roleIds.length > 0) {
                for (Integer roleId : roleIds) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("menuId", menu.getId());
                    map.put("roleId", roleId);
                    menuDao.setMenuAndRole(map);
                }
            }
        }
    }

    /**
     * @Description: 产生新路径
     * @Param: [firstMenuList]
     * @Return: java.lang.String
     * @Author: Wangqibo
     * @Date: 2020/7/26/0026
     */
    private String generatePath(Integer count, List<Menu> firstMenuList) {
        String newPath = String.valueOf(count + 1);
        for (Menu menu : firstMenuList) {
            if (menu.getPath().equals(newPath)) {
                count++;
                newPath = generatePath(count, firstMenuList);
            }
        }
        return newPath;
    }

    /**
     * @Description: 删除菜单及其关联的角色关系
     * @Param: [id]
     * @Return: void
     * @Author: Wangqibo
     * @Date: 2020/7/24/0024
     */
    @Override
    public void deleteMenuAndRelWithRole(Integer id) {
        //查询是否有子菜单
        List<Menu> children = menuDao.findChildrenByParentId(id);
        if (children != null && children.size() > 0) {
            throw new RuntimeException(MessageConstant.DELETE_MENU_FAIL_CHILDREN);
        }
        //删除中间表关系
        menuDao.deleteRoleMenuIdByMenuId(id);
        //删除菜单信息
        menuDao.deleteById(id);
    }

    /**
     * @Description: 获取所有父菜单
     * @Param: []
     * @Return: java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     * @Author: Wangqibo
     * @Date: 2020/7/25/0025
     */
    @Override
    public List<Map<String, Object>> getParentMenu() {
        List<Map<String, Object>> resultList = new ArrayList<>();
        //封装所有一级菜单
        Map<String, Object> map1 = new HashMap<>();
        map1.put("label", "一级菜单");
        List<Menu> menuList4One = menuDao.findMenuByLevel(1);
        if (menuList4One != null && menuList4One.size() > 0) {
            List<Map<String, Object>> mapList = new ArrayList<>();
            for (Menu menu : menuList4One) {
                Map<String, Object> map = new HashMap<>();
                map.put("menuId", menu.getId());
                map.put("menuName", menu.getName());
                mapList.add(map);
            }
            map1.put("options", mapList);
        }
        resultList.add(map1);
        //resultList.add(map2);
        return resultList;
    }

    /**
     * 分页
     */
    @Override
    public PageResult findPage(Integer currentPage, Integer pageSize, String queryString) {
        //第一步：设置分页参数
        PageHelper.startPage(currentPage, pageSize);
        //第二步：查询数据库（代码一定要紧跟设置分页代码  没有手动分页 select * from table where name = 'xx'  ）
        Page<Menu> menuPage = menuDao.selectByCondition(queryString);
        return new PageResult(menuPage.getTotal(), menuPage.getResult());
    }

    /**
     * 根据菜单id查询菜单
     */
    @Override
    public Menu findById(Integer id) {
        return menuDao.findById(id);
    }

    /**
     * 根据菜单id 查询角色id
     */
    @Override
    public List<Integer> findRoleIdsByMenuId(Integer menuId) {
        return menuDao.findRoleIdsByMenuId(menuId);
    }

    /**
     * 编辑菜单
     */
    @Override
    public void edit(Menu menu, Integer[] roleIds) {
        //1.先根据菜单id从菜单角色中间表 删除关系数据
        menuDao.deleteRoleMenuIdByMenuId(menu.getId());
        //2.根据页面传入的角色id 和 菜单重新建立关系
        if (roleIds != null && roleIds.length > 0) {
            for (Integer roleId : roleIds) {
                setMenuAndRole(menu.getId(), roleId);
            }
        }

        //3 更新菜单表数据
        //获取数据库原先菜单信息
        Menu menuInDb = menuDao.findById(menu.getId());
        Integer priority = menu.getPriority();
        //判断编辑的是几级菜单
        if (menuInDb.getLevel()==1) {
            //编辑一级菜单
            List<Menu> firstMenuList = menuDao.findfirstMenu();
            //a)	判断名称有没有更改
            if (!menuInDb.getName().equals(menu.getName())) {
                //b)名称改变，判断有没有和其他一级菜单重名，重名返回运行时异常
                for (Menu firstMenu1 : firstMenuList) {
                    if (firstMenu1.getName().equals(menu.getName())) {
                        //有重名的一级菜单
                        throw new RuntimeException(MessageConstant.ADD_MENU_FAIL3);
                    }
                }
            }
            //c)名称没变，或没有重名，更改优先级
            for (Menu firstMenu : firstMenuList) {
                Integer priorityExist = firstMenu.getPriority();
                if (priorityExist >= priority) {
                    firstMenu.setPriority(priorityExist + 1);
                    //更新父menu的优先级
                    menuDao.edit(firstMenu);
                }
            }
            //d)更新其他信息
            menuDao.edit(menu);
        } else {
            //编辑二级菜单
            List<Menu> secondMenuList = menuDao.findMenuByLevel(2);
            //a)判断名称有没有更改
            if (!menuInDb.getName().equals(menu.getName())) {
                //名称改变，判断有没有和其他二级菜单重名，重名返回运行时异常
                for (Menu secondMenu1 : secondMenuList) {
                    if (secondMenu1.getName().equals(menu.getName())) {
                        //有重名的二级菜单
                        throw new RuntimeException(MessageConstant.ADD_MENU_FAIL3);
                    }
                }
            }
            //名称没变，或没有重名
            //获取目标父菜单及其子菜单
            Menu targerParentMenu = menuDao.findById(menu.getParentMenuId());
            List<Menu> children = null;
            if (targerParentMenu != null) {
                 children = menuDao.findChildrenByParentId(targerParentMenu.getId());
            }
            //更改目标父菜单子菜单集合的优先级和path
            if (children != null && children.size() > 0) {
                for (Menu ziMenu : children) {
                    //获取子菜单的优先级
                    Integer priorityExist = ziMenu.getPriority();
                    if (priorityExist >= priority) {
                        //更新zimenu的优先级
                        ziMenu.setPriority(priorityExist + 1);
                        //更新子菜单的path
                        String ziPath = "/" + targerParentMenu.getPath() + "-" + (ziMenu.getPriority());
                        ziMenu.setPath(ziPath);
                        //更新子菜单
                        menuDao.edit(ziMenu);
                    }
                }
            }
            //设置本次编辑菜单的path
            String path = null;
            if (menu.getLevel() == 1) {
                List<Menu> firstMenuList = menuDao.findfirstMenu();
                //更新一级菜单的优先级
                for (Menu firstMenu : firstMenuList) {
                    Integer priorityExist = firstMenu.getPriority();
                    if (priorityExist >= priority) {
                        firstMenu.setPriority(priorityExist + 1);
                        //更新父menu的优先级
                        menuDao.edit(firstMenu);
                    }
                }
                //设置修改菜单的path
                int size = firstMenuList.size();
                path = generatePath(size, firstMenuList);
            } else {
                path = "/" + targerParentMenu.getPath() + "-" + (menu.getPriority());
            }
            menu.setPath(path);
            //更新其他信息
            menuDao.edit(menu);
        }
    }

    /**
     * 根据菜单id删除菜单
     */
    @Override
    public void deleteById(Integer id) {
        //1.根据菜单id查询菜单角色中间表（count(*)）
        int count1 = menuDao.findCountRoleByMenuId(id);
        if (count1 > 0) {
            throw new RuntimeException(MessageConstant.DELETE_MENU_FAIL2);
        }
        //2.根据菜单id删除记录
        menuDao.deleteById(id);
    }

    /**
     * 查询所有菜单
     */
    @Override
    public List<Menu> findAll() {
        return menuDao.findAll();
    }


    /**
     * 设置菜单和角色中间表
     */
    private void setMenuAndRole(Integer menuId, Integer roleId) {
        if (roleId != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("roleId", roleId);
            map.put("menuId", menuId);
            menuDao.setMenuAndRole(map);
        }
    }

    /**
     * @Description: 删除菜单及其子菜单
     * @Param: [id]
     * @Return: void
     * @Author: Wangqibo
     * @Date: 2020/7/25/0025
     */
    @Override
    public void deleteMenuAndChildren(Integer id) {
        //获取该菜单的所有子菜单
        List<Menu> children = menuDao.findChildrenByParentId(id);
        //删除子菜单及其与角色的关联关系
        for (Menu child : children) {
            deleteMenuAndRelWithRole(child.getId());
        }
        //删除该菜单及其与角色的关联关系
        deleteMenuAndRelWithRole(id);
    }
}




