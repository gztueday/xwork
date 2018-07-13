package com.banggood.xwork.core.common;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "workFlow")
public class WorkFlowConfigProperty {

    private String tryTime;
    private String logPath;
    private String runNum = "5";
    private String schedulerlog;

    public String getSchedulerlog() {
        return schedulerlog;
    }

    public void setSchedulerlog(String schedulerlog) {
        this.schedulerlog = schedulerlog;
    }

    public String getTryTime() {
        return tryTime;
    }

    public void setTryTime(String tryTime) {
        this.tryTime = tryTime;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public String getRunNum() {
        return runNum;
    }

    public void setRunNum(String runNum) {
        this.runNum = runNum;
    }
}
