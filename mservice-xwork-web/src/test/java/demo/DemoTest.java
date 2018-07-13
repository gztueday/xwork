package demo;


import com.alibaba.fastjson.JSONObject;
import org.junit.Test;
import remote.OkHTTPUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DemoTest {

    @Test
    public void jsonTest() {
        List<String> list = new ArrayList<>();
        list.add("123123");
        System.out.println(JSONObject.toJSONString(list));
    }

    @Test
    public void xworkActionAll() {
        String result = new OkHTTPUtil().httpRequestGet("http://192.168.15.213:5567/xwork/action/all");
        System.out.println(result);
    }

    @Test
    public void xworkActionList() {
        String result = new OkHTTPUtil().httpRequestGet("http://127.0.0.1:8082/xwork/action/list?showName=shell action");
        System.out.println(result);
    }

    @Test
    public void xworkFlowCreate() {
        String result = new OkHTTPUtil().httpRequstPost("http://127.0.0.1:8082/xwork/flow/create", WorkFlowConfigJson.workFlowConfig, null);
        System.out.println(result);
    }

    @Test
    public void xworkInstanceCotroller() {
        Integer eventType = 1;
        String instanceid = "wf_testActions3_33919d51-c4eb-460a-b8ce-a049a064dd05_1505790697929";
        String result = new OkHTTPUtil().httpRequestGet("http://127.0.0.1:8082/xwork/instance/controller?eventType=" + eventType + "&&instanceid=" + instanceid);
        System.out.println(result);
    }

    @Test
    public void xworkInstanceStart() {
        HashMap<String, String> map = new HashMap<>();
        map.put("flowName", "testActions5");
        String result = new OkHTTPUtil().httpRequstPost("http://127.0.0.1:8082/xwork/instance/start", "{}", map);
        System.out.println(result);
    }

    @Test
    public void xworkSchedulerCreate() {
        HashMap<String, String> map = new HashMap<>();
        map.put("cronStr", "0/20 * * * * ? ");//每隔20秒运行一次
        map.put("flowName", "testActions5");
        map.put("configerid", "2");
        map.put("schedulerName", "scheduler");
        String result = new OkHTTPUtil().httpRequstPost("http://127.0.0.1:8082/xwork/scheduler/create", "{}", map);
        System.out.println(result);
    }

    @Test
    public void xworkSchedulerDispatch() {
        String schedulerName = "scheduler";
        String result = new OkHTTPUtil().httpRequestGet("http://127.0.0.1:8082/xwork/scheduler/dispatch?schedulerName=" + schedulerName);
        System.out.println(result);
    }

    @Test
    public void xworkSchedulerController() {
        String instanceid = "wf_scheduler_c9f4cb85-492b-4385-a8cf-9d3eeabd11f6_0";
        String eventType = "1";
        String result = new OkHTTPUtil().httpRequestGet("http://127.0.0.1:8082/xwork/scheduler/controller?instanceid=" + instanceid + "&eventType=" + eventType);
        System.out.println(result);
    }

}
