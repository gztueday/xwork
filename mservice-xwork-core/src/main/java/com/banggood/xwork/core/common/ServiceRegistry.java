package com.banggood.xwork.core.common;

import com.alibaba.dubbo.registry.zookeeper.ZookeeperRegistry;
import com.alibaba.dubbo.registry.zookeeper.ZookeeperRegistryFactory;
import com.alibaba.dubbo.remoting.zookeeper.ZookeeperClient;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


@Component
public class ServiceRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistry.class);

    @Autowired
    private ZookeeperRegistryProperty zookeeperRegistryProperty;

    public volatile String IP;

    /**
     * 获取本应用在dubbo上的ip地址
     * @return
     */
    public String getDubbopIP() {
        if (this.IP == null) {
            try {
                ZookeeperRegistry zookeeperRegistry = ZookeeperRegistryFactory.getZookeeperRegistry();
                ZookeeperClient zkClient = zookeeperRegistry.getZkClient();
                List<String> nodes = zkClient.getChildren(zookeeperRegistryProperty.getZkRegistPath());
                this.IP = nodes.stream().filter((node) -> {

                    if (StringUtils.contains(node, zookeeperRegistryProperty.getDubboPort())) {
                        return true;
                    }

                    return false;

                }).collect(Collectors.toList()).get(0);

            } catch (Exception e) {
                LOGGER.error(e.getMessage(),e);
            }
            this.IP = StringUtils.substringBefore(this.IP, "%3Fanyhost");
            try {
                this.IP = URLDecoder.decode(this.IP, "gb2312");
            } catch (UnsupportedEncodingException e) {
                LOGGER.error(e.getMessage(),e);
            }
        }
        return this.IP;
    }

    public List<String> getNodes() throws UnsupportedEncodingException {
        ZookeeperRegistry zookeeperRegistry = ZookeeperRegistryFactory.getZookeeperRegistry();
        ZookeeperClient zkClient = zookeeperRegistry.getZkClient();
        List<String> dataList = new ArrayList<>();
        List<String> nodes = zkClient.getChildren(zookeeperRegistryProperty.getZkRegistPath());
        for (String node : nodes) {
            dataList.add(URLDecoder.decode(StringUtils.substringBefore(node, "%3Fanyhost"), "gb2312"));
        }
        return dataList;
    }

    /**
     * 从多个节点中随机取出一个节点
     *
     * @return
     */
    public java.lang.String discover() {
        ZookeeperRegistry zookeeperRegistry = ZookeeperRegistryFactory.getZookeeperRegistry();
        ZookeeperClient zkClient = zookeeperRegistry.getZkClient();
        java.lang.String data = null;
        List<String> dataList = new ArrayList<>();
        try {
            List<String> nodes = zkClient.getChildren(zookeeperRegistryProperty.getZkRegistPath());
            for (String node : nodes) {
                dataList.add(StringUtils.substringBefore(node, "%3Fanyhost"));
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(),e);
        }
        int size = dataList.size();
        // 存在新节点，使用即可
        if (size > 0) {
            if (size == 1) {
                data = dataList.get(0);
                LOGGER.debug("using only data: {}", data);
            } else {
                data = dataList.get(new Random().nextInt(size));
                LOGGER.debug("using random data: {}", data);
            }
        }
        try {
            data = URLDecoder.decode(data, "gb2312");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.out.println("zookeeper:" + data);
        return data;
    }
}