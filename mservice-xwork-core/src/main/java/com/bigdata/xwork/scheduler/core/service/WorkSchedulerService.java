package com.bigdata.xwork.scheduler.core.service;

import com.alibaba.dubbo.config.ReferenceConfig;
import com.bigdata.xwork.action.core.SeekOut;
import com.bigdata.xwork.action.core.WorkActionBase;
import com.bigdata.xwork.action.core.WorkFlow;
import com.bigdata.xwork.action.core.WorkRunStatus;
import com.banggood.xwork.api.entity.IWebExecutorController;
import com.bigdata.xwork.core.common.FlowActionUtils;
import com.bigdata.xwork.core.common.WorkFlowConfigProperty;
import com.bigdata.xwork.core.exception.ParseActionException;
import com.bigdata.xwork.core.service.WorkFlowService;
import com.banggood.xwork.dao.entity.*;
import com.banggood.xwork.dao.mapper.*;
import com.banggood.xwork.query.*;
import com.bigdata.xwork.dao.entity.*;
import com.bigdata.xwork.dao.mapper.*;
import com.bigdata.xwork.query.*;
import com.bigdata.xwork.query.result.ConfigResult;
import com.banggood.xwork.api.entity.LineResult;
import com.bigdata.xwork.query.result.PageResult;
import com.bigdata.xwork.scheduler.core.WorkScheduler;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;

/**
 * 调度器只部署在web那端，然后将工作流放到各服务器执行（采用队列的方式）
 */
@Component
public class WorkSchedulerService implements IWorkSchedulerService {
    @Autowired
    private WorkFlowConfigProperty wfcp;
    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private UserInfoMapper userInfoMapper;
    @Autowired
    private FlowActionUtils flowActionUtils;
    @Autowired
    private WorkFlowService workFlowService;
    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private SchedulerConfigMapper schedulerConfigMapper;
    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private SchedulerConfigBundleMapper schedulerConfigBundleMapper;
    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private SchedulerInstanceMapper schedulerInstanceMapper;
    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private WorkFlowInstanceMapper workFlowInstanceMapper;

    @Autowired
    private WorkSchedulerService workSchedulerService;
    @Autowired
    private SeekOut seekOut;

    @Value("${spring.dubbo.protocol.port}")
    private String port;

    public static Map<String, JobKey> jobKeyMap = new HashMap<>();

    public static Map<String, Scheduler> schedulerMap = new HashMap<>();

    private final static Logger logger = LoggerFactory.getLogger(WorkSchedulerService.class);


    public long calculatoRemoteVersion(long dependVersion, long calculate) {
        return seekOut.calculatoRemoteVersion(dependVersion, calculate);
    }

    @Override
    public boolean addWorkScheduler(WorkScheduler scheduler) {

        WorkSchedulerInfo workSchedulerInfo = this.flowActionUtils.encodeWorkSchedulerInfo(scheduler);
        UserInfo user = this.userInfoMapper.getUserInfoByID(workSchedulerInfo.getConfigerid());
        return true;
    }

    @Override
    public void updateSchedulerInstanceStatusForSuccess(String relationid, String flowName, Date date) {
//        WorkSchedulerInstance instance = this.schedulerInstanceMapper.selectInstance(relationid, flowName);
//        WorkRunStatus status = instance.getStatus();
//        if (!(WorkRunStatus.KILLED == status || WorkRunStatus.FAILED == status)) {
//            int count = this.schedulerInstanceMapper.updateSchedulerInstanceStatusForSuccess(relationid, flowName, WorkRunStatus.PENDING, date);
//            if (count <= 0) {
//                throw new RuntimeException("there is not this instance ");
//            }
//        }
    }

    @Override
    public PageResult queryForSchedulerInstance(SchedulerInstanceQueryObject qo) {
        int count = this.schedulerInstanceMapper.queryForCount(qo);
        if (count > 0) {
            List<WorkSchedulerInstance> list = this.schedulerInstanceMapper.query(qo);
            return new PageResult(list, count, qo.getCurrentPage(), qo.getPageSize());
        } else {
            return PageResult.getEmply(qo.getPageSize());
        }
    }

    public AcceptSchedulerObject querySchedulerConfig(String schedulerName) {
        AcceptSchedulerObject acceptSchedulerObject = this.schedulerConfigMapper.query(schedulerName);
        if (acceptSchedulerObject == null) {
            throw new RuntimeException("找不到定时任务的配置: " + schedulerName);
        }
        List<Bundle> bundles = this.schedulerConfigBundleMapper.queryBundleConfig(schedulerName);
        if (bundles != null && bundles.size() > 0) {
            acceptSchedulerObject.setBundles(bundles);
        }
        return acceptSchedulerObject;
    }

    public int queryCount(String schedulerName) {
        return this.schedulerConfigMapper.queryCount(schedulerName);
    }

    public void deleteByName(String schedulerName) {
        this.schedulerConfigMapper.delete(schedulerName);
    }

    public WorkSchedulerInstance querySchedulerByInstanceidAndVersion(String remoteInstanceid, long version) {
        return this.schedulerInstanceMapper.querySchedulerByInstanceidAndVersion(remoteInstanceid, version);
    }

    public void updateRelations(String remoteInstanceid, long version, String schedulerRelations) {
        this.schedulerInstanceMapper.updateRelations(remoteInstanceid, version, schedulerRelations);
    }

    public WorkSchedulerInstance querySchedulerInstance(String instanceid) {
        return this.schedulerInstanceMapper.queryByWorkFlowInstanceId(instanceid);
    }

    public WorkFlowInstance queryWorkFlowInstance(String wfInstanceid) {
        return this.workFlowInstanceMapper.selectWorkInstanceByid(wfInstanceid);
    }

    /**
     * workInfo里面包含了从workFlowInstance运行时的,关系消息
     *
     * @param instanceid
     * @param workInfo
     */
    public void updateWorkFlowInstanceRelations(String instanceid, String workInfo) {
        this.workFlowInstanceMapper.bundleSchedulerRelation(instanceid, workInfo);
    }

    public PageResult query(SchedulerConfigQueryObject qo) {
        int count = this.schedulerConfigMapper.queryForCount(qo);
        if (count > 0) {
            List<AcceptSchedulerObject> lists = this.schedulerConfigMapper.querySchedulerConfig(qo);
            return new PageResult(lists, count, qo.getCurrentPage(), qo.getPageSize());
        } else {
            return PageResult.getEmply(qo.getPageSize());
        }
    }

    public List<WorkFlowInfo> queryWorkFlowByName(String workFlowName) {
        return this.workFlowService.queryWorkFlowByName(workFlowName);
    }

    public List<WorkSchedulerInstance> querySchedulerBundleName(String schedulerName) {
        return this.schedulerInstanceMapper.queryByScheduler(schedulerName);
    }

    public List<VersionResult> querySchedulerBundleDate(String id) {
        List<WorkSchedulerInstance> instances = this.schedulerInstanceMapper.BundleDate(id);
        List<VersionResult> versionResults = new ArrayList<>();
        for (WorkSchedulerInstance instance : instances) {
            VersionResult versionResult = new VersionResult();
            versionResult.setDate(instance.getDate());
            versionResult.setVersion(instance.getVersion());
            versionResults.add(versionResult);
        }
        return versionResults;
    }

    /**
     * 先通过scheduler实例的instanceid查询出workFlowInstance的instanceId,在查询出action
     *
     * @param qo
     * @return
     */
    public List<WorkActionBase> querySchedulerBundleActions(QuerySchedulerActionsObject qo) throws ParseActionException, CloneNotSupportedException {
        WorkSchedulerInstance instance = this.schedulerInstanceMapper.querySchedulerByInstanceidAndVersion(qo.getInstanceid(), qo.getVersion());
        String flowName = instance.getFlowName();
        return this.workFlowService.queryWorkFlowByActionName(flowName);

    }

    public void updateSchedulerStatus(WorkFlow workFlow, WorkRunStatus status) {
        this.schedulerInstanceMapper.updateSchedulerStatus(workFlow.getInstanceid(), status);
    }

    public int queryDispatcherCount(String instanceid) {
        return this.schedulerInstanceMapper.queryDispatcherCount(instanceid);
    }

    public void addSchedulerInstance(WorkScheduler workScheduler, long version) {
        this.schedulerInstanceMapper.insertWorkScheduler(workScheduler.getInstanceid(), workScheduler.getDate(), version);
    }

    public Date selectExecuteDate(String instanceid) {
        return this.schedulerInstanceMapper.selectExecuteDate(instanceid);
    }

    public void updateWorkFlowRelationIntoSchedulerStatus(String schedulerInstance, String workFlowInstance, long version, Date runTime) {
        this.schedulerInstanceMapper.updateWorkFlowRelationIntoSchedulerStatus(schedulerInstance, workFlowInstance, version, runTime);

    }

    public List<WorkActionBase> querySchedulerBundleRightActios(String flowName) throws ParseActionException, CloneNotSupportedException {
        return this.workFlowService.queryWorkFlowByActionName(flowName);
    }

    public PageResult statusCalendar(SchedulerStatusInstanceQueryObject qo) {

        int count = this.schedulerInstanceMapper.queryInstanceIdCount(qo);
        if (count > 0) {
            List<WorkSchedulerInstance> workSchedulerInstances = this.schedulerInstanceMapper.queryInstanceId(qo);
            return new PageResult(workSchedulerInstances, count, qo.getCurrentPage(), qo.getPageSize());
        } else {
            return PageResult.getEmply(qo.getPageSize());
        }
    }

    public List<Bundle> statusScheduler(String schedulerInstance) {
        String schedulerName = this.schedulerInstanceMapper.querySchedulerName(schedulerInstance);
        List<Bundle> bundles = this.schedulerConfigBundleMapper.queryBundleConfig(schedulerName);
        if (bundles == null) {
            return new ArrayList<>();
        }
        return bundles;
    }

    public AcceptSchedulerObject configScheduler(String schedulerInstance) {
        String schedulerName = this.schedulerInstanceMapper.querySchedulerName(schedulerInstance);
        return this.workSchedulerService.querySchedulerConfig(schedulerName);
    }

    public List<ConfigResult> configSchedulerConfig(String schedulerInstance) {
        List<ConfigResult> configResults = new ArrayList<>(6);
        AcceptSchedulerObject acceptSchedulerObject = configScheduler(schedulerInstance);
        ConfigResult conf = new ConfigResult();
        conf.setName("定时调度配置名称");
        conf.setValue(acceptSchedulerObject.getSchedulerName());
        configResults.add(conf);

        conf = new ConfigResult();
        conf.setName("调度的普通任务名称");
        conf.setValue(acceptSchedulerObject.getWorkFlowName());
        configResults.add(conf);


        if (acceptSchedulerObject.getStartDate() != null) {
            conf = new ConfigResult();
            conf.setName("开始时间");
            conf.setValue(acceptSchedulerObject.getStartDate().toLocaleString());
            configResults.add(conf);
        } else {
            conf = new ConfigResult();
            conf.setName("开始时间");
            conf.setValue("没有设置开始运行时间");
            configResults.add(conf);
        }
        if (acceptSchedulerObject.getEndDate() != null) {
            conf = new ConfigResult();
            conf.setName("结束时间");
            conf.setValue(acceptSchedulerObject.getEndDate().toLocaleString());
            configResults.add(conf);
        } else {
            conf = new ConfigResult();
            conf.setName("结束时间");
            conf.setValue("没有设置结束运行时间");
            configResults.add(conf);
        }
        conf = new ConfigResult();
        conf.setName("cron表达式");
        conf.setValue(acceptSchedulerObject.getCron());
        configResults.add(conf);

        conf = new ConfigResult();
        conf.setName("配置描述");
        conf.setValue(acceptSchedulerObject.getDescript());
        configResults.add(conf);

        conf = new ConfigResult();
        conf.setName("最近更新时间");
        conf.setValue(acceptSchedulerObject.getUpdateTime().toLocaleString());
        configResults.add(conf);

        return configResults;
    }

    public void dealSchedulerInstance(WorkFlow workFlow) {

        WorkSchedulerInstance instance = decodeSchedulerConfig(workFlow.getSchedulerid());

        instance.setVersion(instance.getVersion() + 1);
        workFlow.setSchedulerInstance(instance);
        loggerSchedulerMsg(workFlow, instance);
    }

    public void loggerSchedulerMsg(WorkFlow workFlow, WorkSchedulerInstance scheduler) {
        StringBuilder sb = new StringBuilder(50);
        sb.append("开始运行普通任务运行id:").append(workFlow.getInstanceid()).append("定时任务表达式:")
                .append(scheduler.getCronStr()).append("定时任务任务的运行id:").append(scheduler.getInstanceid());
        FileWriter fw = null;
        try {
            File f = new File(wfcp.getSchedulerlog() + scheduler.getInstanceid() + ".txt");
            fw = new FileWriter(f, true);
            PrintWriter pw = new PrintWriter(fw);
            pw.println(sb.toString());
            pw.flush();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            try {
                fw.close();
                logger.info(" {} stream is closed", scheduler.getInstanceid());
            } catch (IOException e1) {
                logger.error(e1.getMessage(), e1);
            }
        }
    }


    public WorkSchedulerInstance decodeSchedulerConfig(String schedulerid) {
        WorkSchedulerInstance instance = this.schedulerInstanceMapper.querySchedulerNameForInstanceId(schedulerid);
        addScheduerOutputStream(instance);
        return instance;
    }

    public void addScheduerOutputStream(WorkSchedulerInstance instance) {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(wfcp.getLogPath() + instance.getInstanceid() + ".txt");
            instance.setOutputStream(outputStream);
        } catch (FileNotFoundException e) {
            logger.error("设置定时任务日志输出流", e);
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e1) {
                    logger.error("设置定时任务日志输出流 ", e1);
                }
            }
        }
    }

    public String queryWorkFlowName(String remoteInstanceid) {
        return this.schedulerInstanceMapper.queryWorkFlowName(remoteInstanceid);
    }

    public long queryMinVersionForDispatcher(String schedulerInstanceId) {
        return this.schedulerInstanceMapper.queryMinVersionForDispatcher(schedulerInstanceId);
    }

    public long queryMaxVersionForDispatcher(String schedulerInstanceId) {
        return this.schedulerInstanceMapper.queryMaxVersionForDispatcher(schedulerInstanceId);
    }

    public int querySchedulerInstanceCount(String remoteInstanceid) {
        return this.schedulerInstanceMapper.querySchedulerInstanceCount(remoteInstanceid);
    }

    public int queryCountForDispatcher(String schedulerInstanceId) {
        return this.schedulerInstanceMapper.queryCountForDispatcher(schedulerInstanceId);
    }

    public Date selectExecuteMinDate(String schedulerInstanceId) {
        return this.schedulerInstanceMapper.selectExecuteMinDate(schedulerInstanceId);
    }

    public void updateRelations(String instanceid, String relationJson) {
        this.schedulerInstanceMapper.updateRelationForRightScheduler(instanceid, relationJson);
    }

    public void updateSchedulerStatusGroup(String schedulerInstanceId, WorkRunStatus status) {
        this.schedulerInstanceMapper.updateSchedulerStatusGroup(schedulerInstanceId, status);
    }

    public LineResult querySchedulerLogger(String schedulerInstance, long size) {
        String ip = this.schedulerInstanceMapper.queryExecuteIpBySchedulerInstanceId(schedulerInstance);
        ReferenceConfig<IWebExecutorController> ref = this.workFlowService.dubboComsuer("querySchedulerLogger");
        ref.setUrl(ip);
        return ref.get().querySchedulerLogger(schedulerInstance, size);
    }
}
