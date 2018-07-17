package com.bigdata.xwork.scheduler.core.dispathcer;

import org.junit.Test;
import org.quartz.CronTrigger;
import org.springframework.scheduling.support.CronSequenceGenerator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Created by zouyi on 2018/3/6.
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(classes = XWorkApp.class)
//@WebAppConfiguration
public class FlowActionUtilsTest {
//    @Autowired
//    @SuppressWarnings("SpringJavaAutowiringInspection")
//    private FlowActionUtils flowActionUtils;

    @Test
    public void flowActionUtils() {
//        Date date = flowActionUtils.getDate("30 * * * * ? 2017");
//        System.out.println(date);
        CronTrigger trigger = newTrigger().withIdentity("123", "123123").withSchedule(cronSchedule("0/5 * * * * ? ")).build();
        String cronExpression = trigger.getCronExpression();
        System.out.println(cronExpression);
        Date endTime = trigger.getEndTime();
        Date startTime = trigger.getStartTime();
        TimeZone timeZone = trigger.getTimeZone();
        Date finalFireTime = trigger.getFinalFireTime();
        Date previousFireTime = trigger.getPreviousFireTime();
        Date nextFireTime = trigger.getNextFireTime();
        System.out.println(endTime);
        System.out.println(startTime);
        System.out.println(timeZone);
        System.out.println(finalFireTime);
        System.out.println(previousFireTime);
        System.out.println(nextFireTime);
        System.out.println("-----------------------");

        List<String> list = new ArrayList<>(20);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date nextTimePoint = new Date();
        CronSequenceGenerator cronSequenceGenerator = new CronSequenceGenerator("0/20 * * * * ? ");
        for (int i = 0; i < 20; i++) {
            // 计算下次时间点的开始时间
            nextTimePoint = cronSequenceGenerator.next(nextTimePoint);
            list.add(sdf.format(nextTimePoint));
        }
        System.out.println(list);
    }

}
