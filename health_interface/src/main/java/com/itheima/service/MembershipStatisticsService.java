package com.itheima.service;

import java.util.Map;

/**
 * 会员统计图
 */
public interface MembershipStatisticsService {

    /**
     * 会员性别 数量饼形图
     * @return
     */
    Map<String, Object> sexRatio();

    /**
     * 会员年龄段  数量饼形图
     * @return
     */
    Map<String, Object> ageGroup();
}
