package com.bigdata.xwork.core.common;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by zouyi on 2017/12/4.
 */
@Configuration
@ConfigurationProperties(prefix = "constant.property")
public class ZookeeperRegistryProperty {

    private String zkSessionTime;

    private String zkRegistPath;

    private String registryAddress;

    private String dubboPort;

    public String getZkSessionTime() {
        return zkSessionTime;
    }

    public void setZkSessionTime(String zkSessionTime) {
        this.zkSessionTime = zkSessionTime;
    }

    public String getZkRegistPath() {
        return zkRegistPath;
    }

    public void setZkRegistPath(String zkRegistPath) {
        this.zkRegistPath = zkRegistPath;
    }

    public String getRegistryAddress() {
        return registryAddress;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public String getDubboPort() {
        return dubboPort;
    }

    public void setDubboPort(String dubboPort) {
        this.dubboPort = dubboPort;
    }
}
