package com.bigdata.xwork.api.entity;


public interface IWebExecutorController {


//    EventResponse schedulerRemoteRelation(String workFlowInfo, String instanceid);

    EventResponse remote(String workFlowString);

    LineResult querySchedulerLogger(String schedulerInstance, long size);

    /**
     * 远程确定指定dependAction
     *
     * @param dependAction
     * @return
     */
    EventResponse dependAction(String dependAction);

    EventResponse remoteFail(String remoteWorkFlow);

    EventResponse doKill(String remoteWorkFlow);

    EventResponse resume(String instance);

    void removePendingStatus(String instanceid);

    int queue();

    String bundleInMemory(String remoteWorkFlow);

    EventResponse dispatcherScheduler(String scheduler);

    void schedulerDoKill(String schedulerInstanceid);

    EventResponse schedulerResume(String instanceid);

    LineResult queryWorkFlowLogger(String instanceid, long size);
}