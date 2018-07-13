package com.banggood.xwork.dao;

import com.banggood.xwork.core.service.WorkFlowService;
import com.banggood.xwork.core.service.XWorkApp;
import com.banggood.xwork.query.WorkFlowConfigQueryObject;
import com.banggood.xwork.query.result.PageResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Created by zouyi on 2018/1/22.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = XWorkApp.class)
@WebAppConfiguration
public class DaoTests {
    @Autowired
    private WorkFlowService workFlowService;

    @Test
    public void getWorkFlowAll() {
        PageResult workFlowAll = this.workFlowService.getWorkFlowAll(new WorkFlowConfigQueryObject());
        System.out.println(workFlowAll);

    }

}
