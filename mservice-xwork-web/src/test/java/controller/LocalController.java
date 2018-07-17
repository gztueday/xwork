package controller;


import com.alibaba.fastjson.JSONObject;
import com.bigdata.mservice.xwork.rest.HttpRespone;
import org.junit.Test;
import remote.OkHTTPUtil;

import java.util.HashMap;
import java.util.Map;

public class LocalController {

    @Test
    public void workFlowCreate() {
        String url = "http://127.0.0.1:8082/xwork/flow/create";
        String result = new OkHTTPUtil().httpRequstPost(url, Constant.workFlowConfig, null);
        System.out.println(result);
    }

    @Test
    public void workFlowInstanceController() throws InterruptedException {
        String urlStart = "http://127.0.0.1:8082/xwork/instance/start";
        String flowName = "dw_log_shell";
        Map<String, String> map = new HashMap<>();
        map.put("flowName", flowName);
        String response = new OkHTTPUtil().httpRequstPost(urlStart, null, map);
        HttpRespone httpRespone = JSONObject.parseObject(response, HttpRespone.class);
        String urlController = "http://127.0.0.1:8082/xwork/instance/controller?instanceid=" + httpRespone.getData() + "&eventType=1";
        Thread.sleep(3000);
        String result = new OkHTTPUtil().httpRequestGet(urlController);
        System.out.println(result);
    }

    @Test
    public void workFlowStart() throws InterruptedException {
        String urlStart = "http://127.0.0.1:8082/xwork/instance/start";
        String flowName = "dw_log_shell";
        Map<String, String> map = new HashMap<>();
        map.put("flowName", flowName);
        new OkHTTPUtil().httpRequstPost(urlStart, null, map);
    }

}
