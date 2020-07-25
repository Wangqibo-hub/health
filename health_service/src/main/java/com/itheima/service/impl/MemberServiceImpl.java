package com.itheima.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.itheima.dao.MemberDao;
import com.itheima.pojo.Member;
import com.itheima.service.MemberService;
import com.itheima.utils.MD5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 会员服务实现
 */
@Service(interfaceClass = MemberService.class)
@Transactional
public class MemberServiceImpl implements MemberService {
    @Autowired
    private MemberDao memberDao;
    /**
     * 根据手机号码查询会员信息
     * @param telephone
     * @return
     */
    @Override
    public Member findByTelephone(String telephone) {
        return memberDao.findByTelephone(telephone);
    }
    /**
     * 自动注册方法
     * @param member
     */
    @Override
    public void add(Member member) {
        //密码
        String password = member.getPassword();
        if(!StringUtils.isEmpty(password)){
            //加密 MD5
            String md5Pwd  = MD5Utils.md5(password);
            member.setPassword(md5Pwd);
        }
        memberDao.add(member);
    }
    /**
     * 会员数量折线图
     */
    @Override
    public Map<String, Object> getMemberReport() {
        //定义返回结果Map
        Map<String,Object> rsMap = new HashMap<>();

        //1.key:months—[2020-01,2020-02…最近一年]  --List<String>
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH,-12);
        //遍历获取每一个月年月 ，并将年月放入List<String>集合中
        List<String>  months = new ArrayList<>();
        for (int i = 0;i<=12;i++){
            months.add(new SimpleDateFormat("yyyy-MM").format(calendar.getTime()));
            calendar.add(Calendar.MONTH,1);
        }

        rsMap.put("months",months);

        //key:memberCount—[50,100,…最近一年]       List<Integer>
        //select count(*) from t_member where regTime <= ‘2020-01-31’
        List<Integer>  memberCount = new ArrayList<>();
        if(months !=null && months.size()>0){
            for (String month : months) {
                //month 2020-07 ==> 2020-07-31
               String newMonth = month + "-31";
                Integer mc = memberDao.findMemberCountBeforeDate(newMonth);
                memberCount.add(mc);
            }
        }
        rsMap.put("memberCount",memberCount);
        return rsMap;
    }




    public static void main(String[] args) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH,-12);
        //遍历获取每一个月年月 ，并将年月放入List<String>集合中
        List<String>  months = new ArrayList<>();
        for (int i = 1;i<=12;i++){
            months.add(new SimpleDateFormat("yyyy-MM").format(calendar.getTime()));
            calendar.add(Calendar.MONTH,1);
        }
        System.out.println("************************");
    }

    //输入日期展示会员数量折线图
    @Override
    public Map<String, Object> getMemberReportByDate(List<Date> value1) {
        //定义返回结果Map
        Map<String,Object> rsMap = new HashMap<>();

        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(value1.get(0));
        c2.setTime(value1.get(1));
        int year1 = c1.get(Calendar.YEAR);
        int year2 = c2.get(Calendar.YEAR);
        int month1 = c1.get(Calendar.MONTH);
        int month2 = c2.get(Calendar.MONTH);
        int day1 = c1.get(Calendar.DAY_OF_MONTH);
        int day2 = c2.get(Calendar.DAY_OF_MONTH);
        // 获取年的差值
        int yearInterval = year1 - year2;
        // 如果 d1的 月-日 小于 d2的 月-日 那么 yearInterval-- 这样就得到了相差的年数
        if (month1 < month2 || month1 == month2 && day1 < day2) {
            yearInterval--;
        }
        // 获取月数差值
        int monthInterval = (month1 + 12) - month2;
        if (day1 < day2) {
            monthInterval--;
        }
        monthInterval %= 12;
        int monthsDiff = Math.abs(yearInterval * 12 + monthInterval);
        //遍历获取每一个月年月 ，并将年月放入List<String>集合中
        List<String>  months = new ArrayList<>();
        for (int i =0;i<=monthsDiff;i++){
            months.add(new SimpleDateFormat("yyyy-MM").format(c1.getTime()));
            c1.add(Calendar.MONTH,1);
        }
        rsMap.put("months",months);
        List<Integer>  memberCount = new ArrayList<>();
        if(months !=null && months.size()>0){
            for (String month : months) {
                //month 2020-07 ==> 2020-07-31
                String newMonth = month + "-31";
                Integer mc = memberDao.findMemberCountBeforeDate(newMonth);
                memberCount.add(mc);
            }
        }
        rsMap.put("memberCount",memberCount);
        return rsMap;
    }

}
