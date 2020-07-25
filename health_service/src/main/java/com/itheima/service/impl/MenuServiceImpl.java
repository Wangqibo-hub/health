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
        if (byMenuName!=null) {
            throw new RuntimeException("该菜单名称已经存在");
        }
        Integer parentMenuId = menu.getParentMenuId();
        Integer priority = menu.getPriority();
        if (parentMenuId==null) {
            //添加1级菜单 判断1级菜单的优先级
            List<Menu> menuList =menuDao.findfirstMenu();
            //查询已存在1级菜单的优先级
            for (Menu fuMenu : menuList) {
                Integer priorityExist = fuMenu.getPriority();
                if (priorityExist >= priority) {
                    fuMenu.setPriority(priorityExist+1);
                    //更新父menu的优先级
                    menuDao.edit(fuMenu);
                }
            }
            //添加该菜单
            menuDao.add(menu);
        }else {
            //添加子菜单
            //根据父菜单id查询父菜单
            Menu fuMenu = menuDao.findById(parentMenuId);
            //获取父菜单的path和level
            String fuPath = fuMenu.getPath();
            Integer fuLevel = fuMenu.getLevel();
            //设置新建菜单level
            menu.setLevel(fuLevel+1);
            //设置新建菜单的priority 和 level
            //查询子菜单的集合
            List<Menu> childrenByParentId = menuDao.findChildrenByParentId(parentMenuId);
            for (Menu ziMenu : childrenByParentId) {
                //获取子菜单的优先级
                Integer priorityExist = ziMenu.getPriority();
                if (priorityExist >= priority) {
                    //更新zimenu的优先级
                    ziMenu.setPriority(priorityExist+1);
                    //更新子菜单的path
                    String ziPath = "/" + fuPath + "-" + (ziMenu.getPriority());
                    ziMenu.setPath(ziPath);
                    //更新子菜单
                    menuDao.edit(ziMenu);
                    //子菜单的子菜单的path修改
                    //先查询出所有子菜单的子菜单
                    List<Menu> ziZiMenuList = menuDao.findChildrenByParentId(ziMenu.getId());
                    if (ziZiMenuList!=null && ziZiMenuList.size()>0) {
                        for (Menu ziZiMenu : ziZiMenuList) {
                            //更新子子菜单的path
                            String ziZiPath = "/" + ziPath + "-" + (ziZiMenu.getPriority());
                            ziZiMenu.setPath(ziZiPath);
                            menuDao.edit(ziZiMenu);
                        }
                    }
                }
                String path = "/" + fuPath + "-" + (menu.getPriority());
                menu.setPath(path);

                //添加该菜单
                menuDao.add(menu);
        }
        }
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
        //删除中间表关系
        menuDao.deleteRoleMenuIdByMenuId(id);
        //删除菜单信息
        menuDao.deleteById(id);
    }

    /**
    * @Description: 获取所有父菜单
    * @Param: []
    * @Return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
    * @Author: Wangqibo
    * @Date: 2020/7/25/0025
    */
    @Override
    public List<Map<String, Object>> getParentMenu() {
        List<Map<String,Object>> resultList = new ArrayList<>();
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
        //封装所有二级菜单
        List<Menu> menuList4Two = menuDao.findMenuByLevel(2);
        Map<String, Object> map2 = new HashMap<>();
        map2.put("label", "二级菜单");
        if (menuList4Two != null && menuList4Two.size() > 0) {
            List<Map<String, Object>> mapList = new ArrayList<>();
            for (Menu menu : menuList4Two) {
                Map<String, Object> map = new HashMap<>();
                map.put("menuId", menu.getId());
                map.put("menuName", menu.getName());
                mapList.add(map);
            }
            map2.put("options", mapList);
        }
        resultList.add(map1);
        resultList.add(map2);
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
        //获取数据库中该菜单的信息
        Menu menuInDb = menuDao.findById(menu.getId());
        //获取数据库父菜单id
        Integer parentMenuId = menuInDb.getParentMenuId();
        //获取目标父菜单
        Menu targetParentMenu = menuDao.findById(menu.getParentMenuId());
        if (parentMenuId != null) {
            //编辑的是子菜单
            //查询要编辑的菜单是否有子菜单
            List<Menu> childrenByParentId = menuDao.findChildrenByParentId(menu.getId());
            if (childrenByParentId != null && childrenByParentId.size() > 0) {
                //要编辑的菜单有子菜单
                //a 先处理自身
                changeChildMenu(targetParentMenu, menu);
                //b 再处理子菜单
                for (Menu child : childrenByParentId) {
                    changeChildMenu(menu, child);
                }
            } else {
                //要编辑的菜单没有子菜单
                changeChildMenu(targetParentMenu, menu);
            }
        }else {
            //编辑的是一级菜单
            changeChildMenu(targetParentMenu, menu);
            //查询要编辑的菜单是否有子菜单
            List<Menu> childrenByParentId = menuDao.findChildrenByParentId(menu.getId());
            if (childrenByParentId != null && childrenByParentId.size() > 0) {
                //要编辑的菜单有子菜单
                //a 先处理自身
                changeChildMenu(targetParentMenu, menu);
                //b 再处理子菜单
                for (Menu child : childrenByParentId) {
                    changeChildMenu(menu, child);
                }
            } else {
                //要编辑的菜单没有子菜单
                changeChildMenu(targetParentMenu, menu);
            }
        }
    }

    /**
     * @Description: 修改子菜单的方法
     * @Param: [parent, child]
     * @Return: void
     * @Author: Wangqibo
     * @Date: 2020/7/25/0025
     */
    public void changeChildMenu(Menu parent, Menu child) {
        //获取父菜单的path
        String parentMenuPath = parent.getPath();
        //根据父id查询leave 获得父leavel
        Integer targetParentMenuLevel = parent.getLevel();
        //设置该level menu.setlevel (fuleavel +1),
        child.setLevel(targetParentMenuLevel + 1);
        //获取该父菜单的所有子菜单
        List<Menu> menuChildren = parent.getChildren();
        if (menuChildren != null && menuChildren.size() > 0) {
            //该父菜单有子菜单,更新子菜单信息
            //判断被编辑的菜单是否在父菜单的子菜单中
            Menu contain = isContain(menuChildren, child);
            if (contain != null) {
                //父菜单的子菜单中包含被编辑的菜单
                if (contain.getPriority()!=child.getPriority()) {
                    //优先级改变了，更新优先级
                    for (Menu menuChild : menuChildren) {
                        Integer childPriority = menuChild.getPriority();
                        if (childPriority >= child.getPriority()) {
                            //更新优先级
                            menuChild.setPriority(childPriority + 1);
                            //更新path
                            String childPath = "/" + parentMenuPath + "-" + (childPriority + 1);
                            menuChild.setPath(childPath);
                            menuDao.edit(menuChild);
                        }
                    }
                }
            }else {
                //父菜单的子菜单中不包含被编辑的菜单
                for (Menu menuChild : menuChildren) {
                    Integer childPriority = menuChild.getPriority();
                    if (childPriority >= child.getPriority()) {
                        //更新优先级
                        menuChild.setPriority(childPriority + 1);
                        //更新path
                        String childPath = "/" + parentMenuPath + "-" + (childPriority + 1);
                        menuChild.setPath(childPath);
                        menuDao.edit(menuChild);
                    }
                }
            }
        }
        //插入新菜单
        String nowPath = "/" + parentMenuPath + "-" + child.getPriority();
        child.setPath(nowPath);
        menuDao.edit(child);
    }

    //子菜单是否包含被编辑的菜单
    public Menu isContain(List<Menu> menuList, Menu menu){
        for (Menu menu1 : menuList) {
            if (menu.getId() == menu1.getId()) {
                return menu1;
            }
        }
        return null;
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
}




