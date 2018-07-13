package com.banggood.mservice.xwork.rest;

import com.alibaba.fastjson.JSONObject;
import com.banggood.xwork.api.entity.EventResponse;
import com.banggood.xwork.core.exception.NoParamException;
import com.banggood.xwork.core.exception.ParamDataTypeException;
import com.banggood.xwork.core.exception.ParseActionException;
import com.banggood.xwork.query.AcceptSchedulerObject;
import com.banggood.xwork.query.DeleteQueryObject;
import com.banggood.xwork.scheduler.core.DispatcherScheduler;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/scheduler")
@Api(value = "定时任务调度系统操作模块", description = "定时任务调度系统操作模块"
        , produces = "application/json", protocols = "http")
public class SchedulerController {

    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private DispatcherScheduler dispatcherScheduler;

    private final static Logger logger = LoggerFactory.getLogger(SchedulerController.class);

    @RequestMapping(value = {"/batchDelete"}, method = {RequestMethod.POST})
    @ApiOperation(value = "定时任务批量删除", code = 200, httpMethod = "POST", response = String.class)
    @ApiResponses({
            @ApiResponse(code = 403, message = "传入的参数不对")
            , @ApiResponse(code = 500, message = "后台代码出错")
            , @ApiResponse(code = 404, message = "找不到对应的接口")
    })
    public EventResponse deleteSchedulerConfig(@RequestBody String deleteQueryObjectJson) {
        EventResponse eventResponse = new EventResponse();
        DeleteQueryObject deleteQueryObject = JSONObject.parseObject(deleteQueryObjectJson, DeleteQueryObject.class);
        String[] delete = batchDelete(deleteQueryObject);
        eventResponse.setStatus(HttpServletResponse.SC_OK);
        eventResponse.setDesc(this.dispatcherScheduler.deleteSchedulerConfig(delete));
        return eventResponse;
    }

    private String[] batchDelete(DeleteQueryObject deleteQueryObject) {
        List<String> list = deleteQueryObject.getList();
        String[] delete = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            delete[i] = list.get(i);
        }
        return delete;
    }

    @RequestMapping(value = {"/start"}, method = {RequestMethod.POST})
    @ApiOperation(value = "定时任务开始运行", code = 200, httpMethod = "POST", response = String.class)
    @ApiResponses({
            @ApiResponse(code = 403, message = "传入的参数不对")
            , @ApiResponse(code = 500, message = "后台代码出错")
            , @ApiResponse(code = 404, message = "找不到对应的接口")
    })
    public EventResponse start(@RequestBody String schedulerStartObject) {
        EventResponse resp = new EventResponse();
        JSONObject jsonObject = JSONObject.parseObject(schedulerStartObject);
        try {
            this.dispatcherScheduler.start(jsonObject.getString("schedulerName"), jsonObject.getString("workFlowConfig"));
        } catch (ParseActionException | NoParamException | ParamDataTypeException | ParseException e) {
            logger.error(" 调度系统开始时报错: ", e);
            resp.setDesc(e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return resp;
        }
        resp.setDesc("success ");
        resp.setStatus(HttpServletResponse.SC_OK);
        logger.info("定时调度任务运行成功");
        return resp;
    }

    @RequestMapping(value = {"/insert"}, method = {RequestMethod.POST})
    @ApiOperation(value = "保存workScheduler配置", code = 200, httpMethod = "POST",
            response = String.class)
    @ApiResponses({
            @ApiResponse(code = 403, message = "传入的参数不对")
            , @ApiResponse(code = 500, message = "后台代码出错")
            , @ApiResponse(code = 404, message = "找不到对应的接口")
    })
    public EventResponse insert(@RequestBody String object) {
        EventResponse resp = new EventResponse();
        AcceptSchedulerObject acceptSchedulerObject = JSONObject.parseObject(object).getObject("object", AcceptSchedulerObject.class);
        try {
            checkSchedulerConfig(acceptSchedulerObject);
            this.dispatcherScheduler.insertScheduler(acceptSchedulerObject);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setDesc(" 定时任务保存成功 ");
        } catch (Exception e) {
            resp.setDesc(e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            logger.error(e.getMessage(), e);
        }
        return resp;
    }

    public void checkSchedulerConfig(AcceptSchedulerObject acceptSchedulerObject) {
        Date startDate = acceptSchedulerObject.getStartDate();
        Date endDate = acceptSchedulerObject.getEndDate();
        if (startDate != null && endDate != null) {
            if (startDate.getTime() >= endDate.getTime()) {
                throw new RuntimeException("结束时间必须要比开始时间晚,请检查!");
            }
        }
    }

    @RequestMapping(value = {"/dokill"}, method = {RequestMethod.GET})
    @ApiOperation(value = "定时人dokill接口", notes = "通过指定schedulerInstanceid杀死此定时任务"
            , httpMethod = "GET", response = String.class)
    @ApiImplicitParam(value = "定时任务运行实例id", name = "schedulerInstanceid", dataType = "String",
            required = true, paramType = "query")
    @ApiResponses({
            @ApiResponse(code = 403, message = "传入的参数不对")
            , @ApiResponse(code = 500, message = "后台代码出错")
            , @ApiResponse(code = 404, message = "找不到对应的接口")
    })
    public EventResponse doKill(String schedulerInstanceid) {
        return this.dispatcherScheduler.dokill(schedulerInstanceid);
    }

    @RequestMapping(value = {"/resume"}, method = {RequestMethod.GET})
    @ApiOperation(value = "重启定时任务接口", notes = "通过指定schedulerInstanceId重启定时任务"
            , httpMethod = "GET", response = String.class)
    @ApiImplicitParam(value = "定时任务运行实例id", name = "schedulerInstanceid", dataType = "String",
            required = true, paramType = "query")
    @ApiResponses({
            @ApiResponse(code = 403, message = "传入的参数不对")
            , @ApiResponse(code = 500, message = "后台代码出错")
            , @ApiResponse(code = 404, message = "找不到对应的接口")
    })
    public EventResponse resume(String schedulerInstanceid) {
        return this.dispatcherScheduler.resume(schedulerInstanceid);
    }

}