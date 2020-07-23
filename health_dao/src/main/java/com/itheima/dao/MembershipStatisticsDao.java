package com.itheima.dao;

import java.util.Map;

/**
 * 会员统计图
 */
public interface MembershipStatisticsDao {
    /**
     * 查询会员不同性别人数
     * @param i
     * @return
     */
    Integer getSexNumber(int i);

    /**
     * 根据年龄段查询人数1
     * @param dateMap
     * @return
     */
    Integer getNumberOfAgeGroups1(Map<String, String> dateMap);

    /**
     * 根据年龄段查询人数2
     * @param format60
     * @return
     */
    Integer getNumberOfAgeGroups2(String format60);
}
