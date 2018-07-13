package remote.scheduler;


import com.alibaba.fastjson.JSONObject;
import com.banggood.xwork.api.entity.EventResponse;
import org.junit.Test;
import remote.OkHTTPUtil;

public class SchedulerController {

    @Test
    public void startRemote() throws InterruptedException {
        String url = "http://127.0.0.1:8082/scheduler/start?schedulerName=remote-kettle";
        String response = new OkHTTPUtil().httpRequestGet(url);
        EventResponse eventResponse = JSONObject.parseObject(response, EventResponse.class);
        Thread.sleep(5000);
        url = "http://127.0.0.1:8082/scheduler/remote/controller?instanceid=" + eventResponse.getJson() + "&eventType=1";
        response = new OkHTTPUtil().httpRequestGet(url);
        System.out.println(response);

    }

    @Test
    public void schedulerController() {
        String url = "http://127.0.0.1:8082/scheduler/remote/controller?instanceid=" + "wf_remote-kettle_9141b3b3-8d6a-4440-9989-4745d21cc2a6_1509444503003" + "&eventType=1";
        String response = new OkHTTPUtil().httpRequestGet(url);
    }

}
