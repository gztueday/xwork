package com.bigdata.xwork.action.core;

import com.bigdata.xwork.core.service.WorkFlowService;
import com.google.common.annotations.VisibleForTesting;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

@WorkActionTag(desc = "工作流中最后的节点，起标记作用", name = "结束节点")
public class EndWorkAction extends WorkActionBase {


    private WorkRunCallBack callBack;


    public EndWorkAction() {
        super(WorkActionType.ENDACTION);

        this.setActionName(WorkActionConstant.ENDACTION);
    }

    @Override
    public void kill() {

        try {
            this.setWorkFlowLogger("endAction is killed");
            this.getWorkFlow().updateWorkFlowStatus(WorkRunStatus.KILLED);
            if (this.getWorkFlow().getSchedulerid() == null) {
                this.getWorkDaoService().updateSchedulerInstanceStatus(this.getWorkFlow(), WorkRunStatus.KILLED);
            }
            String msg = "name-------------> kill() " + this.getWorkFlow().getFlowName() + "  endWorkAction do";
            logger.info(msg);
            this.setWorkFlowLogger(msg);
            this.getWorkFlow().getOutputStream().close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (this.callBack != null) {
            this.callBack.listen(this);
        }

        this.getWorkFlowService().removeWorkFlowInstance(this.getWorkFlow().getInstanceid());

    }

    @Override
    public void runDone() {

        try {
            this.setWorkFlowLogger(this.getWorkFlow().getInstanceid() + "任务运行成功");
            this.getWorkFlow().updateWorkFlowStatus(WorkRunStatus.SUCCESS);
            if (this.getWorkFlow().getSchedulerid() != null) {
                this.getWorkDaoService().updateSchedulerInstanceStatus(this.getWorkFlow(), WorkRunStatus.SUCCESS);
            }
            this.getWorkFlow().getOutputStream().close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        if (this.callBack != null) {
            this.callBack.listen(this);
        }

        WorkFlow workFlow = this.getWorkFlow();
        String instanceid = workFlow.getInstanceid();
        WorkFlowService workFlowService = this.getWorkFlowService();
        workFlowService.removeWorkFlowInstance(instanceid);

    }

    @Override
    public void fail() {
        this.getWorkFlowService().removeWorkFlowInstance(this.getWorkFlow().getInstanceid());
        try {
            String msg = "name-------------> fail() " + this.getWorkFlow().getFlowName() + "  endWorkAction do";
            logger.info(msg);
            this.setWorkFlowLogger(msg);
            this.getWorkFlow().getOutputStream().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void beforeStart() {


    }

    @Override
    protected void not2do() {
        if (this.callBack != null) {
            this.callBack.listen(this);
        }

        this.getWorkFlowService().removeWorkFlowInstance(this.getWorkFlow().getInstanceid());
        try {
            String msg = "name-------------> not2do() " + this.getWorkFlow().getFlowName() + "  endWorkAction do";
            logger.info(msg);
            this.setWorkFlowLogger(msg);
            this.getWorkFlow().getOutputStream().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void close() {

    }

    @Override
    public void putActionsArguments(Map<String, String> argumentMaps) {

    }

    @Override
    public void resume() {
        try {
            this.setWorkFlowLogger("endAction is resume");
            this.getWorkFlow().updateWorkFlowStatus(WorkRunStatus.RESUME);
            String msg = "name-------------> resume() " + this.getWorkFlow().getFlowName() + "  endWorkAction do";
            logger.info(msg);
            this.setWorkFlowLogger(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute() {
        this.setWorkFlowLogger(this.getActionName() + " 运行成功");


    }

    @Override
    public String getLog() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Object> getOutPut() {
        Map<String, Object> outPut = new HashMap<>();
        outPut.put("EndActionRunTime:", new java.util.Date().toString());
        return outPut;
    }

    @Override
    public void initialize() {
        // TODO Auto-generated method stub

    }

    public WorkRunCallBack getCallBack() {
        return callBack;
    }

    @VisibleForTesting
    public void setCallBack(CountDownLatch count, WorkRunCallBack callBack) {
        this.callBack = callBack;
        this.callBack.setCount(count);
    }

    @Override
    public void pause() {

    }

}
