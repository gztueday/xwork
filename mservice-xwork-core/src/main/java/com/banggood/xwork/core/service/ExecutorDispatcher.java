package com.banggood.xwork.core.service;

import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.registry.zookeeper.ZookeeperRegistry;
import com.alibaba.dubbo.registry.zookeeper.ZookeeperRegistryFactory;
import com.banggood.xwork.action.core.*;
import com.banggood.xwork.action.impl.HiveAction;
import com.banggood.xwork.api.entity.IWebExecutorController;
import com.banggood.xwork.core.common.WorkFlowConfigProperty;
import com.banggood.xwork.core.common.ZookeeperRegistryProperty;
import com.banggood.xwork.core.exception.NoParamException;
import com.banggood.xwork.core.exception.ParamDataTypeException;
import com.banggood.xwork.core.exception.ParseActionException;
import com.banggood.xwork.dao.service.WorkDaoService;
import com.banggood.xwork.scheduler.core.service.WorkSchedulerService;
import org.apache.hadoop.conf.Configuration;
import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.*;

@Component
public class ExecutorDispatcher {

    @Autowired
    private WorkFlowService workFlowService;

    @Autowired
    private WorkDaoService workDaoService;

    @Autowired
    private WorkFlowConfigProperty wfcp;
    @Autowired
    private WorkSchedulerService workSchedulerService;

    public static String ip;

    @Autowired
    private ZookeeperRegistryProperty zookeeperRegistryProperty;

    private volatile boolean open = true;
    private final Logger logger = LoggerFactory.getLogger(ExecutorDispatcher.class);

    public final String getLocalIp() throws SocketException {

        String ipString = "";
        Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
        InetAddress ip;
        while (allNetInterfaces.hasMoreElements()) {
            NetworkInterface netInterface = allNetInterfaces.nextElement();
            Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                ip = addresses.nextElement();
                if (ip != null && ip instanceof Inet4Address && !ip.getHostAddress().equals("127.0.0.1")) {
                    return ip.getHostAddress();
                }
            }
        }
        return ipString;
    }

    @PostConstruct
    public void startFetcher() throws UnknownHostException, SocketException {
        ip = "dubbo://" + getLocalIp() + ":" + zookeeperRegistryProperty.getDubboPort() + "/com.banggood.xwork.api.entity.IWebExecutorController";
//        ip = "dubbo://192.168.15.211:" + zookeeperRegistryProperty.getDubboPort() + "/com.banggood.xwork.api.entity.IWebExecutorController";
        Configuration conf = HiveAction.conf;
        if (conf == null) {
            initHdfs();
        }
        WorkExecutorPool.submitWork(() -> {
            ZookeeperRegistry zookeeperRegistry = ZookeeperRegistryFactory.getZookeeperRegistry();
            while (zookeeperRegistry == null) {
                zookeeperRegistry = ZookeeperRegistryFactory.getZookeeperRegistry();
            }
            putWorkFlowForPENDING();
            while (true) {
                putWorkFlowForDISTRIBUTE();
            }
        });
    }

    /**
     * hdfs的初始化
     */
    public void initHdfs() {
        if (HiveAction.hiveUrl == null || HiveAction.conf == null) {
            InputStream in = HiveAction.class.getClassLoader().getResourceAsStream("bg.properties");
            Properties p = new Properties();
            try {
                p.load(in);
                HiveAction.hiveUrl = p.getProperty("hive.hiveUrl");
                p.remove("hive.hiveUrl");
                HiveAction.conf = new Configuration();
                for (Object key : p.keySet()) {
                    HiveAction.conf.set(key.toString(), p.getProperty(key.toString()));
                }
            } catch (IOException e) {
                HiveAction.logger.error("can not load the hiveUrl", e);
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    logger.error("初始化hive action时报错", e);
                }
            }
        }
    }

    /**
     * 运行PENDING状态的workFlow
     */
    private void putWorkFlowForPENDING() {
        List<WorkFlow> flows;
        try {
            //在这个方法里面把所有相关workFlow的action创建出来了
            flows = this.workDaoService.getFlowInstancesByStatus(WorkRunStatus.PENDING);
            if (flows.size() > 0) {
                for (WorkFlow workFlow : flows) {
                    ReferenceConfig<IWebExecutorController> ref = this.workFlowService.dubboComsuer("removePendingStatus");
                    ref.setUrl(workFlow.getIp());
                    ref.get().removePendingStatus(workFlow.getInstanceid());
                    WorkFlow wf = this.workDaoService.lockWorkFlowInstance(workFlow, WorkRunStatus.PENDING);
                    wf.initialize();
                    wf.setTyrTime(Integer.valueOf(wfcp.getTryTime()));
                    wf.setWorkDaoService(workDaoService);
                    wf.getEndWorkAction().setWorkFlowService(workFlowService);
                    wf.buildForWorkFlowInstance();
                    setWorkFlowEndaction(wf);
                    WorkFlowService.pendingFlowInstance.put(wf.getInstanceid(), wf);
                    //将PENDING改成RUNNING状态
                    if (this.workDaoService.lockWorkFlowInstanceForPending(wf, WorkRunStatus.PENDING) > 0) {
                        WorkFlowService.pendingFlowInstance.remove(wf.getInstanceid(), wf);
                        setOutputStream(wf);
                        this.workFlowService.addWorkFlowInstance(wf);
                        wf.startWorkFlowInstance(workFlowService);
                    }
                }
                Thread.sleep(1000);
            } else {
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            logger.error("任务在等待时,出现错误 ", e);
        }
    }

    private void setSchedulerWorkEndAction(WorkFlow work) {
        work.getEndWorkAction().setCallBack(null, new WorkRunCallBack() {
            @Override
            public void listen(WorkActionBase action) {
                work.setStatus(WorkRunStatus.SUCCESS);
                work.setEndTime(System.currentTimeMillis());
                /**
                 * 运行完一次后,又进入等待状态,等待下一次时间运行
                 */
                workSchedulerService.updateSchedulerInstanceStatusForSuccess(work.getFatherInstanceId(), work.getFlowName(), new Date());
                try {
                    work.getWorkDaoService().updateWorkFlowInstanceStatus(work);
                } catch (UnknownHostException | ParseActionException e) {
                    logger.error(" 设置定时调度任务的workEndAction ", e);
                }
            }

            @Override
            public void initialize() {

            }

        });

    }

    public void setWorkFlowEndaction(WorkFlow workFlow) {
        workFlow.getEndWorkAction().setCallBack(null, new WorkRunCallBack() {
            @Override
            public void listen(WorkActionBase action) {
                workFlow.setStatus(WorkRunStatus.SUCCESS);
                workFlow.setEndTime(System.currentTimeMillis());
                try {
                    workFlow.getWorkDaoService().updateWorkFlowInstanceStatus(workFlow);
                } catch (UnknownHostException | ParseActionException e) {
                    logger.error("设置普通任务工作流 workFlowEndAction ", e);
                }
            }

            @Override
            public void initialize() {

            }
        });
    }

    /**
     * 运行DISTRIBUTED状态的workFlow
     */
    private void putWorkFlowForDISTRIBUTE() {
        List<WorkFlow> flows;
        try {
            flows = this.workDaoService.getFlowInstancesByStatus(WorkRunStatus.DISTRIBUTED);
            if (flows.size() > 0) {
                for (WorkFlow workFlow : flows) {
                    //将DISTRIBUTED状态改成PENDING状态,同时给数据库和ip设置锁
                    WorkFlow work = this.workDaoService.lockWorkFlowInstance(workFlow, WorkRunStatus.DISTRIBUTED);
                    logger.info("flows {}", flows.toString());
                    if (work != null) {
                        work.initialize();
                        work.setWorkDaoService(workDaoService);
                        work.setTyrTime(Integer.valueOf(wfcp.getTryTime()));
                        work.getEndWorkAction().setWorkFlowService(workFlowService);
                        work.buildForWorkFlowInstance();
                        if (work.getSchedulerid() == null) {
                            setWorkFlowEndaction(work);
                        } else {
                            setSchedulerWorkEndAction(work);
                        }

                        WorkFlowService.pendingFlowInstance.put(workFlow.getInstanceid(), workFlow);
//                      if (WorkFlowService.cachedFlowInstance.size() >= Integer.parseInt(wfcp.getRunNum())) {
                        if (WorkFlowService.cachedFlowInstance.size() >= 100) {
                            open = true;
                            while (open) {
                                Thread.sleep(1000);
                            }
                        }
                        WorkExecutorPool.executorService.submit(() -> {
                            WorkFlow wf = WorkFlowService.pendingFlowInstance.get(workFlow.getInstanceid());
                            if (wf != null) {
                                WorkFlowService.pendingFlowInstance.remove(workFlow.getInstanceid());
                                //将PENDING状态改成RUNNING状态,同时给数据库和ip设置锁
                                try {
                                    if (this.workDaoService.lockWorkFlowInstanceForPending(work, WorkRunStatus.PENDING) > 0) {
                                        setOutputStream(wf);
                                        this.workFlowService.addWorkFlowInstance(wf);
                                        if (wf.getSchedulerid() == null) {
                                            wf.startWorkFlowInstance(workFlowService);
                                        } else {
                                            wf.startSchedulerFlowInstance(workSchedulerService);
                                        }
                                    }
                                } catch (CloneNotSupportedException | NoParamException | UnknownHostException
                                        | InterruptedException | ParamDataTypeException | ParseActionException e) {
                                    logger.error("扫描出来运行时,报错 ", e);
                                }
                            }
                        });
                    }
                }
                Thread.sleep(1000);
            } else {
                Thread.sleep(1000);
            }
        } catch (NoParamException | UnknownHostException | InterruptedException | ParamDataTypeException |
                ParseActionException | CloneNotSupportedException e) {
            logger.error(" running workFlow's dispatcher status ", e);
        }
    }

    public void setOutputStream(WorkFlow wf) {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(wfcp.getLogPath() + wf.getInstanceid() + ".txt");
            wf.setOutputStream(outputStream);
        } catch (FileNotFoundException e) {
            logger.error("设置日志输出流", e);
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e1) {
                    logger.error("设置日志输出流 ", e1);
                }
            }
        }
    }
}