package com.itheima.jobs;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.service.OrderSettingService;
import org.junit.Test;
import org.springframework.stereotype.Component;

import java.text.ParseException;
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


    //@Test
    /*public void Test01() throws ParseException {
        Date date = new Date();
        System.out.println("当前：　"+date);

        long nowTime = date.getTime();
        System.out.println("当前毫秒：　"+nowTime);
        long beforeTime = nowTime - (1000L * 60L * 60L * 24L * 30L);
        System.out.println("30天之前的毫秒：　" + beforeTime);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String format = simpleDateFormat.format(beforeTime);
        System.out.println("30天之前的日期：　"+format);
    }*/
}
