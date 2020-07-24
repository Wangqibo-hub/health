package com.itheima.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.constant.MessageConstant;
import com.itheima.dao.PermissionDao;
import com.itheima.entity.PageResult;
import com.itheima.entity.QueryPageBean;
import com.itheima.entity.Result;
import com.itheima.pojo.Permission;
import com.itheima.pojo.Role;
import com.itheima.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 权限管理业务实现类
 */
@Service(interfaceClass = PermissionService.class)
@Transactional
public class PermissionServiceImpl implements PermissionService{

    @Autowired
    private PermissionDao permissionDao;
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

        //第一步：设置分页参数
        PageHelper.startPage(currentPage,pageSize);
        //第二步：查询数据库（代码一定要紧跟设置分页代码 ）
        Page<Permission> permissionPage =permissionDao.findPage(queryString);

        return new PageResult(permissionPage.getTotal(),permissionPage.getResult());
    }

    /**
     * 新增权限
     * 新增功能-新建权限之前先查询是否已有这个权限
     * @param permission
     */
    @Override
    public void add(Permission permission) {

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
    }

    /**
     * 编辑权限
     * @param permission
     */
    @Override
    public void edit(Permission permission) {
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
     * 删除权限
     * @param id
     */
    /*
    @Override
    public void deleteById(Integer id) {
        permissionDao.deleteById(id);
    }
    */
}
