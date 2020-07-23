package com.itheima.dao;

import com.itheima.pojo.OrderSetting;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 预约设置持久接口
 */
public interface OrderSettingDao {
    /**
     * 根据可预约日期 查询记录是否存在
     * @param orderDate
     * @return
     */
    int findCountByOrderDate(Date orderDate);

    /**
     * 根据预约日期修改可预约人数
     * @param orderSetting
     */
    void editNumberByOrderDate(OrderSetting orderSetting);

    /**
     * 插入预约设置
     * @param orderSetting
     */
    void add(OrderSetting orderSetting);

    /**
     * 根据年月查询t_ordersetting表
     * @param map
     * @return
     */
    List<OrderSetting> getOrderSettingByYearMonth(Map<String, String> map);

    /**
     * 判断当前日期是否可以预约
     * @param orderDateNew
     * @return
     */
    OrderSetting findByOrderDate(Date orderDateNew);

    /**
     * 根据预约日期修改已经预约人数
     * @param orderSetting
     */
    void editReservationsByOrderDate(OrderSetting orderSetting);


    /**
     * 根据时间,获取当前日期之前的预约设置对象集合
     * @param date
     * @return
     */
    List<OrderSetting> getHostoricalDate(Date  date);

    /**
     * 备份数据到备份表t_ordersetting_bak中
     * @param orderSetting
     */
    void saveOrderSetting(OrderSetting orderSetting);

    /**
     * 删除t_ordersetting表的历史数据
     * @param date
     */
    void deleteOrderSetting(Date  date);
}