package com.bigdata.xwork.core.service;

import com.bigdata.xwork.action.core.WorkActionBase;
import com.bigdata.xwork.action.core.WorkActionType;
import com.bigdata.xwork.action.function.FunctionDescriptor;
import com.bigdata.xwork.core.common.CacheRelation;
import com.bigdata.xwork.core.common.DependencyWorkAction;
import com.bigdata.xwork.core.common.FlowActionUtils;
import com.bigdata.xwork.core.common.WorkConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Component
public class WorkActionService {

    @Autowired
    private FlowActionUtils workActionFlowUtils;

    private final String[] DEFAULTCLASS = {"DemoAction",
            "ConditionWorkAction",
            "EndWorkAction",
            "StartWorkAction",
            "ShellAction",
            "KettleAction",
            "HiveAction"};

    private List<WorkActionDescription> actionList = new ArrayList<>();

    private List<FunctionDescriptor> functionList = new ArrayList<>();

    private final static Logger logger = LoggerFactory.getLogger(WorkFlowService.class);


    @PostConstruct
    public void loadActions() {
        for (String clazz : this.DEFAULTCLASS) {
            try {
                actionList.add(workActionFlowUtils.getWorkActionDescription(clazz));
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {

                e.printStackTrace();
                logger.error(e.toString());
            }
        }
    }

    public List<WorkActionDescription> getWorkActionDescriptions() {
        return this.actionList;
    }

    public WorkActionDescription getWorkActionDescriptionByClazz(Class<? extends WorkActionBase> clazz) {
        for (WorkActionDescription descriptor : this.actionList) {
            if (descriptor.getClazz().equals(clazz.getCanonicalName())) {
                return descriptor;
            }
        }

        return null;
    }

    public WorkActionDescription getWorkActionDescription(String name) {
        for (WorkActionDescription descriptor : this.actionList) {
            if (descriptor.getShowName().equals(name)) {
                return descriptor;
            }
        }
        return null;
    }

    public List<FunctionDescriptor> getFunctions() {
        return this.functionList;
    }

    public static class WorkActionDescription
    {
        private CacheRelation cacheRelation;

        private List<DependencyWorkAction> dependWorkActions;

        private String desc;

        private String showName;

        private WorkActionType actionType;

        private WorkConfig configs;

        private String clazz;

        private String actionName;

        public List<DependencyWorkAction> getDependWorkActions() {
            return dependWorkActions;
        }

        public void setDependWorkActions(List<DependencyWorkAction> dependWorkActions) {
            this.dependWorkActions = dependWorkActions;
        }

        public CacheRelation getCacheRelation() {
            return cacheRelation;
        }

        public void setCacheRelation(CacheRelation cacheRelation) {
            this.cacheRelation = cacheRelation;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getShowName() {
            return showName;
        }

        public void setShowName(String showName) {
            this.showName = showName;
        }

        public WorkActionType getActionType() {
            return actionType;
        }

        public void setActionType(WorkActionType actionType) {
            this.actionType = actionType;
        }

        public WorkConfig getConfigs() {
            return configs;
        }

        public void setConfigs(WorkConfig configs) {
            this.configs = configs;
        }

        public String getClazz() {
            return clazz;
        }

        public void setClazz(String clazz) {
            this.clazz = clazz;
        }

        public String getActionName() {
            return actionName;
        }

        public void setActionName(String actionName) {
            this.actionName = actionName;
        }

    }


}
