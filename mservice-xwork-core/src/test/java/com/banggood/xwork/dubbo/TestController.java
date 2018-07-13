package com.banggood.xwork.dubbo;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.MethodConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.banggood.xwork.api.entity.EventResponse;
import com.banggood.xwork.api.entity.IWebExecutorController;
import org.junit.Test;

import java.util.Arrays;



public class TestController {

    @Test
    public void dubbo() {

        ApplicationConfig ac = new ApplicationConfig();
        ac.setName("consumer");
        ReferenceConfig<IWebExecutorController> ref = new ReferenceConfig<>();
        ref.setInterface(IWebExecutorController.class);
        ref.setUrl("dubbo://192.168.2.1:20880/com.banggood.xwork.api.entity.IWebExecutorController");
        ref.setApplication(ac);
        ref.setVersion("1.0.0");
        MethodConfig mc = new MethodConfig();
        mc.setAsync(false);
        mc.setName("remote");
        ref.setMethods(Arrays.asList(new MethodConfig[]{mc}));
        ref.getClient();
        EventResponse hello = ref.get().remote("123");
        System.out.println(hello);
    }

}
