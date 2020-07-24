package com.itheima.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.constant.MessageConstant;
import com.itheima.dao.PermissionDao;
import com.itheima.dao.RoleDao;
import com.itheima.entity.PageResult;
import com.itheima.entity.QueryPageBean;
import com.itheima.entity.Result;
import com.itheima.pojo.Permission;
import com.itheima.pojo.Role;
import com.itheima.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 权限管理业务实现类
 */
@Service(interfaceClass = PermissionService.class)
@Transactional
public class PermissionServiceImpl implements PermissionService{

    @Autowired
    private PermissionDao permissionDao;
    @Autowired
    private RoleDao roleDao;
    /**
     * 权限表分页查询
     * @param queryPageBean
     * @return
     */
    @Override
    public PageResult findPage(QueryPageBean queryPageBean) {
        //当前页（第几页）
        Integer currentPage = queryPageBean.getCurrentPage();
        //每页展示的数据条数
        Integer pageSize = queryPageBean.getPageSize();
        //查询条件
        String queryString = queryPageBean.getQueryString();

        if (queryString != null && queryString.length()>0){
            //根据条件查询权限数据的数量
            int count = permissionDao.findByCondition(queryString);
            if ((count/pageSize+1) < currentPage){
                currentPage = count/pageSize+1;
            }
        }

        //第一步：设置分页参数
        PageHelper.startPage(currentPage,pageSize);
        //第二步：查询数据库（代码一定要紧跟设置分页代码 ）
        Page<Permission> permissionPage =permissionDao.findPage(queryString);

        return new PageResult(permissionPage.getTotal(),permissionPage.getResult(),permissionPage.getPageNum());
    }


    /**
     * 新增权限
     * 新增功能-新建权限之前先查询是否已有这个权限
     * @param permission
     */
    @Override
    public void add(Permission permission,Integer[] roleIds) {

        String permissionName = permission.getName();
        String keyword = permission.getKeyword();
        //新增权限前先查询次权限是否已存在
        int count1 =permissionDao.findByKeyword(keyword);
        if (count1 > 0){
            throw new RuntimeException(MessageConstant.ADD_PERMISSION_FAIL3);
        }
        //新增权限前先查询此权限是否已存在
        int count = permissionDao.findByName(permissionName);
        if (count > 0){
            throw new RuntimeException(MessageConstant.ADD_PERMISSION_FAIL2);
        }
        //新增权限
        permissionDao.add(permission);
        Permission permission1 = permissionDao.findByPermissionName(permissionName);
        if (roleIds != null && roleIds.length > 0) {
            for (Integer roleId : roleIds) {
                Map<String, Object> map = new HashMap<>();
                map.put("roleId", roleId);
                map.put("permissionId", permission1.getId());
                roleDao.setRoleAndPermission(map);
            }
        }
    }

    /**
     * 编辑权限
     * @param permission
     */
    @Override
    public void edit(Permission permission,Integer[] roleIds) {
        //先删除该权限关联的角色ids
        permissionDao.deletePermissionRelRoleByPermissionId(permission.getId());
        //重新建立关系
        if (roleIds != null && roleIds.length > 0) {
            for (Integer roleId : roleIds) {
                Map<String, Object> map = new HashMap<>();
                map.put("roleId", roleId);
                map.put("permissionId", permission.getId());
                roleDao.setRoleAndPermission(map);
            }
        }
        //更新权限信息
        permissionDao.edit(permission);
    }

    /**
     * 编辑-回显权限
     * @param id
     * @return
     */
    @Override
    public Permission findById(Integer id) {
       return permissionDao.findById(id);
    }


    /**
     * 这里发生改变，新增查询角色表
     * 删除权限
     * @param id
     */
    @Override
    public Result deleteById(Integer id) {
        try {
            List<Role> roleList = permissionDao.findRoleByPermissionId(id);
            if (roleList != null && roleList.size()>0){
                return new Result(false, MessageConstant.DELETE_PERMISSION_FAIL);
            }
            permissionDao.deleteById(id);
            return new Result(true,MessageConstant.DELETE_PERMISSION_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,MessageConstant.DELETE_PERMISSION_FAIL2);
        }
    }
    /**
     * 查询所有权限
     * @param
     */
    @Override
    public List<Permission> findAll() {
        return permissionDao.findAll();
    }

    /**
    * @Description: 根据权限id查询角色
    * @Param: [permissionId]
    * @Return: java.util.List<com.itheima.pojo.Role>
    * @Author: Wangqibo
    * @Date: 2020/7/24/0024
    */
    @Override
    public List<Role> findRoleByPermissionId(Integer permissionId) {
        return permissionDao.findRoleByPermissionId(permissionId);
    }

    /**
     * 新增、编辑权限时异步校验权限名和权限码
     * @param permission
     * @return
     */
    @Override
    public Result verifyByPermission(Permission permission) {
        String ResName = permission.getName();
        String ResKeyword = permission.getKeyword();

        List<Permission> DatabasePermissionList = permissionDao.findAll();

        for (Permission DatabasePermission : DatabasePermissionList) {
            if (ResName != null && ResName.length() > 0){
                if (DatabasePermission.getName().equals(ResName)){
                    return new Result(false,MessageConstant.ADD_PERMISSION_FAIL2);
                }
            }

            if (DatabasePermission.getKeyword().equals(ResKeyword)){
                return new Result(false,MessageConstant.ADD_PERMISSION_FAIL3);
            }

        }
        return null;
    }

}
