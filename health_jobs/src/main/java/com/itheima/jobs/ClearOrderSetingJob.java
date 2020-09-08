package com.itheima.jobs;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.service.OrderSettingService;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Component
public class ClearOrderSetingJob {

    @Reference
    private OrderSettingService orderSettingService;

    public void clearOrderSetingJob() throws Exception {
        System.out.println("orderSettingService" + "执行了");
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH,-30);
        String format1 = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
        orderSettingService.clearOrderSetingJob(format1);
    }
}
