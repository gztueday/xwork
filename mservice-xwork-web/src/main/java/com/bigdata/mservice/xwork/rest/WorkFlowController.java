package com.bigdata.mservice.xwork.rest;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.banggood.xwork.action.core.WorkActionParam.ParamDataType;
import com.banggood.xwork.core.exception.NoParamException;
import com.banggood.xwork.core.exception.ParamDataTypeException;
import com.banggood.xwork.core.exception.ParseActionException;
import com.banggood.xwork.core.exception.RelationshipException;
import com.banggood.xwork.core.service.WorkActionService.WorkActionDescription;
import com.banggood.xwork.core.service.WorkFlowService;
import com.banggood.xwork.dao.entity.UserInfo;
import com.banggood.xwork.dao.entity.WorkActionTransfer;
import com.banggood.xwork.dao.entity.WorkFlowConfig;
import com.banggood.xwork.dao.service.WorkDaoService;
import com.banggood.xwork.query.DeleteQueryObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.util.StringUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * workFlow的操作类
 */
@RestController
@RequestMapping("/xwork")
@Api(value = "任务调度系统操作模块", description = "任务调度系统的操作模块"
        , produces = "application/json", protocols = "http")
public class WorkFlowController {

    @Autowired
    private WorkFlowService workFlowService;
    @Autowired
    private WorkDaoService workDaoService;
    @Autowired
    private FlowActionUtils workActionFlowUtils;

    private final static Logger logger = LoggerFactory.getLogger(WorkFlowController.class);

    private String startWorkFlowByName(String flowName, JSONArray parameters)
            throws ParseActionException, CloneNotSupportedException, NoParamException,
            ParamDataTypeException, UnknownHostException {

        WorkFlow workFlow = this.workDaoService.getWorkFlow(flowName);
        workFlow.initialize();
        workFlow.setStatus(WorkRunStatus.DISTRIBUTED);
        workFlow.setRunTime(System.currentTimeMillis());
        workFlow.setStartTime(System.currentTimeMillis());

        if (parameters != null) {
            workFlowService.parseRunParam(parameters, workFlow);
        }
        workFlow.build();
        logger.info("workFlow: [{}]", workFlow.toString());
        return this.workDaoService.addWorkFlowInstance(workFlow, workFlow.getInstanceid());
    }

    @RequestMapping(value = {"/config/queue"}, method = {RequestMethod.GET})
    @ApiOperation(value = "查询集群正在运行的实例数量", notes = "传入workFlow名称删除对应workFlow", code = 200
            , httpMethod = "POST", response = Boolean.class)
    public HttpRespone queue() {
        HttpRespone httpRespone = new HttpRespone();
        try {
            httpRespone.setData(this.workFlowService.queue());
            httpRespone.setStatus(HttpServletResponse.SC_OK);
        } catch (UnsupportedEncodingException e) {
            logger.error("/config/queue 接口报错: ", e);
            httpRespone.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            httpRespone.setDesc(e.toString());
        }
        return httpRespone;
    }

    @RequestMapping(value = {"/config/delete"}, method = {RequestMethod.POST})
    @ApiOperation(value = "删除workFlow配置", notes = "传入workFlow名称删除对应workFlow", code = 200
            , httpMethod = "POST", response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 500, message = "后台代码出错"),
            @ApiResponse(code = 404, message = "找不到对应的接口")})
    public HttpRespone deleteXWorkConfig(@RequestBody String deleteQueryObjectJson) {
        HttpRespone httpRespone = new HttpRespone();
        DeleteQueryObject deleteQueryObject = JSONObject.parseObject(deleteQueryObjectJson, DeleteQueryObject.class);
        String[] delete = batchDelete(deleteQueryObject);
        httpRespone.setStatus(HttpServletResponse.SC_OK);
        httpRespone.setData(this.workFlowService.deleteWorkFlowInfo(delete));
        return httpRespone;
    }

    @RequestMapping(value = {"/workFlowInstance/delete"}, method = {RequestMethod.POST})
    @ApiOperation(value = "批量删除workFlow实例", notes = "(id是数组)传入workFlow实例id删除对应workFlow实例", code = 200
            , httpMethod = "POST", response = Boolean.class)
    @ApiResponses({@ApiResponse(code = 500, message = "后台代码出错"),
            @ApiResponse(code = 404, message = "找不到对应的接口")})
    public HttpRespone deleteXWorkInstance(@RequestBody String deleteQueryObjectJson) {
        HttpRespone httpRespone = new HttpRespone();
        DeleteQueryObject deleteQueryObject = JSONObject.parseObject(deleteQueryObjectJson, DeleteQueryObject.class);
        String[] delete = batchDelete(deleteQueryObject);
        httpRespone.setStatus(HttpServletResponse.SC_OK);
        httpRespone.setData(this.workFlowService.deleteWorkFlowInstance(delete));
        return httpRespone;
    }

    private String[] batchDelete(DeleteQueryObject deleteQueryObject) {
        List<String> list = deleteQueryObject.getList();
        String[] delete = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            delete[i] = list.get(i);
        }
        return delete;
    }

    @RequestMapping(value = {"/instance/resume"}, method = {RequestMethod.POST})
    @ApiOperation(value = "workFlowInstance操作", notes = "resume操作"
            , code = 200, httpMethod = "POST", response = String.class)
    @ApiResponses({@ApiResponse(code = 403, message = "传入的参数不对"), @ApiResponse(code = 500, message = "后台代码出错")
            , @ApiResponse(code = 404, message = "找不到对应的接口")
            , @ApiResponse(code = 404, message = "找不到对应的接口")})
    public HttpRespone resume(@RequestBody String deleteQueryObject) {
        HttpRespone resp = new HttpRespone();
        String instanceId = JSONObject.parseObject(deleteQueryObject).getString("deleteQueryObject");
        try {
            String msg = this.workDaoService.resume(instanceId);
            resp.setDesc(JSONObject.toJSONString(msg));
            resp.setStatus(HttpServletResponse.SC_OK);
            return resp;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resp.setDesc(e.toString());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return resp;
        }
    }

    @RequestMapping(value = {"/instance/dokill"}, method = {RequestMethod.POST})
    @ApiOperation(value = "workFlowInstance操作", notes = "dokill操作"
            , code = 200, httpMethod = "POST", response = String.class)
    @ApiResponses({@ApiResponse(code = 403, message = "传入的参数不对"), @ApiResponse(code = 500, message = "后台代码出错")
            , @ApiResponse(code = 404, message = "找不到对应的接口")
            , @ApiResponse(code = 404, message = "找不到对应的接口")})
    public HttpRespone doKill(@RequestBody String deleteQueryObject) {
        HttpRespone resp = new HttpRespone();
        DeleteQueryObject deletes = JSONObject.parseObject(deleteQueryObject, DeleteQueryObject.class);
        List<String> list = deletes.getList();
        List<String> msgs = new ArrayList<>(list.size());
        try {
            for (String instanceId : list) {
                String msg = this.workDaoService.doKill(instanceId);
                msgs.add(msg);
            }
            resp.setDesc(JSONObject.toJSONString(msgs));
            resp.setStatus(HttpServletResponse.SC_OK);
            return resp;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resp.setDesc(e.toString());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return resp;
        }
    }

    @RequestMapping(value = {"/resume/action"}, method = {RequestMethod.GET})
    @ApiOperation(value = "没有依赖的重启action", notes = "重启单个Action", code = 200
            , httpMethod = "GET", response = String.class)
    @ApiResponses({@ApiResponse(code = 500, message = "后台代码出错"),
            @ApiResponse(code = 404, message = "找不到对应的接口")})
    public HttpRespone actionResume(String id) {
        HttpRespone resp = new HttpRespone();
        try {
            this.workFlowService.resumeOneAction(id);
            resp.setDesc("重启单个Action成功");
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            resp.setDesc("重启单个Action失败");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            logger.error(e.getMessage(), e);
        }
        return resp;
    }

    /**
     * 测试用,马上执行的方法
     *
     * @param workFlowNames
     * @return
     */
    @RequestMapping(value = {"/remote/submit"}, method = {RequestMethod.GET})
    @ApiOperation(value = "远程workFlow调用", notes = "远程workFlow调用", code = 200
            , httpMethod = "GET", response = String.class)
    @ApiResponses({@ApiResponse(code = 500, message = "后台代码出错"),
            @ApiResponse(code = 404, message = "找不到对应的接口")})
    public HttpRespone dependRemoteWrokFlow(String workFlowNames) {
        HttpRespone resp = new HttpRespone();
        String[] wfns = new String[]{workFlowNames};
        try {
            resp.setData(this.workFlowService.remote(wfns, false));
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setDesc(" remote procedure call has been successed ");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setDesc(e.toString());
            logger.error("dependRemoteWrokFlow: ", e);
        }
        return resp;
    }

    /**
     * 先把实例保存入数据库,从数据库中扫描出实例来启动
     *
     * @param workFlowConfig
     * @return
     */
    @RequestMapping(value = {"/instance/start"}, method = {RequestMethod.POST})
    @ApiOperation(value = "创建workFlowInstance"
            , notes = "通过指定workFlow的配置创建workFlowInstance,并保存进数据库(work_flow_instance这张表)"
            , code = 200, httpMethod = "POST", response = String.class)
    @ApiResponses({@ApiResponse(code = 403, message = "传入的参数不对"), @ApiResponse(code = 500, message = "后台代码出错"), @ApiResponse(code = 404, message = "找不到对应的接口")})
    public HttpRespone startInstance(@RequestBody String workFlowConfig) {
        HttpRespone resp = new HttpRespone();
        JSONObject jsonObject = JSONObject.parseObject(workFlowConfig);
        try {
            String instanceid = startWorkFlowByName(jsonObject.getString("workFlowName"), jsonObject.getJSONArray("workFlowConfig"));
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setDesc(" insert flowInstance success");
            resp.setData(instanceid);
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setDesc(" created instance error: " + e.getMessage());
            logger.error("创建workFlowInstance:", e);
        }
        return resp;
    }

    @RequestMapping(value = "/flow/update", method = RequestMethod.POST)
    @ApiOperation(value = "更新一个新的workFlow", notes = "传入的对象为workFlowConfig,json格式"
            , code = 200, httpMethod = "POST", response = String.class)
    @ApiResponses({@ApiResponse(code = 500, message = "后台代码出错"), @ApiResponse(code = 401, message = "showName is null"), @ApiResponse(code = 404, message = "找不到对应的接口")
            , @ApiResponse(code = 403, message = "can not find action by  actionName(参数不对)")
            , @ApiResponse(code = 100, message = "can find action success")})
    public HttpRespone updateWorkFlow(@RequestBody String workFlowConfig) {
        HttpRespone resp = new HttpRespone();
        if (checkWorkFlowConfig(workFlowConfig, resp)) {
            return resp;
        }
        try {
            WorkFlowConfig config = JSONObject.parseObject(workFlowConfig, WorkFlowConfig.class);
            this.checkAction(config);
            if (config.getEmails() != null && config.getEmails().size() > 0) {
                this.checkEmail(config.getEmails());
            }
            List<WorkActionBase> actions = new ArrayList<>();
            List<DependencyWorkAction> wfns = new ArrayList<>();
            List<RemoteWorkAction> rwa = new ArrayList<>();
            Map<String, WorkActionRelation> newRelations = config.getRelations();
            WorkFlow workFlow = this.workDaoService.getWorkFlowForUUID(config.getUuid());

            Map<String, WorkActionRelation> oldRelations = workFlow.getRelations();
            for (WorkActionDescription descriptor : config.getDescriptors()) {
                WorkActionBase action = this.workActionFlowUtils.copyAction(descriptor.getClazz());
                action.setConfig(descriptor.getConfigs());
                action.setActionName(descriptor.getActionName());
                action.setCacheRelation(descriptor.getCacheRelation());

                List<DependencyWorkAction> newDependWorkActions = newRelations.get(action.getActionName())
                        .getDependWorkActions();
                if (newDependWorkActions != null && newDependWorkActions.size() > 0) {
                    action.setDependendWorkActions(newDependWorkActions);
                    wfns.addAll(newDependWorkActions);

                    for (DependencyWorkAction newDependWorkAction : newDependWorkActions) {
                        WorkActionRelation oldWorkActionRelation = oldRelations.get(action.getActionName());
                        if (oldWorkActionRelation != null) {
                            DependencyWorkAction oldDependencyWorkAction = oldWorkActionRelation.findDependWorkAction(newDependWorkAction.getWorkActionName());
                            if (oldDependencyWorkAction == null) {
                                /**
                                 * 从workFlow_relations表中查询出来
                                 */
                                WorkFlow wf = getWorkFlow(newDependWorkAction, config);

                                Map<String, WorkActionRelation> relations = wf.getRelations();
                                WorkActionRelation workActionRelation = relations.get(newDependWorkAction.getWorkActionName());
                                workActionRelation.updateRemoteRelation(config.getFlowName(), action.getActionName());

                                addWorkFlowRelation(config, wf, relations, newDependWorkAction.getWorkFlowName(), config.getFlowName());

                            }
                        }
                    }
                }

                List<RemoteWorkAction> newRemoteDepends = newRelations.get(action.getActionName())
                        .getRemoteDepends();
                if (newRemoteDepends != null && newRemoteDepends.size() > 0) {
                    action.setRemoteDepend(newRemoteDepends);
                    rwa.addAll(newRemoteDepends);

                    for (RemoteWorkAction newRemoteDepend : newRemoteDepends) {
                        WorkActionRelation oldWorkActionRelation = oldRelations.get(action.getActionName());
                        if (oldWorkActionRelation != null) {
                            RemoteWorkAction remoteWorkAction = oldWorkActionRelation.findRemoteWorkAction(newRemoteDepend.getWorkActionName());
                            if (remoteWorkAction == null) {
                                WorkFlow wf = getWorkFlow(newRemoteDepend, config);
                                Map<String, WorkActionRelation> relations = wf.getRelations();
                                WorkActionRelation workActionRelation = relations.get(newRemoteDepend.getWorkActionName());
                                workActionRelation.updateDependRelation(config.getFlowName(), action.getActionName());

                                addWorkFlowRelation(config, wf, relations, newRemoteDepend.getWorkFlowName(), config.getFlowName());

                            }
                        }
                    }
                }
                actions.add(action);
            }

            for (String oldKey : oldRelations.keySet()) {
                WorkActionRelation oldWorkActionRelation = oldRelations.get(oldKey);
                WorkActionRelation newWorkActionRelation = newRelations.get(oldKey);
                if (newWorkActionRelation != null) {

                    List<DependencyWorkAction> oldDependWorkActions = oldWorkActionRelation.getDependWorkActions();
                    if (oldDependWorkActions != null && oldDependWorkActions.size() > 0) {
                        for (DependencyWorkAction oldDependWorkAction : oldDependWorkActions) {
                            DependencyWorkAction dependWorkAction = newWorkActionRelation.findDependWorkAction(oldDependWorkAction.getWorkActionName());
                            if (dependWorkAction == null) {
                                WorkFlow wf = getWorkFlow(oldDependWorkAction, config);
                                Map<String, WorkActionRelation> relations = wf.getRelations();
                                WorkActionRelation workActionRelation = relations.get(oldDependWorkAction.getWorkActionName());
                                workActionRelation.removeRemoteRelation(config.getFlowName(), oldKey);

                                addWorkFlowRelation(config, wf, relations, oldDependWorkAction.getWorkFlowName(), config.getFlowName());

                            }
                        }
                    }

                    List<RemoteWorkAction> oldRemoteDepends = oldWorkActionRelation.getRemoteDepends();
                    if (oldRemoteDepends != null && oldRemoteDepends.size() > 0) {
                        for (RemoteWorkAction oldRemoteDepend : oldRemoteDepends) {
                            RemoteWorkAction remoteWorkAction = newWorkActionRelation.findRemoteWorkAction(oldRemoteDepend.getWorkActionName());
                            if (remoteWorkAction == null) {
                                WorkFlow wf = getWorkFlow(oldRemoteDepend, config);
                                Map<String, WorkActionRelation> relations = wf.getRelations();
                                WorkActionRelation workActionRelation = relations.get(oldRemoteDepend.getWorkActionName());
                                workActionRelation.removeDependRelation(config.getFlowName(), oldKey);
                                addWorkFlowRelation(config, wf, relations, oldRemoteDepend.getWorkFlowName(), config.getFlowName());
                            }
                        }
                    }
                }
            }
            WorkFlow flow = new WorkFlow();
            flow.setEmails(config.getEmails());
            /**
             * 依赖显示
             */
            flow.setDependName("");
            flow.setFlowName(config.getFlowName());
            flow.setConfig(config.getFlowConfig());
            flow.setActions(actions);
            flow.setRelations(newRelations);
            flow.initialize();
            flow.build();
            UserInfo user = this.workDaoService.getUserInfoByName("mxq");
            this.workDaoService.checkDeleteRelation(config.getFlowName());
            if (this.workDaoService.updateWorkFlow(flow, user, config)) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setDesc("create flow success!");
                return resp;
            }
        } catch (NoParamException | ParamDataTypeException | ParseActionException | CloneNotSupportedException |
                IllegalAccessException | InstantiationException | ClassNotFoundException | RelationshipException e) {
            logger.error("updateWorkFlow", e);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setDesc(e.toString());
            return resp;
        }
        resp.setDesc(" when insert into database , has error(may be the workFlowName has been repeated) ");
        resp.setStatus(HttpServletResponse.SC_RESET_CONTENT);
        return resp;
    }

    @RequestMapping(value = "/flow/insert", method = RequestMethod.POST)
    @ApiOperation(value = "创建一个新的workFlow", notes = "传入的对象为workFlowConfig,json格式"
            , code = 200, httpMethod = "POST", response = String.class)
    @ApiResponses({@ApiResponse(code = 500, message = "后台代码出错"), @ApiResponse(code = 401, message = "showName is null"), @ApiResponse(code = 404, message = "找不到对应的接口")
            , @ApiResponse(code = 403, message = "can not find action by  actionName(参数不对)")
            , @ApiResponse(code = 100, message = "can find action success")})
    public HttpRespone insertWorkFlow(@RequestBody String workFlowConfig) {

        HttpRespone resp = new HttpRespone();
        if (checkWorkFlowConfig(workFlowConfig, resp)) {
            return resp;
        }
        JSONObject jsonObject = JSONObject.parseObject(workFlowConfig);
        try {
            WorkFlowConfig config = JSONObject.parseObject(jsonObject.getString("workFlowConfig"), WorkFlowConfig.class);
            String paramsJson = config.getParamsJson();
            if (paramsJson != null) {
                JSONArray jsonArray = JSONObject.parseArray(paramsJson);
                Map<String, Map<String, String>> parseRunParamMap = this.workFlowService.parseRunParam(jsonArray);
                config.setParams(parseRunParamMap);
            }
            this.checkAction(config);
            if (this.workFlowService.getWorkFlowForCount(config.getFlowName()) > 0) {
                this.workFlowService.realDeWF(config.getFlowName());
            }
            if (config.getEmails() != null && config.getEmails().size() > 0) {
                this.checkEmail(config.getEmails());
            }
            List<WorkActionBase> actions = new ArrayList<>();
            List<DependencyWorkAction> wfns = new ArrayList<>();
            List<RemoteWorkAction> rwa = new ArrayList<>();
            Map<String, WorkActionRelation> relations = config.getRelations();

            for (WorkActionDescription descriptor : config.getDescriptors()) {
                WorkActionBase action = this.workActionFlowUtils.copyAction(descriptor.getClazz());
                action.setConfig(descriptor.getConfigs());
                action.setActionName(descriptor.getActionName());
                action.setCacheRelation(descriptor.getCacheRelation());
                List<DependencyWorkAction> dependWorkActions = relations.get(action.getActionName())
                        .getDependWorkActions();
                if (dependWorkActions != null && dependWorkActions.size() > 0) {
                    action.setDependendWorkActions(dependWorkActions);
                    wfns.addAll(dependWorkActions);
                    for (DependencyWorkAction dependWorkAction : dependWorkActions) {
                        WorkFlow workFlow = getWorkFlow(dependWorkAction, config);
                        Map<String, WorkActionRelation> r = workFlow.getRelations();
                        WorkActionRelation workActionRelation = r.get(dependWorkAction.getWorkActionName());
                        if (workActionRelation == null) {
                            throw new RelationshipException(" 找不到这个action [" + dependWorkAction.getWorkActionName() + "] 在当前普通任务: " + workFlow);
                        } else {
                            RemoteWorkAction remoteWorkAction = workActionRelation.findRemoteWorkAction(dependWorkAction.getWorkActionName());
                            if (remoteWorkAction == null) {
                                workActionRelation.updateRemoteRelation(config.getFlowName(), action.getActionName());
                            }
                        }
                        addWorkFlowRelation(config, workFlow, r, dependWorkAction.getWorkFlowName(), config.getFlowName());
                    }
                }

                List<RemoteWorkAction> remoteDepends = relations.get(action.getActionName()).getRemoteDepends();
                if (remoteDepends != null && remoteDepends.size() > 0) {
                    action.setRemoteDepend(remoteDepends);
                    rwa.addAll(remoteDepends);
                    for (RemoteWorkAction remoteDepend : remoteDepends) {

                        WorkFlow workFlow = getWorkFlow(remoteDepend, config);

                        Map<String, WorkActionRelation> r = workFlow.getRelations();
                        WorkActionRelation workActionRelation = r.get(remoteDepend.getWorkActionName());
                        if (workActionRelation == null) {
                            throw new RelationshipException(" 找不到这个action [" + remoteDepend.getWorkActionName() + "] 在当前普通任务: " + workFlow);
                        } else {
                            DependencyWorkAction dependencyWorkAction = workActionRelation.findDependWorkAction(remoteDepend.getWorkActionName());
                            if (dependencyWorkAction == null) {
                                workActionRelation.updateDependRelation(config.getFlowName(), action.getActionName());
                            }
                        }
                        addWorkFlowRelation(config, workFlow, r, remoteDepend.getWorkFlowName(), config.getFlowName());
                    }
                }
                actions.add(action);
            }

            StringBuilder sb = new StringBuilder(50);
            List<String> temp = new ArrayList<>();

            wfns.stream().map((dwa) -> dwa.getWorkFlowName()).forEach((x) -> {
                for (DependencyWorkAction wfn : wfns) {
                    if (!temp.contains(x)) {
                        temp.add(x);
                    }
                }
            });

            for (int i = 0; i < temp.size(); i++) {
                sb.append(temp.get(i));
                if (i < temp.size() - 1) {
                    sb.append(",");
                }
            }

            WorkFlow flow = new WorkFlow();
            flow.setEmails(config.getEmails());
            /**
             * 依赖显示
             */
            flow.setRequestObj(config.getRequestObj());
            flow.setDependName(sb.toString());
            flow.setFlowName(config.getFlowName());
            flow.setConfig(config.getFlowConfig());
            flow.setActions(actions);
            flow.setRelations(relations);
            flow.setUpdateUUID(config.getUuid());
            flow.initialize();
            flow.build();
            flow.setRunParamJson(paramsJson);
            flow.setDescription(config.getDescription());
            flow.setSubWorkFlowJson(JSONObject.toJSONString(config.getSubWorkFlowJson()));
            UserInfo user = this.workDaoService.getUserInfoByName("mxq");
            if (this.workDaoService.addWorkFlow(flow, user)) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setDesc("创建成功");
                return resp;
            }
            resp.setDesc("当保存进数据库时出错,请检查配置!");
            resp.setStatus(HttpServletResponse.SC_RESET_CONTENT);
            return resp;
        } catch (Exception ex) {
            resp.setDesc(" 普通调度任务配置解析出错 " + ex.getMessage());
            resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            logger.error(ex.getMessage(), ex);
            return resp;
        }
    }

    private boolean checkWorkFlowConfig(String workFlowConfig, HttpRespone resp) {
        if (StringUtil.isEmpty(workFlowConfig)) {
            resp.setDesc("普通调度任务为空,请检查配置!");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return true;
        }
        return false;
    }

    private void checkEmail(List<String> emails) {
        String patternStr = "^[A-Za-zd]+([-_.][A-Za-zd]+)*@([A-Za-zd]+[-.])+[A-Za-zd]{2,5}$";
        Pattern pattern = Pattern.compile(patternStr);
        for (String email : emails) {
            if (!pattern.matcher(email).find()) {
                throw new RuntimeException(" 邮箱的格式不对 ");
            }
        }
    }

    private List<String> checkAction(WorkFlowConfig config)
            throws NoParamException, ParamDataTypeException, ParseActionException,
            CloneNotSupportedException {
        /*--------------------check action name ------------------*/

        List<String> names = new ArrayList<>();
        /*--------------------check action param config ------------------*/
        // Map<String, Map<String,List<DependencyWorkAction>>> maps = new HashMap<>();
        // Map<String, List<DependencyWorkAction>> stringListMap = new HashMap<>();
        for (WorkActionDescription descriptor : config.getDescriptors()) {

            this.checkDependWorkAction(descriptor, config.getFlowName());
            this.checkActionsParamters(descriptor);

            if (names.contains(descriptor.getActionName())) {
                throw new RuntimeException("action的名称重复了: " + descriptor.getActionName());
            } else {
                names.add(descriptor.getActionName());
            }
            WorkConfig configs = descriptor.getConfigs();
            if (configs.getParameters() == null || configs.getParameters().size() == 0) {
                throw new RuntimeException("parameters is none in descriptors");
            }
            doCheckConfig(configs);
        }
//        maps.put(config.getFlowName(),stringListMap);
        Map<String, WorkActionRelation> relations = config.getRelations();
        WorkActionRelation start = relations.get("START");
        if (start != null) {
            start.setActionName("START");
        } else {
            throw new RuntimeException(" startAction is null ");
        }
        this.checkActionNames(config, names);

        for (String node : names) {
            this.checkInfiniteLoops(node, relations);
            this.checkActioinRelation(node, relations);
        }
        this.checkEndAction("END", relations);
        this.checkSubWorkFlow(config.getSubWorkFlows(), relations);
        return names;
    }

    private void checkSubWorkFlow(List<SubWorkFlow> subWorkFlows, Map<String, WorkActionRelation> relations) {
        if (subWorkFlows != null && subWorkFlows.size() > 0) {
            for (SubWorkFlow subWorkFlow : subWorkFlows) {
                if (!relations.containsKey(subWorkFlow.getChildrenAction()) || !relations.containsKey(subWorkFlow.getFatherAction())) {
                    throw new RuntimeException("子工作流配置有问题,请检查");
                }
            }
        }
    }

    /**
     * 检查远程调用的死循环
     *
     * @param stringListMap
     * @param relations
     */
    private void checkRemoteInfiniteLoops(Map<String, List<DependencyWorkAction>> stringListMap
            , Map<String, WorkActionRelation> relations)
            throws ParseActionException, CloneNotSupportedException {
        for (String key : stringListMap.keySet()) {
            List<DependencyWorkAction> dwas = stringListMap.get(key);

            for (DependencyWorkAction dwa : dwas) {
                this.remoteLoops(key, dwa);
            }
        }
    }

    private void remoteLoops(String key, DependencyWorkAction dwa)
            throws ParseActionException, CloneNotSupportedException {
        WorkFlow workFlow = this.workDaoService.getWorkFlow(dwa.getWorkFlowName());
        DependencyWorkAction dependcyWorkAction = workFlow.getDependcyWorkActionByName(dwa.getWorkActionName());
        if (dependcyWorkAction != null) {
            this.remoteLoops(null, dwa);
        } else {
            String workActionName = dwa.getWorkActionName();
            WorkActionBase workAction = workFlow.getWorkActionByName(workActionName);
            List<WorkActionBase> fatherActions = workAction.listFatherActions();
            for (WorkActionBase fatherAction : fatherActions) {
                List<DependencyWorkAction> dependendWorkActions = fatherAction.getDependendWorkActions();
                if (dependendWorkActions != null && dependendWorkActions.size() > 0) {
                    for (DependencyWorkAction dependendWorkAction : dependendWorkActions) {
                        this.remoteLoops(null, dwa);
                    }
                } else {
                    this.fatherLoops(fatherAction, key);
                }
            }
        }
    }

    private void fatherLoops(WorkActionBase fatherAction, String key)
            throws ParseActionException, CloneNotSupportedException {
        for (WorkActionBase actionBase : fatherAction.listFatherActions()) {
            List<DependencyWorkAction> dependendWorkActions = actionBase.getDependendWorkActions();
            if (dependendWorkActions != null && dependendWorkActions.size() > 0) {
                for (DependencyWorkAction dependendWorkAction : dependendWorkActions) {
                    this.remoteLoops(null, dependendWorkAction);
                }
            } else {
                if (fatherAction.getActionName().equals(key)) {
                    throw new RuntimeException("remote action dowm in infiniteLoops");
                }
                if (fatherAction.listFatherActions() != null && fatherAction.listFatherActions().size() > 0) {
                    this.fatherLoops(fatherAction, key);
                }
            }
        }
    }

    /**
     * 检验endAction是否是最终的节点
     *
     * @param end
     * @param relations
     */
    private void checkEndAction(String end, Map<String, WorkActionRelation> relations) {
        WorkActionRelation workActionRelation = relations.get(end);
        if (workActionRelation.getFathers().size() <= 0) {
            throw new RuntimeException("endAction is not the end");
        }
    }

    /**
     * 调度系统action之间的依赖双边都要有配置
     * 一个关系涉及到启动
     * 一个关系涉及到能否启动
     *
     * @param node
     * @param relations
     */
    private void checkActioinRelation(String node, Map<String, WorkActionRelation> relations) {
        WorkActionRelation workActionRelation = relations.get(node);

        List<String> children = workActionRelation.getChildren();
        for (String child : children) {
            WorkActionRelation childrenAction = relations.get(child);
            List<String> fathers = childrenAction.getFathers();
            if (fathers.size() > 0 && !fathers.contains(node)) {
                throw new RuntimeException(" 请把action关系的配置设置准确:" + child);
            }
        }

        List<String> fathers = workActionRelation.getFathers();
        for (String father : fathers) {
            WorkActionRelation fatherAction = relations.get(father);
            List<String> childrens = fatherAction.getChildren();
            if (childrens.size() > 0 && !childrens.contains(node)) {
                throw new RuntimeException(" 请把action关系的配置设置准确" + father);
            }
        }

    }

    private void checkInfiniteLoops(String node, Map<String, WorkActionRelation> relations) {
        WorkActionRelation war = relations.get(node);
        this.loop(war, relations, node);
    }

    private void loop(WorkActionRelation war, Map<String, WorkActionRelation> relations, String node) {
        List<String> children = war.getChildren();
        for (String child : children) {
            if (node.equals(child)) {
                throw new RuntimeException("死循环" + child);
            }
            WorkActionRelation nodeChildren = relations.get(child);
            this.loop(nodeChildren, relations, node);
        }
    }

    private List<String> checkActionNames(WorkFlowConfig config, List<String> names) {
        Map<String, WorkActionRelation> relations = config.getRelations();
        names.add("START");
        names.add("END");
        List<String> lists = new ArrayList<>();

        relations.keySet().stream().map(key -> relations.get(key)).forEach(workActionRelation -> {
            lists.addAll(workActionRelation.getChildren());
            lists.addAll(workActionRelation.getFathers());
        });

        List<String> collect = lists.stream().distinct().collect(Collectors.toList());
        collect.forEach(acitonName -> {
            if (!names.contains(acitonName)) {
                throw new RuntimeException("there is not this action: " + acitonName);
            }
        });

        return collect;

    }

    private void checkDependWorkAction(WorkActionDescription descriptor, String flowName)
            throws ParseActionException, CloneNotSupportedException {
        List<DependencyWorkAction> dependWorkActions = descriptor.getDependWorkActions();
        if (dependWorkActions != null && dependWorkActions.size() > 0) {
            for (DependencyWorkAction dependWorkAction : dependWorkActions) {
                String workFlowName = dependWorkAction.getWorkFlowName();
                if (workFlowName.equals(flowName)) {
                    throw new RuntimeException(" it can not remote yourself");
                }
            }
        }
    }

    private void checkCacheRelation(CacheRelation cacheRelation) {
        if (cacheRelation.getDependAction() == null) {
            throw new RuntimeException("cacheRelation's dependAction has been required but there is none ");
        } else if (cacheRelation.getDependWorkFlow() == null) {
            throw new RuntimeException("cacheRelation's dependWorkFlow has been required but there is none ");
        }
    }

    private void checkActionsParamters(WorkActionDescription descriptor) {
        if (descriptor.getActionName() == null) {
            throw new RuntimeException("找不到action的名称");
        } else if (descriptor.getActionType() == null) {
            throw new RuntimeException("找不到action的名称");
        } else if (descriptor.getClazz() == null) {
            throw new RuntimeException("找不到action的名称");
        } else if (descriptor.getConfigs() == null) {
            throw new RuntimeException("找不到action的名称");
        }
    }

    private void doCheckConfig(WorkConfig config) throws NoParamException, ParamDataTypeException {
        Iterator<Entry<String, WorkActionParam>> iter = config.getParameters().entrySet().iterator();

        while (iter.hasNext()) {
            Entry<String, WorkActionParam> entry = iter.next();
            WorkActionParam param = entry.getValue();
            String key = entry.getKey();
            if (param.isRequired()) {
                if (!param.isSetContent()) {
                    throw new NoParamException("no param for key:" + key + " it's required");
                }
                checkDataType(param, param.getContent());
            }
        }
    }

    private void checkDataType(WorkActionParam param, Object value) throws ParamDataTypeException {
        if (param.getType() == ParamDataType.LIST) {
            if (!(value instanceof List)) {
                throw new ParamDataTypeException(
                        "content data type is " + value.getClass().getCanonicalName() + " but it's expected as list");
            }
        } else if (param.getType() == ParamDataType.MAP) {

            if (!(value instanceof Map)) {
                throw new ParamDataTypeException(
                        "content data type is " + value.getClass().getCanonicalName() + " but it's expected as map");
            }
        } else if (param.getType() == ParamDataType.STRING) {
            if (!(value instanceof String)) {
                throw new ParamDataTypeException(
                        "content data type is " + value.getClass().getCanonicalName() + " but it's expected as String");
            }
        } else {
            throw new ParamDataTypeException("unsupport data type");
        }
    }

    public WorkFlow getWorkFlow(WorkActionTransfer newDependWorkAction, WorkFlowConfig config) throws ParseActionException, CloneNotSupportedException {
        WorkFlow wf = this.workDaoService.getWorkFlowByRelation(newDependWorkAction.getWorkFlowName(), config.getFlowName());
        if (wf == null) {
            wf = this.workDaoService.getWorkFlow(newDependWorkAction.getWorkFlowName());
            config.setOpen(true);
            if (wf == null) {
                throw new ParseActionException("can not find work flow config for name:" + newDependWorkAction.getWorkFlowName());
            }
        }
        return wf;
    }

    public void addWorkFlowRelation(WorkFlowConfig config, WorkFlow workFlow, Map<String, WorkActionRelation> r, String workFlowName, String fatherName) throws ParseActionException {
        if (!config.getOpen()) {
            this.workDaoService.updateRelation(r, workFlowName, fatherName);
        } else {
            if (config.getUuid() == null) {
                config.setUuid(UUID.randomUUID().toString());
            }
            this.workDaoService.insertRelation(workFlow, fatherName, config);
        }
        config.setOpen(false);
    }
}