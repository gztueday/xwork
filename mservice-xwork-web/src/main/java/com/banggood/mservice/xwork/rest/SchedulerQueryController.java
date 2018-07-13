package com.banggood.mservice.xwork.rest;

import com.alibaba.fastjson.JSONObject;
import com.banggood.xwork.action.core.WorkActionBase;
import com.banggood.xwork.core.common.WorkFlowConfigProperty;
import com.banggood.xwork.core.exception.ParseActionException;
import com.banggood.xwork.dao.entity.Bundle;
import com.banggood.xwork.dao.entity.WorkFlowInfo;
import com.banggood.xwork.dao.entity.WorkSchedulerInstance;
import com.banggood.xwork.query.*;
import com.banggood.xwork.query.result.ConfigResult;
import com.banggood.xwork.api.entity.LineResult;
import com.banggood.xwork.query.result.PageResult;
import com.banggood.xwork.scheduler.core.service.WorkSchedulerService;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by zouyi on 2018/2/6.
 */
@RestController
@RequestMapping("/scheduler/query")
@Api(value = "定时任务调度系统查询模块", description = "定时任务调度系统查询模块"
        , produces = "application/json", protocols = "http")
public class SchedulerQueryController {

    private final static Logger logger = LoggerFactory.getLogger(SchedulerQueryController.class);
    @Autowired
    private WorkSchedulerService workSchedulerService;
    @Autowired
    private WorkFlowConfigProperty wfcp;

    @RequestMapping(value = {"/logs"}, method = {RequestMethod.GET})
    @ApiOperation(value = "查询workFlow日志", notes = "根据Instanceid查询对应workFlow的日志", code = 200, httpMethod = "GET", response = String.class)
    @ApiImplicitParams({
            @ApiImplicitParam(value = "scheduler的id(String类型)"
                    , name = "schedulerInstance", dataType = "String"
                    , required = true, paramType = "query"),
            @ApiImplicitParam(value = "size(long类型)"
                    , name = "size", dataType = "long"
                    , required = true, paramType = "query")})
    @ApiResponses({@ApiResponse(code = 500, message = "后台代码出错"), @ApiResponse(code = 404, message = "找不到对应的接口")})
    public LineResult logger(String schedulerInstance, long size) {
        return workSchedulerService.querySchedulerLogger(schedulerInstance, size);
    }

    /**
     * 这个接口未写
     *
     * @param schedulerInstance
     * @return
     */
    @RequestMapping(value = {"/status/log"}, method = {RequestMethod.GET})
    @ApiOperation(value = "配置", code = 200, httpMethod = "GET")
    @ApiImplicitParam(value = "scheduler相关联的remote's scheduler", name = "schedulerInstance",
            dataType = "String", required = true, paramType = "query")
    @ApiResponses({@ApiResponse(code = 500, message = "后台代码出错"), @ApiResponse(code = 404, message = "找不到对应的接口")})
    public List<ConfigResult> log(String schedulerInstance) {
        return this.workSchedulerService.configSchedulerConfig(schedulerInstance);
    }

    @RequestMapping(value = {"/config/scheduler"}, method = {RequestMethod.GET})
    @ApiOperation(value = "配置", code = 200, httpMethod = "GET")
    @ApiImplicitParam(value = "scheduler相关联的remote's scheduler", name = "schedulerInstance",
            dataType = "String", required = true, paramType = "query")
    @ApiResponses({@ApiResponse(code = 500, message = "后台代码出错"), @ApiResponse(code = 404, message = "找不到对应的接口")})
    public AcceptSchedulerObject configScheduler(String schedulerInstance) {
        return this.workSchedulerService.configScheduler(schedulerInstance);
    }

    @RequestMapping(value = {"/status/scheduler"}, method = {RequestMethod.GET})
    @ApiOperation(value = "scheduler", code = 200, httpMethod = "GET")
    @ApiImplicitParam(value = "scheduler相关联的remote's scheduler", name = "schedulerInstance",
            dataType = "String", required = true, paramType = "query")
    @ApiResponses({@ApiResponse(code = 500, message = "后台代码出错"), @ApiResponse(code = 404, message = "找不到对应的接口")})
    public List<Bundle> statusScheduler(String schedulerInstance) {
        return this.workSchedulerService.statusScheduler(schedulerInstance);
    }

    @RequestMapping(value = {"/status/calendar"}, method = {RequestMethod.POST})
    @ApiOperation(value = "calendar", code = 200, httpMethod = "POST")
    @ApiResponses({@ApiResponse(code = 500, message = "后台代码出错"), @ApiResponse(code = 404, message = "找不到对应的接口")})
    public PageResult statusCalendar(@RequestBody String qoStr) {
        SchedulerStatusInstanceQueryObject qo;
        if (qoStr != null && "".equals(qoStr)) {
            qo = new SchedulerStatusInstanceQueryObject();
        } else {
            qo = JSONObject.parseObject(qoStr, SchedulerStatusInstanceQueryObject.class);
        }
        return this.workSchedulerService.statusCalendar(qo);
    }

    @RequestMapping(value = {"/bundle/actionsRight"}, method = {RequestMethod.GET})
    @ApiOperation(value = "Actions", code = 200, httpMethod = "GET")
    @ApiResponses({@ApiResponse(code = 500, message = "后台代码出错"), @ApiResponse(code = 404, message = "找不到对应的接口")})
    public List<WorkActionBase> querySchedulerBundleRightActios(String flowName) {

        try {
            return workSchedulerService.querySchedulerBundleRightActios(flowName);
        } catch (ParseActionException | CloneNotSupportedException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 查询出绑定的action
     *
     * @param actionsObjectStr
     * @return
     */
    @RequestMapping(value = {"/bundle/actions"}, method = {RequestMethod.POST})
    @ApiOperation(value = "Actions", code = 200, httpMethod = "POST")
    @ApiResponses({@ApiResponse(code = 500, message = "后台代码出错"), @ApiResponse(code = 404, message = "找不到对应的接口")})
    public List<WorkActionBase> querySchedulerBundleActios(@RequestBody String actionsObjectStr) {
        QuerySchedulerActionsObject qo;
        if (actionsObjectStr != null) {
            qo = JSONObject.parseObject(actionsObjectStr, QuerySchedulerActionsObject.class);
        } else {
            qo = new QuerySchedulerActionsObject();
        }
        try {
            return this.workSchedulerService.querySchedulerBundleActions(qo);
        } catch (ParseActionException | CloneNotSupportedException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 查询出绑定的日期
     *
     * @param instance
     * @return
     */
    @RequestMapping(value = {"/bundle/ID"}, method = {RequestMethod.GET})
    @ApiOperation(value = "ID", code = 200, httpMethod = "GET")
    @ApiImplicitParam(value = "可以通过scheduler的名称搜索", name = "instance", dataType = "String",
            required = true, paramType = "query")
    @ApiResponses({@ApiResponse(code = 500, message = "后台代码出错"), @ApiResponse(code = 404, message = "找不到对应的接口")})
    public List<VersionResult> querySchedulerBundleDate(String instance) {
        return this.workSchedulerService.querySchedulerBundleDate(instance);
    }

    /**
     * 通过SchedulerInstance实例的id查询执行日期
     *
     * @param schedulerName
     * @return
     */
    @RequestMapping(value = {"/bundle/schedulerName"}, method = {RequestMethod.GET})
    @ApiOperation(value = "Scheduler", code = 200, httpMethod = "GET")
    @ApiImplicitParam(value = "可以通过scheduler的名称搜索", name = "schedulerName", dataType = "String",
            required = false, paramType = "query")
    @ApiResponses({@ApiResponse(code = 500, message = "后台代码出错"), @ApiResponse(code = 404, message = "找不到对应的接口")})
    public List<WorkSchedulerInstance> querySchedulerBundleName(String schedulerName) {
        List<WorkSchedulerInstance> workSchedulerInstances = this.workSchedulerService.querySchedulerBundleName(schedulerName);
        return workSchedulerInstances;
    }

    @RequestMapping(value = {"/workFlowName"}, method = {RequestMethod.GET})
    @ApiOperation(value = "My workFlow", code = 200, httpMethod = "GET")
    @ApiImplicitParam(value = "可以通过workFlow的名称搜索", name = "workFlowName", dataType = "String", required = false, paramType = "query")
    @ApiResponses({@ApiResponse(code = 500, message = "后台代码出错"), @ApiResponse(code = 404, message = "找不到对应的接口")})
    public List<WorkFlowInfo> queryForWorkFlowName(String workFlowName) {
        return this.workSchedulerService.queryWorkFlowByName(workFlowName);
    }

    @RequestMapping(value = {"/schedulerConfig"}, method = {RequestMethod.POST})
    @ApiOperation(value = "查询scheduler配置", code = 200, httpMethod = "POST",
            response = PageResult.class)
    @ApiResponses({@ApiResponse(code = 500, message = "后台代码出错"), @ApiResponse(code = 404, message = "找不到对应的接口")})
    public PageResult query(@RequestBody String qoStr) {
        logger.info("定时调度任务查询");
        SchedulerConfigQueryObject qo;
        if ((qoStr != null && "".equals(qoStr)) || null == qoStr) {
            qo = new SchedulerConfigQueryObject();
        } else {
            qo = JSONObject.parseObject(qoStr, SchedulerConfigQueryObject.class);
        }
        return this.workSchedulerService.query(qo);
    }

    @RequestMapping(value = {"/schedulerInstance"}, method = {RequestMethod.POST})
    @ApiOperation(value = "查询scheduler运行实例", code = 200, httpMethod = "POST",
            response = PageResult.class)
    @ApiResponses({@ApiResponse(code = 500, message = "后台代码出错"), @ApiResponse(code = 404, message = "找不到对应的接口")})
    public PageResult queryForSchedulerInstance(@RequestBody String qoStr) {
        logger.info("定时调度任务实例查询");
        SchedulerInstanceQueryObject qo;
        if ((qoStr != null && "".equals(qoStr)) || null == qoStr) {
            qo = new SchedulerInstanceQueryObject();
        } else {
            qo = JSONObject.parseObject(qoStr, SchedulerInstanceQueryObject.class);
        }
        return this.workSchedulerService.queryForSchedulerInstance(qo);
    }

}
