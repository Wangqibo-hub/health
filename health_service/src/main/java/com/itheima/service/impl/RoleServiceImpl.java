package com.itheima.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.constant.MessageConstant;
import com.itheima.dao.RoleDao;
import com.itheima.entity.PageResult;
import com.itheima.pojo.Role;
import com.itheima.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 包名:com.itheima.service.impl
 * 作者:Jiaming
 * 日期2020-07-22   11:43
 */
@Service(interfaceClass = RoleService.class)
@Transactional
public class RoleServiceImpl implements RoleService {
    @Autowired
    private RoleDao roleDao;
    /**
     * 检查分页查询
     */
    @Override
    public PageResult findPage(Integer currentPage, Integer pageSize, String queryString) {
//第一步：设置分页参数
        PageHelper.startPage(currentPage,pageSize);
        //第二步：查询数据库（代码一定要紧跟设置分页代码  没有手动分页 select * from table where name = 'xx'  ）
        Page<Role> rolePage= roleDao.selectByCondition(queryString);
        return new PageResult(rolePage.getTotal(),rolePage.getResult());
    }
    /**
     * 根据角色id查询角色
     */
    @Override
    public Role findById(Integer roleId) {
        return roleDao.findById(roleId);
    }
    /**
     * 根据角色id 查询菜单项ids
     */
    @Override
    public List<Integer> findMenuIdsByRoleId(Integer roleId) {
        //查询中间表,得到菜单项ids
        return roleDao.findMenuIdsByRoleId(roleId);
    }
    /**
     * 根据角色id删除
     */
    @Override
    public void deleteById(Integer id) {
        //先判断要删除的角色 是否有关联关系
        //如果有,则不删除
        //如果没有,则直接删除
        //1.根据角色id查询角色/菜单中间表（count(*)）
        int count1 = roleDao.findCountMenuByMenuId(id);
        if(count1>0){
            throw new RuntimeException(MessageConstant.DELETE_MENU_FAIL2);
        }
        //2.如果第三步关系不存在，根据角色id查询权限数据（count(*)）
        int count2 = roleDao.findCountPermissionByRoleId(id);
        if(count2>0){
            throw new RuntimeException(MessageConstant.DELETE_PERMISSION_FAIL);
        }
        //3.如果第三步关系不存在，根据角色id查询用户（count(*)）
        int count3 = roleDao.findCountUserByRoleId(id);
        if(count3>0){
            throw new RuntimeException(MessageConstant.DELETE_USER_FAIL);
        }
        roleDao.deleteById(id);
    }
    /**
     * 查询所有角色
     */
    @Override
    public List<Role> findAll() {
        return roleDao.findAll();
    }
    /**
     * 新增角色
     */
    @Override
    public void add(Role role, Integer[] menuIds, Integer[] permissionIds) {
        //判断数据库是否已经存在该角色
        int count = roleDao.findRoleExist(role);
        if (count>0) {
            throw new RuntimeException(MessageConstant.ADD_ROLE_FAIL1);
        }
        //第一步：保存角色表
        roleDao.add(role);
        //第二步：获取角色id
        Integer roleId = role.getId();
        //第三步：往角色组菜单中间表 遍历插入关系数据
        setRoleAndMenu(roleId,menuIds);
        //第四步：往角色组权限中间表 遍历插入关系数据
        setRoleAndPermission(roleId,permissionIds);
    }
    private void setRoleAndMenu(Integer roleId, Integer[] menuIds) {
        if(menuIds != null && menuIds.length>0){
            for (Integer menuId : menuIds) {
                Map<String,Object> map = new HashMap<>();
                map.put("menuId",menuId);
                map.put("roleId",roleId);
                roleDao.setRoleAndMenu(map);
            }
        }
    }
    private void setRoleAndPermission(Integer roleId, Integer[] permissionIds) {
        if(permissionIds != null && permissionIds.length>0){
            for (Integer permissionId : permissionIds) {
                Map<String,Object> map = new HashMap<>();
                map.put("permissionId",permissionId);
                map.put("roleId",roleId);
                roleDao.setRoleAndPermission(map);
            }
        }
    }
    /**
     * 编辑角色
     */
    @Override
    public void edit(Role role, Integer[] menuIds, Integer[] permissionIds) {
        //1.先根据角色id从角色/菜单中间表 删除关系数据
        roleDao.deleteRelByRoleById(role.getId());
        //2.先根据角色id从角色/权限中间表 删除关系数据
        roleDao.deletePermissionRelByRoleById(role.getId());
        //3.根据页面传入的菜单ids 和 角色重新建立关系
        setRoleAndMenu(role.getId(),menuIds);
        //4.根据页面传入的菜单ids 和 角色重新建立关系
        setRoleAndPermission(role.getId(),permissionIds);
        //5.根据角色id 更新角色数据
        roleDao.edit(role);
    }
    /**
     * 根据角色id 查询权限项ids
     */
    @Override
    public List<Integer> findPermissionIdsByRoleId(Integer roleId) {
        return roleDao.findPermissionIdsByRoleId(roleId);
    }
}
