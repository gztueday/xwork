package com.banggood.xwork.action.impl;

import com.banggood.xwork.action.core.WorkActionBase;
import com.banggood.xwork.action.core.WorkActionParam;
import com.banggood.xwork.action.core.WorkActionParam.ParamDataType;
import com.banggood.xwork.action.core.WorkActionTag;
import com.banggood.xwork.action.core.WorkActionType;
import com.banggood.xwork.core.exception.NoParamException;
import com.banggood.xwork.core.exception.ParamDataTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

@WorkActionTag(desc = "演示demo，只打印日志", name = "demo")
public class DemoAction extends WorkActionBase {

    private final static Logger logger = LoggerFactory.getLogger(DemoAction.class);

    private Map<String, Object> outPut = new HashMap<>();


    public DemoAction() {
        super(WorkActionType.NORMORL);
        this.registerParam("sleep", new WorkActionParam(true, ParamDataType.STRING));
        outPut.put("random", 6);
    }


    @Override
    public void kill() {

        String msg = "DemoAction: kill action instanceid:" + this.getFormatName();
        logger.info(msg);
        this.setWorkFlowLogger(msg);

        try {
            this.getWorkFlow().getOutputStream().close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void fail() {

        logger.info("fail action instanceid:" + this.getFormatName());
    }


    @Override
    public void resume() {

        logger.info("resume action instanceid:" + this.getFormatName());

    }

    @Override
    public String getLog() {

        return null;
    }

    @Override
    public void beforeStart() {


    }

    @Override
    protected void not2do() {
        logger.info("not2do action instanceid:" + this.getFormatName());
    }

    @Override
    protected void close() {

    }

    @Override
    public void putActionsArguments(Map<String, String> argumentMaps) {

    }

    @Override
    public void execute() throws UnknownHostException {

        Object sleep = this.getConfig().getParameters().get("sleep").getContent();
        String msg = "sleep:" + sleep;
        logger.info(msg);
        this.setWorkFlowLogger(msg);
        int loop = 0;
        while (loop < 10) {
            //System.out.println("work aciton:"+this.getFormatName()+" deal num:"+loop);
            try {
                msg = "DemoAction:  " + this.getActionName() + "  :  " + this.getFormatName() + " deal num:" + loop + "address:" + InetAddress.getLocalHost().getHostAddress();
                logger.info(msg);
                this.setWorkFlowLogger(msg);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            loop++;

            try {
                Thread.sleep(Long.valueOf(sleep.toString()));
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        msg = this.getActionName() + "is DemoAction execute success , address:" + InetAddress.getLocalHost().getHostAddress();
        logger.info(msg);
        this.setWorkFlowLogger(msg);
    }


    @Override
    public Map<String, Object> getOutPut() {
        // TODO Auto-generated method stub
        return outPut;
    }


    @Override
    public void initialize() {
        // TODO Auto-generated method stub

    }


    @Override
    public void runDone() {

        logger.info("action instanceid:" + this.getFormatName() + " run done");

    }

    public void setSleep(long sleep) throws NoParamException, ParamDataTypeException {
        this.putToConfig("sleep", sleep);
    }

    public void setSleepUseVarible() throws NoParamException, ParamDataTypeException {
        this.putToConfig("sleep", "${sleep_1}");
    }


    @Override
    public void pause() {

    }

}
