package com.banggood.xwork.scheduler.core.dispathcer;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.banggood.xwork.action.core.WorkActionRelation;
import com.banggood.xwork.core.exception.NoParamException;
import com.banggood.xwork.core.exception.ParamDataTypeException;
import com.banggood.xwork.core.exception.ParseActionException;
import com.banggood.xwork.core.service.XWorkApp;
import com.banggood.xwork.dao.entity.Bundle;
import com.banggood.xwork.query.AcceptSchedulerObject;
import com.banggood.xwork.query.DateUtil;
import com.banggood.xwork.scheduler.core.DispatcherScheduler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import sun.rmi.runtime.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by zouyi on 2018/3/12.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = XWorkApp.class)
@WebAppConfiguration
public class AcceptSchedulerObjectTest {

    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private DispatcherScheduler dispatcherScheduler;

    @Test
    public void parseJson() {
        String json = "{\"scheduler1_2\":{\"children\":[],\"dependWorkActions\":[{\"remoteDependName\":\"scheduler1_2\",\"remoteName\":\"scheduler1_2\",\"remoteSwitch\":false,\"schedulerid\":0,\"workFlowName\":\"scheduler_one\"},{\"remoteDependName\":\"scheduler1_2\",\"remoteName\":\"scheduler1_3\",\"remoteSwitch\":false,\"schedulerid\":0,\"workFlowName\":\"scheduler_one\"}],\"fathers\":[],\"remoteDepends\":[]}}";
        Map<String, WorkActionRelation> relationMap = JSONObject.parseObject(json, new TypeReference<Map<String, WorkActionRelation>>() {
        });
        for (String key : relationMap.keySet()) {
            WorkActionRelation workActionRelation = relationMap.get(key);
            System.out.println(workActionRelation);
        }
        System.out.println(relationMap);
    }


    @Test
    public void schedulerObjectStartTest() {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            this.dispatcherScheduler.start("scheduler_test1", null);
            latch.await(60 * 3 * 1000, TimeUnit.MILLISECONDS);
        } catch (ParseActionException e) {
            e.printStackTrace();
        } catch (NoParamException e) {
            e.printStackTrace();
        } catch (ParamDataTypeException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void schedulerObjectTest() {
        AcceptSchedulerObject acceptSchedulerObject = new AcceptSchedulerObject();
        acceptSchedulerObject.setCreateTime(new Date());
        acceptSchedulerObject.setStartDate(new Date());
        acceptSchedulerObject.setEndDate(DateUtil.getDayAgo(new Date(), -5));
        acceptSchedulerObject.setCron("0/25 * * * * ? ");
        acceptSchedulerObject.setDescript("定时任务测试");
        acceptSchedulerObject.setWorkFlowName("test_scheduler_bundle");
        acceptSchedulerObject.setSubmiterid(0);
        acceptSchedulerObject.setSchedulerName("scheduler_one_scheduler_1");
        acceptSchedulerObject.setUpdateTime(new Date());
        acceptSchedulerObject.setDependVersion(new Date().getTime() - 1000);

        try {
            dispatcherScheduler.insertScheduler(acceptSchedulerObject);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void schedulerObjectBundleTest() {
        AcceptSchedulerObject acceptSchedulerObject = new AcceptSchedulerObject();
        acceptSchedulerObject.setCreateTime(new Date());
        acceptSchedulerObject.setStartDate(new Date());
        acceptSchedulerObject.setEndDate(DateUtil.getDayAgo(new Date(), -5));
        acceptSchedulerObject.setCron("0/25 * * * * ? ");
        acceptSchedulerObject.setDescript("定时任务测试");
        acceptSchedulerObject.setWorkFlowName("scheduler_one");
        acceptSchedulerObject.setSubmiterid(0);
        acceptSchedulerObject.setSchedulerName("scheduler_right");
        acceptSchedulerObject.setUpdateTime(new Date());
        acceptSchedulerObject.setDependVersion(new Date().getTime() - 1000);

        List<Bundle> bundleList = new ArrayList<>();

        for (int i = 0; i < 1; i++) {
            Bundle bundle = new Bundle();
            /**
             * 与自己的配置建立关系的属性
             */
            bundle.setSchedulerName("scheduler_right");
            bundle.setDependAction("scheduler1_2");
            bundle.setRemoteActionStr("scheduler1_2,scheduler1_3");
            bundle.setRemoteInstanceid("scheduler_2ed42742-790d-41bd-974a-7f99b44a9f34_num_1521457205467");
            bundle.setVersion(1520915323234L);
            bundleList.add(bundle);
        }
        acceptSchedulerObject.setBundles(bundleList);
        System.out.println("--------------------------------------");
        System.out.println(JSONObject.toJSONString(acceptSchedulerObject));
        System.out.println("--------------------------------------");
        try {
            dispatcherScheduler.insertScheduler(acceptSchedulerObject);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
}
