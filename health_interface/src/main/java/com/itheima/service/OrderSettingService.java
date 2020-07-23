package com.itheima.service;

import com.itheima.pojo.OrderSetting;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 预约设置服务接口
 */
public interface OrderSettingService {
    /**
     * 批量预约设置
     * @param orderSettingList
     */
    void batchAdd(List<OrderSetting> orderSettingList);
    /**
     * 根据年月 查询预约设置数据List<Map></Map>
     */
    List<Map> getOrderSettingByYearMonth(String yearMonth);
    /**
     * 单个预约设置（根据预约日期修改可预约人数）
     */
    void editNumberByDate(OrderSetting orderSetting);


    /**
     * 清理预约信息的历史数据,并备份数据到备份表t_ordersetting_bak中
     * (在数据库中新建了t_ordersetting_bak表用于存储历史数据)
     * @param date
     */
    void clearOrderSetingJob(Date date);
}
