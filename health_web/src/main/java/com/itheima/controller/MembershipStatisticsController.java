package com.itheima.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.constant.MessageConstant;
import com.itheima.entity.Result;
import com.itheima.service.MembershipStatisticsService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 会员统计图
 */
@RestController
@RequestMapping("member")
public class MembershipStatisticsController {

    @Reference
    private MembershipStatisticsService membershipStatisticsService;

    /**
     * 会员性别 数量饼形图
     * @return
     */
    @RequestMapping("sexRatio")
    public Result sexRatio(){
        try {
            Map<String,Object> map=membershipStatisticsService.sexRatio();

            return new Result(true, MessageConstant.GET_SEX_REPORT_SUCCESS,map);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, MessageConstant.GET_SEX_REPORT_FAIL);

        }
    }

    /**
     * 会员年龄段  数量饼形图
     * @return
     */
    @RequestMapping("ageGroup")
    public Result ageGroup(){

        try {
            Map<String,Object> map=membershipStatisticsService.ageGroup();
            return new Result(true, MessageConstant.GET_SEX_REPORT_SUCCESS,map);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, MessageConstant.GET_SEX_REPORT_FAIL);
        }
    }
}
