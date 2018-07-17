package com.bigdata.xwork.action.core;

import java.util.HashMap;
import java.util.Map;

@WorkActionTag(desc = "工作流中最开始的节点，起标记作用", name = "开始结点")
public class StartWorkAction extends WorkActionBase {


    public StartWorkAction() {
        super(WorkActionType.STARTACTION);

        this.setActionName(WorkActionConstant.STARTACTION);
    }

    @Override
    public void kill() {

        logger.info("kill action:" + this.getActionName() + " status:" + this.getStatus());

    }

    @Override
    public void fail() {


    }

    @Override
    public void beforeStart() {


    }

    @Override
    protected void not2do() {
        logger.info("not2do action:" + this.getActionName() + " status:" + this.getStatus());
    }

    @Override
    protected void close() {

    }

    @Override
    public void putActionsArguments(Map<String, String> argumentMaps) {

    }

    @Override
    public void resume() {


    }

    private static int startAction = 0;

    @Override
    public void execute() {
        this.setWorkFlowLogger(this.getActionName() + " 运行成功");
        StringBuilder sb = new StringBuilder(50);
        Map<String, Map<String, String>> arguments = this.getWorkFlow().getArguments();
        sb.append("任务的参数解析 :");
        for (String actionName : arguments.keySet()) {
            Map<String, String> stringStringMap = arguments.get(actionName);
            sb.append("action[").append(actionName).append("]中的");
            for (String key : stringStringMap.keySet()) {
                sb.append(":[").append(key).append("解析为").append(stringStringMap.get(key)).append("]").append(";");
            }
            sb.append(";");
        }
        this.setWorkFlowLogger(sb.toString());
    }

    @Override
    public String getLog() {

        return null;
    }

    @Override
    public Map<String, Object> getOutPut() {
        Map<String, Object> outPut = new HashMap<>();
        outPut.put("StartActionRunTime:", new java.util.Date().toString());
        return outPut;
    }

    @Override
    public void initialize() {


    }

    @Override
    public void runDone() {


    }

    @Override
    public void pause() {


    }

}
