package remote;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bigdata.mservice.xwork.rest.HttpRespone;
import org.junit.Test;

public class RemoteController {


    @Test
    public void remoteDepend() {
        //String[] workFlowName = new String[]{"dw_fop", "dw_sale"};
        String url = "http://localhost:8082/xwork/remote/depend?workFlowNames=dw_fop,dw_sale&open=true";
        String result = new OkHTTPUtil().httpRequestGet(url);
        System.out.println(result);
    }

    @Test
    public void remoteDependController() throws InterruptedException {
        String url = "http://localhost:8082/xwork/remote/depend?workFlowNames=dw_fop,dw_sale&open=true";
        String result = new OkHTTPUtil().httpRequestGet(url);
        HttpRespone respone = JSONObject.parseObject(result, HttpRespone.class);
        JSONArray objects = JSONObject.parseArray(respone.getData().toString());
        StringBuilder sb = new StringBuilder(50);
        for (int i = 0; i < objects.size(); i++) {
            sb.append(objects.get(i).toString());
            if (i < objects.size() - 1) {
                sb.append(",");
            }
        }
        Thread.sleep(5000);
        url = "http://127.0.0.1:8082/xwork/remote/controller?instances=" + sb.toString() + "&eventType=1";
        result = new OkHTTPUtil().httpRequestGet(url);
        System.out.println(result);
    }
}