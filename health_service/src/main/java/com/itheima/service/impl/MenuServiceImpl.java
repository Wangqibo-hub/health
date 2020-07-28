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

import java.util.*;

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

        //先添加到数据库中去 再更新
        menuDao.add(menu);
        //获取添加后的menudb 获取id
        Menu menuDB = menuDao.findByMenuName(menuName);
        Integer id = menuDB.getId();
        //更新角色关联
        if (roleIds !=null && roleIds.length>0) {
            for (Integer roleId : roleIds) {
                Map<String, Object> map = new HashMap();
                map.put("menuId",id);
                map.put("roleId",roleId);
                menuDao.setMenuAndRole(map);
            }
        }
        //判断添加的是几级菜单
        Integer level = menu.getLevel();
        if (level == 1) {
            //添加的是一级菜单
           //获取当前所有一级菜单
            List<Menu> firstMenuList = menuDao.findFirstMenuExceptSelf(id);
            //更新优先级和path
            editListPathAndPriority(firstMenuList,menu,null);
        } else {
            //添加的是二级菜单
            //获取其同级子菜单
            Integer parentMenuId = menu.getParentMenuId();
            List<Menu> childrenByParentId = menuDao.findChildrenByParentId(parentMenuId);
            //更新优先级和path
            editListPathAndPriority(childrenByParentId,menu,parentMenuId);
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
        editListPathAndPriorityAfterDelete(id);
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
   * @Author: cz
   * @Description: 编辑菜单
   * @Param: [menu, roleIds]
   * @Return: void
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
        //判断菜单名 根据前端用户名查询后端  与自身除外
        Menu menuDB2 = menuDao.findByMenuName(menu.getName());
        if (menuDB2 != null && menuDB2.getId()!= menu.getId()) {
            //重复返回异常 不能重复 与自身除外
            throw new RuntimeException("菜单名已经存在 请重新输入");
        }
        //不重复查询出来为null继续{}
        //if.1 根据id查询menudb  查询优先级 判断编辑的菜单原始等级
        Menu menuDB =menuDao.findById(menu.getId());
        Integer level = menuDB.getLevel();
        Integer priority = menu.getPriority();
        Integer priorityDB = menuDB.getPriority();
        if (level==1) {
        //说明编辑是一级菜单 只能改优先级 不能改为别人的子菜单
            // 判断db和前端的优先级是否改变
            if (priorityDB==priority) {
                //if.1.1优先级没有改变 直接更新
                menuDao.edit(menu);
                return;
            }else {
                //if.1.2 //优先级发生改变
                //查询同级一级菜单 自身除外
               List<Menu> brotherList = menuDao.findFirstMenuExceptSelf(menu.getId());
                //更新数据库level 优先级和path方法： 按优先级排序 编辑设置优先级和path
                editListPathAndPriority(brotherList, menu,null);
            }
        }else{
            // if.2 查询数据库优先级 // 说明编辑的是二级菜单
            //判断父id是否发生改变
            Integer parentMenuId = menu.getParentMenuId();
            Integer parentMenuIdDB = menuDB.getParentMenuId();
            if (parentMenuIdDB == parentMenuId) {
               // 父id未改变 说明未更换父菜单 等级不需要改变
                //判断db和前端的优先级是否改变
                if (menuDB.getPriority() == menu.getPriority()){
                    //优先级没有改变 直接更新
                    menuDao.edit(menu);
                    return;
                }else{
                    //父id没有改变 仅仅优先级发生改变
                    //根据父id 查询子菜单 除自己外
                    List<Menu> brotherList = menuDao.findBrotherMenuExceptSelfByParentID(parentMenuIdDB,menu.getId());
                    //更新优先级和方法：集合按照优先级排序按优先级排序 编辑设置优先级和path
                    editListPathAndPriority(brotherList,menu,parentMenuIdDB);

                }
            }else {
                //父id改变  1 改为null  2 改为别的父id
                //处理剩下前同级子菜单 的优先级和path
                List<Menu> brotherList = menuDao.findBrotherMenuExceptSelfByParentID(parentMenuIdDB,menu.getId());
                //前同级子菜单优先级排序 编辑优先级和path 并更新到数据库
                editListPathAndPriority(brotherList,null,parentMenuIdDB);
                //处理自己
                if (parentMenuId != null) {
                    //说明二级菜单更换父菜单 仍然为二级菜单
                    // 查询出要插入的父菜单底下的子菜单
                    List<Menu> targetBrotherList = menuDao.findChildrenByParentId(parentMenuId);

                    //调用方法更新优先级 调用方法  //更新优先级和方法
                    editListPathAndPriority(targetBrotherList,menu,parentMenuIdDB);

                }else {
                    //父id为null 说明更新为了一级菜单
                    menu.setLevel(1);
                    //查询出所有一级菜单
                    List<Menu> firstMenuList = menuDao.findfirstMenu();
                    editListPathAndPriority(firstMenuList,menu,null);
                }
            }
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
        editListPathAndPriorityAfterDelete(id);
        //2.根据菜单id删除菜单
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
        editListPathAndPriorityAfterDelete(id);
        //删除该菜单及其与角色的关联关系
        deleteMenuAndRelWithRole(id);
    }


    /**
    * @Author: cz
    * @Description:  更新优先级和方法：按照优先级排序 设置优先级和path 并更新到数据库
    * @Param: [menuList, newMenu, parentMenuId]
    * @Return: void
    */
    public void editListPathAndPriority(List<Menu> menuList, Menu newMenu, Integer parentMenuId){
        //设置menu新增的属性 dpriority
        for (Menu menu1 : menuList) {
            menu1.setDpriority(Double.valueOf(menu1.getPriority()));
        }
        if (newMenu != null) {
            //优化自身优先级 便于排序
            newMenu.setDpriority(newMenu.getPriority()-0.5);
            menuList.add(newMenu);
        }
        Collections.sort(menuList, new Comparator<Menu>() {
            @Override
            public int compare(Menu o1, Menu o2) {
                return (int)((o1.getDpriority()-o2.getDpriority())*10);
            }
        });
        int p = 1;
        for (Menu menu : menuList) {
            System.out.println(menu.getName());
        }
        for (Menu menu : menuList) {
            //修改排序好的优先级
            menu.setPriority(p);
            menu.setDpriority(Double.valueOf(p));
            p++;
            //修改每个path
            String path = null;

            if (parentMenuId!=null) {
                Menu parentMenuDB = menuDao.findById(parentMenuId);
                String parentPath = parentMenuDB.getPath();
                path = "/"+parentPath+"-"+(p-1);
                menu.setPath(path);
            }else {
                path= p-1+"";
                menu.setPath(path);
            }
            menuDao.edit(menu);
        }
    }

    public void editListPathAndPriorityAfterDelete(Integer id){
        Menu menu = menuDao.findById(id);
        Integer level = menu.getLevel();
        if (level==1) {
            //删除一级菜单
            //获取所有一级菜单除自己外
            List<Menu> firstMenuExceptSelf = menuDao.findFirstMenuExceptSelf(menu.getId());
            // 更新一级菜单的优先级和路由
            editListPathAndPriority(firstMenuExceptSelf,null,null);

        }else{
            // 删除二级菜单
            // 查询同级菜单集合
            List<Menu> brotherMenu = menuDao.findBrotherMenuExceptSelfByParentID(menu.getParentMenuId(), menu.getId());
            // 更新同级菜单的优先级和路由
            editListPathAndPriority(brotherMenu,null,menu.getParentMenuId());
        }
    }
}




