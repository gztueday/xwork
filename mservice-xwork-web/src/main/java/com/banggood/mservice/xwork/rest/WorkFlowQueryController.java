package com.banggood.mservice.xwork.rest;

import com.alibaba.fastjson.JSONObject;
import com.banggood.xwork.action.core.WorkRunStatus;
import com.banggood.xwork.core.common.WorkFlowConfigProperty;
import com.banggood.xwork.core.exception.ParseActionException;
import com.banggood.xwork.core.service.WorkActionService;
import com.banggood.xwork.core.service.WorkFlowService;
import com.banggood.xwork.dao.entity.WorkActionInstance;
import com.banggood.xwork.dao.entity.WorkFlowInfo;
import com.banggood.xwork.query.WorkFlowConfigQueryObject;
import com.banggood.xwork.query.WorkFlowInstanceQueryObject;
import com.banggood.xwork.query.WorkFlowNameQueryObject;
import com.banggood.xwork.query.result.ConfigResult;
import com.banggood.xwork.api.entity.LineResult;
import com.banggood.xwork.query.result.PageResult;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.util.StringUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/xwork")
@Api(value = "任务调度系统查询模块", description = "任务调度系统的查询", produces = "application/json", protocols = "http")
public class WorkFlowQueryController {

    @Autowired
    private WorkActionService workActionService;
    @Autowired
    private WorkFlowService workFlowService;
    @Autowired
    private WorkFlowConfigProperty wfcp;

    private final static Logger logger = LoggerFactory.getLogger(WorkFlowQueryController.class);

    /**
     * 查询workFlowInstance配置接口
     *
     * @param instanceId
     * @return
     */
    @RequestMapping(value = {"/workInfo/one"}, method = {RequestMethod.GET})
    @ApiOperation(value = "workFlowInstance配置页面", code = 200, httpMethod = "GET", response = String.class)
    public String workFlowInstanceConfig(String instanceId) {
        return this.workFlowService.getWorkFlowInstanceConfig(instanceId);
    }

    @RequestMapping(value = {"/workInfo/oneByName"}, method = {RequestMethod.GET})
    @ApiOperation(value = "通过具体的workFlowName查询指定名称的workFlow", notes = "返回单个workFlow配置", code = 200, httpMethod = "GET", response = String.class)
    @ApiImplicitParam(value = "workFlow的名称(String类型)"
            , name = "workFlowName", dataType = "String"
            , required = true, paramType = "query")
    @ApiResponses({@ApiResponse(code = 500, message = "后台代码出错"), @ApiResponse(code = 404, message = "找不到对应的接口")})
    public WorkFlowInfo getWorkInfoOneByName(String workFlowName) {
        return this.workFlowService.getWorkInfoOneByName(workFlowName);
    }

    @RequestMapping(value = {"/action/instance"}, method = {RequestMethod.GET})
    @ApiOperation(value = "通过workFlow实例的instanceid查询对应action实例", notes = "返回action的集合", code = 200, httpMethod = "GET", response = String.class)
    @ApiImplicitParam(value = "workFlow的id(String类型)"
            , name = "instanceid", dataType = "String"
            , required = true, paramType = "query")
    @ApiResponses({@ApiResponse(code = 500, message = "后台代码出错"), @ApiResponse(code = 404, message = "找不到对应的接口")})
    public List<WorkActionInstance> selectWorkAction(String instanceid) {
        return this.workFlowService.queryActionInstanceByWFName(instanceid);
    }

    /**
     * ModelAttribute
     *
     * @param qoStr
     * @return
     */
    @RequestMapping(value = {"/name"}, method = {RequestMethod.POST})
    @ApiOperation(value = "通过名称查询workFlow配置", notes = "编辑和创建页面接口", code = 200, httpMethod = "POST", response = String.class)
//    @ApiImplicitParam(value = "workFlow配置的名称", name = "qoStr", dataType = "String"
//            , required = false, paramType = "query")
    @ApiResponses({@ApiResponse(code = 500, message = "后台代码出错"), @ApiResponse(code = 404, message = "找不到对应的接口")})
    public PageResult selectWorkConfigByName(@RequestBody String qoStr) {
        WorkFlowNameQueryObject qo;
        if (qoStr != null && "".equals(qoStr)) {
            qo = new WorkFlowNameQueryObject();
        } else {
            qo = JSONObject.parseObject(qoStr, WorkFlowNameQueryObject.class);
        }
        return workFlowService.queryWorkFlowConfigByName(qo);
    }

    @RequestMapping(value = {"/config/workFlow"}, method = {RequestMethod.GET})
    @ApiOperation(value = "配置", code = 200, httpMethod = "GET")
    @ApiImplicitParam(value = "instanceid", name = "instanceid",
            dataType = "String", required = true, paramType = "query")
    @ApiResponses({@ApiResponse(code = 500, message = "后台代码出错"), @ApiResponse(code = 404, message = "找不到对应的接口")})
    public List<ConfigResult> configWorkFlow(String instanceid) {
        List<ConfigResult> stringStringMap = new ArrayList<>();
        try {
            stringStringMap = this.workFlowService.configWorkFlow(instanceid);
        } catch (ParseActionException | CloneNotSupportedException e) {
            logger.error(e.getMessage(), e);
        }
        return stringStringMap;
    }

    /**
     * 查询日志接口
     *
     * @return
     */
    @RequestMapping(value = {"/logs"}, method = {RequestMethod.GET})
    @ApiOperation(value = "查询workFlow日志", notes = "根据Instanceid查询对应workFlow的日志", code = 200, httpMethod = "GET", response = String.class)
    @ApiImplicitParams({
            @ApiImplicitParam(value = "workFlow的id(String类型)", name = "instanceid", dataType = "String"
                    , required = true, paramType = "query"),
            @ApiImplicitParam(value = "size(long类型)", name = "size", dataType = "long"
                    , required = true, paramType = "query")})
    @ApiResponses({@ApiResponse(code = 500, message = "后台代码出错"), @ApiResponse(code = 404, message = "找不到对应的接口")})
    public LineResult logger(String instanceid, long size) {
        return this.workFlowService.queryWorkFlowLogger(instanceid, size);
    }

    @RequestMapping(value = {"/query/workFlowList"}, method = {RequestMethod.POST, RequestMethod.OPTIONS})
    @ApiOperation(value = "获取所有已有的workFlow配置", notes = "获取所有已有的workFlow配置"
            , code = 200, httpMethod = "GET", response = PageResult.class)
    @ApiResponses({@ApiResponse(code = 500, message = "后台代码出错"), @ApiResponse(code = 404, message = "找不到对应的接口")})
    public PageResult queryWorkFlow(@RequestBody String qoStr) {
        WorkFlowConfigQueryObject qo;
        if (qoStr == null) {
            qo = new WorkFlowConfigQueryObject();
        } else {
            qo = JSONObject.parseObject(qoStr, WorkFlowConfigQueryObject.class);
        }
        return this.workFlowService.getWorkFlowAll(qo);
    }

    @RequestMapping(value = {"/query/workFlowInstanceForStatus"}, method = RequestMethod.POST)
    @ApiOperation(value = "WorkFlowInstance状态", notes = "查询不同状态的workFlowInstance", code = 200, httpMethod = "POST", response = String.class)
    @ApiResponses({@ApiResponse(code = 500, message = "后台代码出错"), @ApiResponse(code = 404, message = "找不到对应的接口")})
    public PageResult queryWorkFlowInstance(@RequestBody String qoStr) {
        WorkFlowInstanceQueryObject qo;
        if (qoStr == null) {
            qo = new WorkFlowInstanceQueryObject();
        } else {
            qo = JSONObject.parseObject(qoStr, WorkFlowInstanceQueryObject.class);
        }
        return this.workFlowService.query(qo);
    }

    @RequestMapping(value = {"/query/work/requestObj"}, method = RequestMethod.GET)
    @ApiOperation(value = "编辑返回数据接口", notes = "点击编辑返回RequsetObj", code = 200, httpMethod = "GET", response = String.class)
    @ApiImplicitParam(value = "workFlow配置的名称", name = "flowName", dataType = "String"
            , required = false, paramType = "query")
    @ApiResponses({@ApiResponse(code = 500, message = "后台代码出错"), @ApiResponse(code = 404, message = "找不到对应的接口")})
    public WorkFlowInfo queryRequestObj(String flowName) {
        return this.workFlowService.queryRequestObj(flowName);
    }

    /**
     * 获取当前action的详细信息
     *
     * @return
     */
    @RequestMapping(value = {"/action/actionAll"}, method = RequestMethod.GET)
    @ApiOperation(value = "获取所有已有的action", notes = "获取已有action的详细信息"
            , code = 200, httpMethod = "GET", response = String.class)
    @ApiResponses({@ApiResponse(code = 500, message = "后台代码出错"), @ApiResponse(code = 404, message = "找不到对应的接口")})
    public List<WorkActionService.WorkActionDescription> getActionsInfos() {
        return this.workActionService.getWorkActionDescriptions();
    }

    /**
     * 查看所有已经添加了的action
     *
     * @param showName
     * @return
     */
    @RequestMapping(value = "/action/actionOne", method = RequestMethod.GET)

    @ApiOperation(value = "获取指定已有的action", notes = "获取已有的action", code = 200, httpMethod = "GET", response = String.class)
    @ApiImplicitParams({
            @ApiImplicitParam(value = "action名称", name = "showName", dataType = "String", required = true, paramType = "query")})
    @ApiResponses({@ApiResponse(code = 500, message = "后台代码出错"), @ApiResponse(code = 401, message = "showName is null"), @ApiResponse(code = 404, message = "找不到对应的接口")
            , @ApiResponse(code = 403, message = "can not find action by  actionName(参数不对)"), @ApiResponse(code = 100, message = "can find action success")})
    public HttpRespone getActionsInfo(String showName) {
        HttpRespone resp = new HttpRespone();
        if (StringUtil.isEmpty(showName)) {
            resp.setDesc("parameter showName is null,please check!");
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return resp;
        }

        WorkActionService.WorkActionDescription description = this.workActionService.getWorkActionDescription(showName);

        if (description == null) {
            resp.setDesc("can not find action by  actionName:" + showName + ",please check!");
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);

            return resp;
        } else {
            resp.setDesc("has been find action success");
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setData(description);

            return resp;
        }

    }

    public WorkRunStatus choseType(Integer type) {
        WorkRunStatus workRunStatus = null;
        switch (type) {
            case 1:
                workRunStatus = WorkRunStatus.SUCCESS;
                break;
            case 2:
                workRunStatus = WorkRunStatus.KILLED;
                break;
            case 3:
                workRunStatus = WorkRunStatus.RUNNING;
                break;
            default:
                break;
        }
        return workRunStatus;
    }
}
