package com.banggood.xwork.core.service;

import com.banggood.xwork.core.common.FlowActionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Date;

/**
 * Created by zouyi on 2018/3/5.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = XWorkApp.class)
@WebAppConfiguration
public class FlowActionUtilsTest {
    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private FlowActionUtils flowActionUtils;


}
