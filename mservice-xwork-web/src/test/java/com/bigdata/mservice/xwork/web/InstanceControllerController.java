package com.bigdata.mservice.xwork.web;


import com.alibaba.fastjson.JSONObject;
import org.junit.Test;
import remote.OkHTTPUtil;

import java.util.HashMap;
import java.util.Map;

public class InstanceControllerController {

    private String uri = "http://127.0.0.1:8082";

    @Test
    public void instanceStart() throws Exception {
        String url = uri + "/xwork/instance/start?flowName=test-work";
        String result = new OkHTTPUtil().httpRequestGet(url);
        System.out.println("--------------content------------->>");
        System.out.println(result);
        System.out.println("<<--------------content-------------");

    }

    @Test
    public void instacncecontrollerController() {
        String url = uri + "/xwork/instance/controller?instanceid=xxx&&eventType=yyy&&param=zzz";
        Map<String, Object> param = new HashMap<>();
        param.put("sleep", 100);
        param.put("runTime", String.valueOf(System.currentTimeMillis()));
        url = url.replace("xxx", "wf_test-work_cd2215c8-880d-463b-aeca-e4fef3fd232f_1504933692422").replace("yyy", "1")
                .replace("zzz", JSONObject.toJSONString(param));
        String result = new OkHTTPUtil().httpRequestGet(url);
        System.out.println("--------------content------------->>");
        System.out.println(result);
        System.out.println("<<--------------content-------------");
    }
}
