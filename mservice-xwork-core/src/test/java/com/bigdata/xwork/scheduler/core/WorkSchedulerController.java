package com.bigdata.xwork.scheduler.core;

import com.bigdata.xwork.action.core.BaseTestApplication;
import com.banggood.xwork.api.entity.EventResponse;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


public class WorkSchedulerController extends BaseTestApplication {

    @Test
    public void schedulerController() {
        String s = StringUtils.substringBeforeLast("/asdfasdf/asdf", "/");
        System.out.println(s);
    }
}
