package com.bigdata.xwork.action.core;

import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.annotation.JSONField;
import com.banggood.xwork.api.entity.Constant;
import com.banggood.xwork.api.entity.EventResponse;
import com.banggood.xwork.api.entity.IWebExecutorController;
import com.bigdata.xwork.core.common.AbstractEnviroment;
import com.bigdata.xwork.core.common.CacheRelation;
import com.bigdata.xwork.core.common.DependencyWorkAction;
import com.bigdata.xwork.core.common.RemoteWorkAction;
import com.bigdata.xwork.core.exception.NoParamException;
import com.bigdata.xwork.core.exception.ParamDataTypeException;
import com.bigdata.xwork.core.exception.ParseActionException;
import com.bigdata.xwork.core.service.WorkFlowService;
import com.banggood.xwork.dao.entity.*;
import com.bigdata.xwork.dao.service.WorkDaoService;
import com.bigdata.xwork.query.AcceptSchedulerObject;
import com.bigdata.xwork.scheduler.core.service.WorkSchedulerService;
import com.bigdata.xwork.dao.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;

/**
 * notrunning->running->success or killed failed 关于参数解析：
 * 第一种是捕获前面的节点的输出，这类型参数在运行时动态解析 第二种是自定义函数或变理解析，这类型的参数在work action flow实例化前解析
 * 所有参数是全局的
 */
public abstract class WorkActionBase extends AbstractEnviroment implements Runnable {

    // ------------------------------动态变量，无需持久化------------------------------------------

    /**
     * 父类动作节点
     */
    private List<WorkActionBase> fathers = new ArrayList<>();

    private CacheRelation cacheRelation;

    /**
     * 子节点
     */
    private List<WorkActionBase> children = new ArrayList<>();

    /**
     * 包含此动作的子工作流
     */
    private WorkFlow subWorkFlow;

    private boolean startOtherWorkFlowAction = false;
    /**
     * 实例ID
     */
    private String instanceid;

    /**
     * 标志是否条件过滤
     */
    private volatile boolean conditionPass = false;

    /**
     * 运行状态
     */
    private volatile WorkRunStatus status = WorkRunStatus.DISTRIBUTED;

    /**
     * 运行实例唯一uuid
     */
    private String uuid;

    /**
     * 调度id
     */
    private String schedulerid;
    /**
     * father线程开关
     */
    private volatile boolean submit = true;
    /**
     * action开始时间
     */
    private Long startTime;

    /**
     * 业务运行时间
     */
    private Long runTime;

    /**
     * 业务运行时间
     */
    private Long endTime;

    /**
     * 工作流实体
     */
    private WorkFlow workFlow;
    /**
     *
     * */
    private WorkFlowService workFlowService;

    /**
     * 持久化服务
     */
    private WorkDaoService workDaoService;

    // ------------------------------------remote---------------------------------------------------

    /**
     * 依赖的其他work flow action name,根据名称获取依赖action状态
     */
//    private static Map<String, List<WorkActionBase>> dependencyWorkAction = new HashMap<>();
    /**
     * 依赖
     */
    private List<DependencyWorkAction> dependendWorkActions = new ArrayList<>();
    /**
     * 调用
     */
    private List<RemoteWorkAction> remoteDepend = new ArrayList<>();

    private Class clazz;

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public List<DependencyWorkAction> getDependendWorkActions() {
        return dependendWorkActions;
    }

    public void setDependendWorkActions(List<DependencyWorkAction> dependendWorkActions) {
        this.dependendWorkActions = dependendWorkActions;
    }

    public WorkFlowService getWorkFlowService() {
        return workFlowService;
    }

    public void setWorkFlowService(WorkFlowService workFlowService) {
        this.workFlowService = workFlowService;
    }

    public List<RemoteWorkAction> getRemoteDepend() {
        return remoteDepend;
    }

    public void setRemoteDepend(List<RemoteWorkAction> remoteDepends) {
        this.remoteDepend = remoteDepends;
    }

    public void addRemoteDepend(RemoteWorkAction remoteDepend) {
        this.remoteDepend.add(remoteDepend);
    }

    // ------------------------------------静态变量，需持久化------------------------------------------

    /**
     * 是否共享
     */
    private boolean share;

    /**
     * action 名称
     */
    private String actionName;

    /**
     * 工作流名称
     */
    private String flowName;

    /**
     * action类型
     */
    private WorkActionType actionType;

    /**
     * 子工作流名字
     */
    private String subWorkFlowName;

    private String formatName;

    /**
     * 有依赖的action
     */
    //  private static List<WorkActionBase> wabs = new ArrayList<>();
    public CacheRelation getCacheRelation() {
        return cacheRelation;
    }

    public void setCacheRelation(CacheRelation cacheRelation) {
        this.cacheRelation = cacheRelation;
    }

    protected final static Logger logger = LoggerFactory.getLogger(WorkActionBase.class);

    public WorkActionBase() {

    }

    public void setFormatName(String formatName) {
        this.formatName = formatName;
    }

    public WorkActionBase(WorkActionType actionType) {
        this.actionType = actionType;
    }

    public String getFormatName() {
        StringBuilder sb = new StringBuilder(50);
        sb.append(this.flowName).append("_").append(this.actionName).append("_").append(this.uuid);
        return sb.toString();
    }

    public void changeStatus(WorkRunStatus status) {
        try {
            this.status = status;
            //更改数据库里面的状态
            this.getWorkDaoService().updateActionStatus(this);
            if (this.workFlow.getSchedulerid() != null) {
                this.workFlow.getWorkSchedulerService().updateSchedulerStatus(this.workFlow, status);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public String getSchedulerid() {
        return schedulerid;
    }

    public void setSchedulerid(String schedulerid) {
        this.schedulerid = schedulerid;
    }

    public void doBeforeStart() {

        this.startTime = System.currentTimeMillis();

        beforeStart();
        logger.info(" action 开始运行 ");
        this.changeStatus(WorkRunStatus.RUNNING);
        this.runTime = System.currentTimeMillis();
        WorkExecutorPool.executorService.submit(this);
    }

    public void doFail() {
        this.fail();
        this.endTime = System.currentTimeMillis();
        this.changeStatus(WorkRunStatus.FAILED);
    }

    public void doKill() {

        this.kill();
        this.endTime = System.currentTimeMillis();
        this.changeStatus(WorkRunStatus.KILLED);
    }

    public void doPause() {
        this.pause();
        this.changeStatus(WorkRunStatus.PAUSE);
    }

    public void doResume() {
        this.resume();
    }

    public void doRunDone() throws InterruptedException {
        runDone();
        if (WorkRunStatus.RUNNING == this.getStatus()) {
            this.endTime = System.currentTimeMillis();
            this.changeStatus(WorkRunStatus.SUCCESS);
        }
    }

    public void notdo() {
        this.not2do();
        this.endTime = System.currentTimeMillis();
        this.changeStatus(WorkRunStatus.NOTDO);
    }

    protected abstract void resume();

    public abstract void execute() throws Exception;

    protected abstract void runDone() throws InterruptedException;

    protected abstract void kill();

    protected abstract void pause();

    protected abstract void fail();

    public abstract void beforeStart();

    protected abstract void not2do();

    protected void doChangeConditionPass(boolean pass) {
        this.changeConditionPass(pass);
        loopChangeConditionPass(this, pass);
    }

    private void loopChangeConditionPass(WorkActionBase action, boolean pass) {

        for (WorkActionBase child : action.listChildActions()) {
            if (child.getActionName() != WorkActionConstant.ENDACTION) {
                child.changeConditionPass(pass);
            }
            if (!child.listChildActions().isEmpty()) {
                loopChangeConditionPass(child, pass);
            }
        }
    }

    @Override
    public void run() {
        doWork();
    }


    public void doWork() {
        if (this.getStatus() != WorkRunStatus.SUCCESS) {
            WorkFlow workFlow = this.getWorkFlow();
            try {
                if (workFlow != null) {
                    workFlow.getRunningActions().put(this.actionName, this);
                }
                //wd_3和wd_2都试一下
//                if (this.actionName.equals("action_3")) {
//                    throw new RuntimeException("测试fail的action级别的重启");
//                }
                logger.info("执行任务 execute: {}", this.actionName);
//                Thread.sleep(20000);
                this.execute();//执行方法
//                int i = 1 / 0;
                this.statusTransfer(this, WorkFlowEvent.DONE);//运行完这个线程就变成success
                //远程启动服务
                this.remoteWorkAction();
                //检查并启动sub action
                this.checkChildren();
            } catch (Exception e) {
                logger.error("action运行时报错 ", e);
                this.setWorkFlowLogger(e.getLocalizedMessage());
                /**
                 * 远程报错停用我放在tryRun方法里面的
                 */
                this.tryRun(e);
            } finally {
                this.close();
                workFlow.getRunningActions().remove(this.actionName);
            }
        }
    }

    private void tryRun(Throwable exception) {
        if (this.getWorkFlow().getTime() <= this.getWorkFlow().getTyrTime()) {
            try {
                this.getWorkFlow().setTime(this.getWorkFlow().getTime() + 1);
                logger.info(" 尝试次数: {}", this.getWorkFlow().getTime());
                this.statusTransfer(this, WorkFlowEvent.DONE);
            } catch (InterruptedException e) {
                logger.info(" 发生了错误: ", e);
                exception = e;
            } finally {
                this.tryRun(exception);
            }
        } else {
            logger.error("action因为此错误而重试: ", exception);
            /**
             * 停止本生workFlow的action
             */
            this.doFail(exception);
//            if (this.getWorkFlow().getCoordinators() != null) {
//                /**
//                 * 改变scheduler实例的状态也在这个方法里面
//                 */
//                remoteSchedulerFlowFail();
//                remoteFlowFail(exception);
//            } else {
            remoteFlowFail(exception);
//            }
            if (this.getWorkFlow().getEmails() != null && this.getWorkFlow().getEmails().size() > 0) {
                this.sendEmailByWorkFlow(exception);
            }
        }
    }

    public void sendEmailByWorkFlow(Throwable exception) {
        WorkExecutorPool.submitWork(() -> {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", Constant.SMTP_HOST);
            props.put("mail.transport.protocol", Constant.EMAIL_PROTOCOL);
            Session mailSession = Session.getInstance(
                    props,
                    new MyAuthenticator(Constant.FROM_ADDRESS, Constant.FROM_EMAILL_PASSWORD));
            Transport transport = null;
            try {
                InternetAddress fromAddress = new InternetAddress(Constant.FROM_ADDRESS);
                Message msg = new MimeMessage(mailSession);
                msg.setSubject(Constant.EMAILL_SUBJECT);
                msg.setFrom(fromAddress);
                msg.setContent(this.emailMsg(exception), "text/html;charset=UTF-8");
                msg.setSentDate(Calendar.getInstance().getTime());
                transport = mailSession.getTransport();
                transport.connect(Constant.SMTP_HOST, Constant.AUTHORIZATION_CODE);
                Address[] addresses = new Address[this.getWorkFlow().getEmails().size()];
                for (int i = 0; i < this.getWorkFlow().getEmails().size(); i++) {
                    addresses[i] = new InternetAddress(this.getWorkFlow().getEmails().get(i));
                }
                transport.sendMessage(msg, addresses);
                this.setWorkFlowLogger(" send error email succeed");
                logger.info(" send error email succeed");
            } catch (MessagingException e) {
                e.printStackTrace();
                this.setWorkFlowLogger(e.toString());
                logger.error(e.toString());
            } finally {
                try {
                    if (transport != null) {
                        transport.close();
                    }
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private String emailMsg(Throwable exception) {
        StackTraceElement[] messages = exception.getStackTrace();
        int length = messages.length;
        StringBuffer error = new StringBuffer();
        for (int i = 0; i < length; i++) {
//            error.append("ClassName:" + messages[i].getClassName() + "\n");
//            error.append("getFileName:" + messages[i].getFileName() + "\n");
//            error.append("getLineNumber:" + messages[i].getLineNumber() + "\n");
//            error.append("getMethodName:" + messages[i].getMethodName() + "\n");
            error.append(messages[i].toString() + "<br>");
        }
        EmailMsg emailMsg = new EmailMsg();
        emailMsg.setAction(this.getActionName());
        emailMsg.setIp(this.getWorkFlow().getIp());
        emailMsg.setWorkFlow(this.getFlowName());
        emailMsg.setTime(new Date());
        emailMsg.setError(error.toString());
        return JSONObject.toJSONString(emailMsg);
    }

    public void addDepencyWorkAction(DependencyWorkAction dependencyWorkAction) {
        this.dependendWorkActions.add(dependencyWorkAction);
    }

    public void cleanChildren(String childrenName) {
        for (int i = 0; i < this.children.size(); i++) {
            if (this.children.get(i).getActionName().equals(childrenName)) {
                this.children.remove(i);
            }
        }
    }

    public void cleanFather(String fatherName) {
        for (int i = 0; i < this.fathers.size(); i++) {
            if (this.fathers.get(i).getActionName().equals(fatherName)) {
                this.fathers.remove(i);
            }
        }
    }

    class EmailMsg {
        private String ip;
        private String workFlow;
        private String action;
        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        private Date time;
        private String error;

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getWorkFlow() {
            return workFlow;
        }

        public void setWorkFlow(String workFlow) {
            this.workFlow = workFlow;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public Date getTime() {
            return time;
        }

        public void setTime(Date time) {
            this.time = time;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }

    public void remoteFlowFail(Throwable exeception) {
        Throwable finalExeception = exeception;
        for (RemoteWorkFlow remoteWorkFlow : this.getWorkFlow().getRemoteWorkFlows()) {
            if (remoteWorkFlow.isSubmit()) {
                if (!remoteWorkFlow.getRemoteWorkFlowInstanceId().equals(this.getWorkFlow().getInstanceid())) {
                    remoteFail(finalExeception, remoteWorkFlow);
                }
            } else {
                if (this.actionName.equals(remoteWorkFlow.getRemotActionName())) {
                    remoteFailStart(finalExeception);
                }
            }
        }
    }

    private void remoteFailStart(Throwable finalExeception) {
        ReferenceConfig<IWebExecutorController> ref = this.getWorkFlow().dubboComsuer("remoteFail");
        if (this.remoteDepend.size() > 0) {
            for (RemoteWorkAction remoteWorkAction : this.remoteDepend) {
                WorkFlowInstance instance = this.getWorkFlow().getWorkDaoService().selectWFByFatherId(this.getWorkFlow().getFatherInstanceId(), remoteWorkAction.getWorkFlowName());
                remoteWorkAction.setWorkFlowInstanceid(instance.getInstanceid());
                remoteWorkAction.setIp(this.getWorkFlow().getWorkDaoService().getLocalIp());
                if (instance.getStatus() == WorkRunStatus.DISTRIBUTED) {
                    this.getWorkFlow().getWorkDaoService().updateWorkFlowInstance(instance, remoteWorkAction);
                } else if (instance.getStatus() == WorkRunStatus.RUNNING ||
                        instance.getStatus() == WorkRunStatus.PENDING) {
                    remoteWorkAction.setStatus(instance.getStatus());
                    ref.setUrl(instance.getExecuteIP());
                    RemoteWorkFlow rwf = new RemoteWorkFlow();
                    rwf.setRemoteWorkFlowName(this.flowName);
                    rwf.setExceptionMessage(finalExeception.getMessage());
                    rwf.setRemoteWorkFlowInstanceId(instance.getInstanceid());
                    rwf.setRemoteIp(this.getWorkFlow().getWorkDaoService().getLocalIp());
                    rwf.setRemotActionName(this.actionName);
                    rwf.setDependActionName(remoteWorkAction.getWorkActionName());
                    EventResponse eventResponse = ref.get().remoteFail(JSONObject.toJSONString(rwf));
                    if (eventResponse.getStatus() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
                        logger.error(eventResponse.getDesc(), eventResponse);
                    }
                }
            }
        }
    }

    private void remoteFail(Throwable finalExeception, RemoteWorkFlow remoteWorkFlow) {
        String remoteIp = remoteWorkFlow.getRemoteIp();
        ReferenceConfig<IWebExecutorController> ref = this.getWorkFlow().dubboComsuer("remoteFail");
        ref.setUrl(remoteIp);
        remoteWorkFlow.setRemoteWorkFlowName(this.flowName);
        remoteWorkFlow.setExceptionMessage(finalExeception.getMessage());
        EventResponse eventResponse = ref.get().remoteFail(JSONObject.toJSONString(remoteWorkFlow));
        this.setWorkFlowLogger(" remoteFlowFail ip is " + remoteIp);
        if (eventResponse.getStatus() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
            logger.error(eventResponse.getDesc(), eventResponse);
        }
    }

    private void remoteWorkAction() throws ParseActionException, CloneNotSupportedException, UnknownHostException, NoParamException, ParamDataTypeException {
//        if ("scheduler1_2".equals(this.actionName) || "scheduler1_3".equals(this.actionName)) {
//            System.out.println("------------------------");
//        }
//        Map<String, WorkFlow> cachedFlowInstance = WorkFlowService.cachedFlowInstance;
//        System.out.println(cachedFlowInstance);
        try {
            if (this.remoteDepend.size() > 0) {
                for (RemoteWorkAction remoteWorkAction : remoteDepend) {
                    WorkFlow workFlow = this.getWorkFlow();
                    ReferenceConfig<IWebExecutorController> ref = workFlow.dubboComsuer("dependAction");
                    String leftSchedulerInstance = workFlow.getSchedulerid();
                    if (leftSchedulerInstance != null) {
                        WorkDaoService workDaoService = workFlow.getWorkDaoService();
                        long calculate = remoteWorkAction.getCalculate();
                        String rightSchedulerInstance = remoteWorkAction.getDependSchedulerInstanceid();
                        WorkSchedulerInstance leftScheduler = workDaoService.querySchedulerInstanceByWorkFlowInstanceid(workFlow.getInstanceid());
                        long leftVersion = leftScheduler.getVersion();
                        long rightVersion = workDaoService.queryRightVersion(leftVersion, calculate);
                        WorkSchedulerInstance rightScheduler = workDaoService.querySchedulerInstanceByScheduleridAndVersion(rightSchedulerInstance, rightVersion);
                        /**
                         * 当右边的scheduler实例还没有加载出来的时候
                         */
                        if (rightScheduler == null) {
                            System.out.println("---------------");
                        }
                        String rightSchedulerWfInstanceid = rightScheduler.getWfInstanceid();
                        if (rightSchedulerWfInstanceid == null) {
                            String relationsJson = rightScheduler.getRelationsJson();
                            Map<String, WorkActionRelation> relationMap = JSONObject.parseObject(relationsJson, new TypeReference<Map<String, WorkActionRelation>>() {
                            });
                            for (String key : relationMap.keySet()) {
                                if (key.equals(remoteWorkAction.getRemoteDependName())) {
                                    relationMap.remove(key);
                                }
                            }
                            String jsonString = JSONObject.toJSONString(relationMap);
                            rightScheduler.setRelationsJson(jsonString);
                            workDaoService.updateSchedulerRelation(rightScheduler.getInstanceid(), rightVersion, jsonString);
                        } else {
                            remoteWorkAction.setWorkFlowInstanceid(rightSchedulerWfInstanceid);
                            String rightSchedulerWFIp = workDaoService.queryWorkFlowInstanceIp(rightSchedulerWfInstanceid);
                            ref.setUrl(rightSchedulerWFIp);
                            remoteWorkAction.setIp(rightScheduler.getExecuteIp());
                            remoteWorkAction.setRemoteWorkFlowName(workFlow.getFlowName());
                            logger.info(" left scheduler 发送的RemoteWorkAction: {}", remoteWorkAction.toString());
                            ref.get().dependAction(JSONObject.toJSONString(remoteWorkAction));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }
    }

    private void checkChildren() throws Exception {
        if (this.children != null && this.children.size() > 0) {

            for (WorkActionBase action : this.children) {

                if (action.checkFatherActionsStatus() == WorkRunStatus.SUCCESS &&
                        (action.checkDependActionsStatus() == WorkRunStatus.SUCCESS || action.checkDependActionsStatusInDataBase())) {

                    if (action.submit() && (!startOtherWorkFlowAction)) {
                        try {
                            statusTransfer(action, WorkFlowEvent.SUBMIT);
                            String msg = "begin action:" + action.getFormatName();
                            logger.info(msg);
                            this.setWorkFlowLogger(msg);
                        } catch (InterruptedException e) {
                            logger.error("when statusTransfer this action for submit , the action name is {}", action.getActionName(), e);
                            this.setWorkFlowERRORLogger(e);
                            closeOutPutStream();
                        }
                    }
                }
            }
        }
    }

    private boolean checkDependActionsStatusInDataBase() {
        try {
            WorkSchedulerService workSchedulerService = this.workFlow.getWorkSchedulerService();
            if (workSchedulerService == null) {
                return true;
            }
            WorkSchedulerInstance rightInstance = workSchedulerService.querySchedulerInstance(this.workFlow.getInstanceid());
            AcceptSchedulerObject rightConfig = workSchedulerService.querySchedulerConfig(rightInstance.getSchedulerName());
            List<Bundle> bundles = rightConfig.getBundles();
            List<DependencyWorkAction> dependendWorkActions = this.getDependendWorkActions();
            if (dependendWorkActions != null && dependendWorkActions.size() > 0) {
                for (DependencyWorkAction dependendWorkAction : dependendWorkActions) {
                    Long rightInstanceVersion = rightInstance.getVersion();
                    for (Bundle bundle : bundles) {
                        if (bundle.getRemoteInstanceid().equals(dependendWorkAction.getSchedulerid())) {
                            long calculate = bundle.getCalculate();
                            long leftVersion = workSchedulerService.calculatoRemoteVersion(rightInstanceVersion, calculate);
                            WorkSchedulerInstance leftSchedulerInstance = workSchedulerService.querySchedulerByInstanceidAndVersion(dependendWorkAction.getSchedulerid(), leftVersion);
                            String wfInstanceid = leftSchedulerInstance.getWfInstanceid();
                            if (wfInstanceid != null) {
                                WorkActionInstance workActionBase = this.workDaoService.queryActionByWFidAndActionName(wfInstanceid, dependendWorkAction.getRemoteName());
                                if (workActionBase == null) {
                                    return false;
                                } else {
                                    if (!WorkRunStatus.SUCCESS.equals(workActionBase.getStatus())) {
                                        return false;
                                    }
                                }
                            } else {
                                return false;
                            }
                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * 如果拓扑图的其中一个action挂了(即其中一条线挂了),不应该清除该workFlow内存的引用
     *
     * @param e
     */
    public void doFail(Throwable e) {

        logger.error("action do fail", e);
        this.setWorkFlowERRORLogger(e);
        this.doFail();
        try {
            this.getWorkDaoService().updateWorkFlowInstanceToFail(this.workFlow, WorkRunStatus.FAILED);
            if (this.workFlow.getSchedulerid() == null) {
                this.getWorkDaoService().updateSchedulerInstanceStatus(this.workFlow, WorkRunStatus.FAILED);
            }
        } catch (Exception e1) {
            String msg = " update workFlowInstance to Fail has error,workFlow name is " + this.getWorkFlow().getFlowName();
            this.setWorkFlowERRORLogger(e);
            logger.error(msg, e1);
            closeOutPutStream();
        }
    }

    private void closeOutPutStream() {
        try {
            FileOutputStream outputStream = this.getWorkFlow().getOutputStream();
            outputStream.close();
            outputStream = null;
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    /**
     * 线程开关
     *
     * @return
     */
    private boolean submit() {
        if (this.submit) {
            submit = false;
            return true;
        }
        return false;
    }

    /**
     * 检查父类所有动作状态,如果父类的所有动作都完成了,则开始执行此动作
     */
    public WorkRunStatus checkFatherActionsStatus() {

        for (WorkActionBase action : this.fathers) {

            if (action.conditionPass) {
                continue;
            }

            if (action.getStatus() != WorkRunStatus.SUCCESS) {
                return this.getStatus();
            }

        }

        return WorkRunStatus.SUCCESS;
    }

    private WorkRunStatus checkDependActionsStatus() {
        if (dependendWorkActions.size() <= 0) {

            return WorkRunStatus.SUCCESS;

        } else {

            int i = 0;
            for (DependencyWorkAction dependencyWorkAction : this.dependendWorkActions) {
                if (dependencyWorkAction.isRemoteSwitch()) {
                    i++;
                }
            }
            if (i == dependendWorkActions.size()) {
                return WorkRunStatus.SUCCESS;
            }
            return WorkRunStatus.FAILED;
        }
    }

    public WorkRunStatus getStatus() {

        if (this.conditionPass) {
            return WorkRunStatus.SUCCESS;
        }
        return this.status;

    }

    public abstract String getLog();

    /**
     * 统一管理状态转移
     */
    public boolean statusTransfer(WorkActionBase action, WorkFlowEvent event)
            throws InterruptedException {
        boolean success = false;

        switch (event) {

            case KILL:
                if (action.getStatus() == WorkRunStatus.RUNNING
                        || action.getStatus() == WorkRunStatus.PENDING) {
                    action.doKill();
                } else if (action.getStatus() == WorkRunStatus.DISTRIBUTED) {
                    action.notdo();
                }
                success = true;
                break;

            case FAIL:
                if (action.getStatus() == WorkRunStatus.PENDING
                        || action.getStatus() == WorkRunStatus.RUNNING) {
                    action.doFail();
                    success = true;
                }
                break;

            case PAUSE:
                if (action.getStatus() == WorkRunStatus.PENDING
                        || action.getStatus() == WorkRunStatus.RUNNING) {
                    action.doPause();
                    success = true;
                }
                break;
            case SUBMIT:
                if (action.getStatus() != WorkRunStatus.SUCCESS
                        && action.getStatus() != WorkRunStatus.RUNNING) {
                    //这个方法开启线程
                    action.doBeforeStart();
                    success = true;
                }

                break;

            case RESUME:
                if (action.getStatus() != WorkRunStatus.SUCCESS
                        && action.getStatus() != WorkRunStatus.RUNNING) {
                    action.doResume();
                    success = true;
                }
                break;

            case DONE:
                if (action.getStatus() == WorkRunStatus.RUNNING) {
                    action.doRunDone();
                    success = true;
                }
                break;

            default:
                break;

        }

//        if (!success) {
//            String msg = "statusTransfer Method :instanceid is" + action.getInstanceid() + " status: " + action.getStatus() + " event:" + event;
//            this.setWorkFlowLogger(msg);
//        }
        return success;

    }


    public void changeAllActionStatus(WorkRunStatus status, WorkFlowEvent event) throws InterruptedException {

        statusTransfer(this.getWorkFlow().getStartWorkAction(), event);
        List<WorkActionBase> actions = this.getWorkFlow().getActions();
        for (WorkActionBase action : actions) {
            statusTransfer(action, event);
        }
        statusTransfer(this.getWorkFlow().getEndWorkAction(), event);
    }

    /**
     * 设置父类动作，包括子工作流
     */
    public void setFatherActions(List<WorkActionBase> fathers) {
        for (WorkActionBase father : fathers) {
            boolean find = false;
            for (WorkActionBase f : this.fathers) {
                if (f.getActionName().equals(father.getActionName()) && f.getFlowName().equals(father.getFlowName())) {
                    find = true;
                    break;
                }
            }
            if (find) {
                logger.warn("action name:{} exist,it will be override", father.getActionName());
            } else {
                this.fathers.add(father);
            }

        }
    }

    public void setFatherActions(WorkActionBase[] fathers) {

        for (WorkActionBase father : fathers) {
            boolean find = false;
            for (WorkActionBase f : this.fathers) {
                if (f.getActionName().equals(father.getActionName()) && f.getFlowName().equals(father.getFlowName())) {
                    find = true;
                    break;
                }
            }
            if (find) {
                logger.warn("action name:{} has exist,it will be override", father.getActionName());
            } else {
                this.fathers.add(father);
            }

        }
    }

    public void setChildrenActions(List<WorkActionBase> children) {
        for (WorkActionBase child : children) {
            boolean find = false;
            for (WorkActionBase ch : this.children) {
                if (ch.getActionName().equals(child.getActionName()) && ch.getFlowName().equals(child.getFlowName())) {
                    find = true;
                    break;
                }
            }
            if (find) {
                logger.warn("action name: {} has exsist,it will be overried", child.getActionName());
            } else {
                this.children.add(child);
            }

        }
    }

    public void setChildrenActions(WorkActionBase[] children) {
        for (WorkActionBase child : children) {
            boolean find = false;
            for (WorkActionBase ch : this.children) {
                if (ch.getActionName().equals(child.getActionName()) && ch.getFlowName().equals(child.getFlowName())) {
                    find = true;
                    break;
                }
            }
            if (find) {
                logger.warn("action name: {} has exsist,it will be overried", child.getFormatName());
            } else {
                this.children.add(child);
            }

        }

    }

    /**
     * status is in notrunning then can fail，工作流自发行为
     */
    protected void loopFail(WorkActionBase action) throws InterruptedException {
        List<WorkActionBase> children = action.listChildActions();
        for (WorkActionBase child : children) {

            statusTransfer(child, WorkFlowEvent.FAIL);

            if (child.listChildActions().size() > 0) {
                loopFail(child);
            }
        }
    }

    /**
     * 只从没有
     */
    protected boolean loopResume(WorkActionBase action) throws InterruptedException {
        if (statusTransfer(action, WorkFlowEvent.RESUME)) {
            return true;
        }
        List<WorkActionBase> children = action.listChildActions();
        for (WorkActionBase child : children) {

            boolean resume = statusTransfer(child, WorkFlowEvent.RESUME);

            if (!resume && child.listChildActions().size() > 0) {
                if (loopResume(child)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setStatus(WorkRunStatus status) {
        this.status = status;
    }

    public List<WorkActionBase> listFatherActions() {
        return fathers;
    }

    public List<WorkActionBase> listChildActions() {
        return children;
    }

    public List<WorkActionBase> getFathers() {
        return fathers;
    }

    public void setFathers(List<WorkActionBase> fathers) {
        this.fathers = fathers;
    }

    public List<WorkActionBase> getChildren() {
        return children;
    }

    public void setChildren(List<WorkActionBase> children) {
        this.children = children;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public abstract Map<String, Object> getOutPut();

    public boolean isConditionPass() {
        return this.conditionPass;
    }

    public void changeConditionPass(boolean pass) {
        this.conditionPass = pass;
    }

    public FileOutputStream setSchedulerLogger(String msg) {

        FileOutputStream outputStream = this.getWorkFlow().getSchedulerInstance().getOutputStream();
        outputStream = loggerMsg(msg, outputStream);
        return outputStream;
    }

    public FileOutputStream setWorkFlowLogger(String msg) {

        WorkFlow workFlow = this.getWorkFlow();
        FileOutputStream outputStream = null;
        if (workFlow != null) {
            outputStream = workFlow.getOutputStream();
        }
        outputStream = loggerMsg(msg, outputStream);
        return outputStream;
    }

    public FileOutputStream loggerMsg(String msg, FileOutputStream outputStream) {
        try {
            StringBuilder sb = new StringBuilder(50);
//            sb.append(new Date().toString()).append(" --- ").append("  [").append(Thread.currentThread().getStackTrace()[2]).append("]  ").append(Thread.currentThread().getName())
//                    .append(":").append(System.getProperty("line.separator")).append(msg).append(System.getProperty("line.separator"));
            sb.append(new Date().toString()).append(" --- ").append("  [").append(Thread.currentThread().getName()).append("] ")
                    .append(msg).append(System.getProperty("line.separator"));
            if (outputStream != null && outputStream.getFD().valid()) {
                outputStream.write(sb.toString().getBytes());
            }

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            try {
                outputStream.close();
                outputStream = null;
                logger.info(" {} stream is closed", this.getActionName());
            } catch (IOException e1) {
                logger.error(e1.getMessage(), e1);
            }
        }
        return outputStream;
    }


    public String setInstanceid(String instanceid) {
        return this.instanceid = instanceid;
    }

    public String getInstanceid() {
        return instanceid;
    }

    public WorkActionType getActionType() {
        return actionType;
    }

    public void setActionType(WorkActionType actionType) {
        this.actionType = actionType;
    }

    public String getSubWorkFlowName() {
        return subWorkFlowName;
    }

    public void setSubWorkFlowName(String subWorkFlowName) {
        this.subWorkFlowName = subWorkFlowName;
    }

    public WorkFlow getSubWorkFlow() {
        return subWorkFlow;
    }

    public void setSubWorkFlow(WorkFlow subWorkFlow) {
        this.subWorkFlow = subWorkFlow;
    }

    public WorkFlow getWorkFlow() {
        return workFlow;
    }

    public void setWorkFlow(WorkFlow workFlow) {
        this.workFlow = workFlow;
        this.setFlowName(workFlow.getFlowName());
        this.setRunTime(this.workFlow.getRunTime());
        this.setUuid(this.workFlow.getUuid());
        this.setInstanceid(this.getFormatName());
        this.setStartTime(System.currentTimeMillis());
        this.setWorkDaoService(this.workFlow.getWorkDaoService());
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getRunTime() {
        return runTime;
    }

    public void setRunTime(Long runTime) {
        this.runTime = runTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public void setConditionPass(boolean conditionPass) {
        this.conditionPass = conditionPass;
    }

    public WorkDaoService getWorkDaoService() {
        return workDaoService;
    }

    public void setWorkDaoService(WorkDaoService workDaoService) {
        this.workDaoService = workDaoService;
    }

    @Override
    public String toString() {
        return "WorkActionBase{" +
                ", instanceid='" + instanceid + '\'' +
                ", conditionPass=" + conditionPass +
                ", status=" + status +
                ", uuid='" + uuid + '\'' +
                ", schedulerid=" + schedulerid +
                ", startTime=" + startTime +
                ", runTime=" + runTime +
                ", endTime=" + endTime +
                ", share=" + share +
                ", actionName='" + actionName + '\'' +
                ", flowName='" + flowName + '\'' +
                ", actionType=" + actionType +
                ", subWorkFlowName='" + subWorkFlowName + '\'' +
                '}';
    }

    private void setWorkFlowERRORLogger(Throwable e) {
        StackTraceElement[] messages = e.getStackTrace();
        int length = messages.length;
        StringBuffer error = new StringBuffer();
        for (int i = 0; i < length; i++) {
//            error.append("ClassName:" + messages[i].getClassName() + "\n");
//            error.append("getFileName:" + messages[i].getFileName() + "\n");
//            error.append("getLineNumber:" + messages[i].getLineNumber() + "\n");
//            error.append("getMethodName:" + messages[i].getMethodName() + "\n");
            error.append("      " + messages[i].toString() + System.getProperty("line.separator"));
        }
        this.setWorkFlowLogger(error.toString());
    }

    abstract protected void close();

    public boolean isSubmit() {
        return submit;
    }

    public void setSubmit(boolean submit) {
        this.submit = submit;
    }

    class MyAuthenticator extends Authenticator {
        String userName = "";
        String password = "";

        public MyAuthenticator() {

        }

        public MyAuthenticator(String userName, String password) {
            this.userName = userName;
            this.password = password;
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(userName, password);
        }
    }

    public abstract void putActionsArguments(Map<String, String> argumentMaps);

}