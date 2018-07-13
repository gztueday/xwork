package com.banggood.xwork.use.dubbo.zkClient;

import com.banggood.xwork.core.common.ServiceRegistry;
import com.banggood.xwork.core.service.XWorkApp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Created by zouyi on 2018/1/8.
 */
@SuppressWarnings("ALL")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = XWorkApp.class)
@WebAppConfiguration
public class DubboZKClient {
    @Autowired
    ServiceRegistry registry;

    @Test
    public void serviceRegisterTest() {
//        String dubbopIP = registry.getDubbopIP();
//        String discover = registry.discover();
//        System.out.println("================================");
//        System.out.println(discover);
//        System.out.println(dubbopIP);
    }

    @Test
    public void zkClientTest() {
//        ZookeeperRegistry zookeeperRegistry = ZookeeperRegistryFactory.getZookeeperRegistry();
//        ZookeeperClient zkClient = zookeeperRegistry.getZkClient();
//        URL url = zkClient.getUrl();
//        System.out.println(url);
//        Set<URL> registered = zookeeperRegistry.getRegistered();
//        for (URL url1 : registered) {
//            String host = url1.getHost();
//            url1.getPath();
//            url1.getFull();
//            String ip = url1.getIp();
//            System.out.println(ip);
//        }
//        Iterator<URL> iterator = zookeeperRegistry.getRegistered().iterator();
//        URL urlxxx = iterator.next();
//        String full = urlxxx.getFull();
//        String ip = StringUtils.substringBefore(full, "?");
//        System.out.println(ip);
    }
}
