package com.banggood.xwork.dubb.controller;

import com.alibaba.fastjson.JSONObject;
import com.banggood.xwork.api.entity.WorkFlowEventObject;
import org.junit.Test;


public class WebExecutorControllerTest  {

//    @Autowired
//    private WorkFlowService workFlowService;

    @Test
    public void monitor() {

        WorkFlowEventObject wfeo = new WorkFlowEventObject();
        wfeo.setFlowName("yyyyyy");
        wfeo.setInstanceid("123123");
        wfeo.setEventType(123);
        String json = JSONObject.toJSONString(wfeo);
        String result = new OkHTTPUtil().httpRequstPost("http://localhost:8081/exectuor/monitor", json,null);
        System.out.println("result:" + result);
    }

}
