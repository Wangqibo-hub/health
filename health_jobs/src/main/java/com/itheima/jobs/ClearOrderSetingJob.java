package com.itheima.jobs;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.service.OrderSettingService;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class ClearOrderSetingJob {

    @Reference
    private OrderSettingService orderSettingService;

    public void clearOrderSetingJob() throws Exception {
        System.out.println("orderSettingService" + "执行了");
        Date date = new Date();
        orderSettingService.clearOrderSetingJob(date);
    }
}
