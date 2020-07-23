package com.itheima.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.itheima.dao.MembershipStatisticsDao;
import com.itheima.service.MembershipStatisticsService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 会员统计图
 */
@Service(interfaceClass = MembershipStatisticsService.class)
@Transactional
public class MembershipStatisticsServiceImpl implements MembershipStatisticsService {

    @Autowired
    private MembershipStatisticsDao membershipStatisticsDao;
    /**
     * 会员性别 数量饼形图
     * @return
     */
    @Override
    public Map<String, Object> sexRatio() {
        //创建map
        Map<String,Object> map=new HashMap<>();
        List<String> list=new ArrayList<>();
        //表示性别
        list.add("男");
        list.add("女");
        map.put("age",list);
        //数据库1表示男 2表示女 性别和性别占数
        Integer nan=membershipStatisticsDao.getSexNumber(1);
        Integer nv=membershipStatisticsDao.getSexNumber(2);
        List<Map<String,Object>> list1=new ArrayList<>();
        Map<String,Object> map1=new HashMap<>();
        map1.put("name","男");
        map1.put("value",nan);
        Map<String,Object> map2=new HashMap<>();
        map2.put("name","女");
        map2.put("value",nv);
        list1.add(map1);
        list1.add(map2);
        //会员性别占比
        map.put("sexRatio",list1);
        return map;
    }

    /**
     * 会员年龄段  数量饼形图
     * @return
     */
    @Override
    public Map<String, Object> ageGroup() {
        //创建map
        Map<String,Object> map=new HashMap<>();
        //年龄段自己设计
        List<String> list=new ArrayList<>();
        list.add("0-18");
        list.add("18-30");
        list.add("30-60");
        list.add("60以上");
        //年龄段集合
        map.put("gender",list);
        //根据年龄段查询会员年龄段人数 因为有两个参数所有放到map中
        Map<String,Integer> map1=new HashMap<>();
        //数据库只有出生日期所有根据现在日期查询年龄段
        //获得当前年月日格式yyyy-MM-dd
        Calendar calendar = Calendar.getInstance();
        String format0 = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
        //18年前
        calendar.add(Calendar.YEAR,-18);
        String format18 = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());

        //将年龄段人数存入map再插入list
        List<Map<String,Object>> list1=new ArrayList<>();
        Map<String,Object> map2=new HashMap<>();
        //调用Dao查询各个年龄段人数0-18
        Integer number = getNumber(format0, format18);
        map2.put("name","0-18");
        map2.put("value",number);
        //年龄段自己设计
        list1.add(map2);

        //30年前
        calendar.add(Calendar.YEAR,-30+18);
        String format30 = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
        Map<String,Object> map3=new HashMap<>();
        //调用Dao查询各个年龄段人数18-30
        Integer number2 = getNumber(format18, format30);
        map3.put("name","18-30");
        map3.put("value",number2);
        //年龄段自己设计
        list1.add(map3);
        //60年前
        calendar.add(Calendar.YEAR,-60+30);
        String format60 = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
        Map<String,Object> map4=new HashMap<>();
        //调用Dao查询各个年龄段人数18-30
        Integer number3 = getNumber(format30, format60);
        map4.put("name","30-60");
        map4.put("value",number3);
        //年龄段自己设计
        list1.add(map4);
        Map<String,Object> map5=new HashMap<>();
        //调用Dao查询各个年龄段人数18-30
        Integer numberOfAgeGroups1 = membershipStatisticsDao.getNumberOfAgeGroups2(format60);
        map5.put("name","60以上");
        map5.put("value",numberOfAgeGroups1);
        //年龄段自己设计
        list1.add(map5);

        //调用Dao查询各个年龄段人数
//        Integer nianld1=membershipStatisticsDao.getNumberOfAgeGroups1();
//        Integer nianld2=membershipStatisticsDao.getNumberOfAgeGroups2();

        map.put("gender",list);
        map.put("sexRatio",list1);
        return map;
    }
    public Integer getNumber(String s1,String s2){
        Map<String,String> dateMap=new HashMap<>();
        dateMap.put("qian",s1);
        dateMap.put("ho",s2);
        Integer nianld1=membershipStatisticsDao.getNumberOfAgeGroups1(dateMap);
        return nianld1;
    }
}
