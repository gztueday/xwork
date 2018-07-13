package com.banggood.xwork.action.core;

import com.banggood.xwork.core.common.DependencyWorkAction;
import com.banggood.xwork.core.common.RemoteWorkAction;
import com.banggood.xwork.core.exception.RelationshipException;

import java.util.ArrayList;
import java.util.List;

public class WorkActionRelation {

    private String actionName;

    private List<String> fathers = new ArrayList<>();

    private List<String> children = new ArrayList<>();
    /**
     * 依赖的action
     */
    private List<DependencyWorkAction> dependWorkActions;
    /**
     * 调用的action
     */
    private List<RemoteWorkAction> remoteDepends;

    public List<RemoteWorkAction> getRemoteDepends() {
        if (this.remoteDepends == null) {
            remoteDepends = new ArrayList<>();
        }
        return remoteDepends;
    }

    public void setRemoteDepends(List<RemoteWorkAction> remoteDepends) {
        this.remoteDepends = remoteDepends;
    }

    public RemoteWorkAction findRemoteWorkAction(String remoteWorkActionName) {
        if (this.remoteDepends != null && this.remoteDepends.size() > 0) {
            for (RemoteWorkAction remoteDepend : this.remoteDepends) {
                if (remoteDepend.getWorkActionName().equals(remoteWorkActionName)) {
                    return remoteDepend;
                }
            }
        }
        return null;
    }

    public List<DependencyWorkAction> getDependWorkActions() {
        if (this.dependWorkActions == null) {
            this.dependWorkActions = new ArrayList<>();
        }
        return dependWorkActions;
    }

    public void setDependWorkActions(List<DependencyWorkAction> dependWorkActions) {
        this.dependWorkActions = dependWorkActions;
    }

    public void addFatherName(String father) {
        this.fathers.add(father);
    }

    public void addChildName(String child) {
        this.children.add(child);
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public List<String> getFathers() {
        return fathers;
    }

    public void setFathers(List<String> fathers) {
        this.fathers = fathers;
    }

    public List<String> getChildren() {
        return children;
    }

    public void setChildren(List<String> children) {
        this.children = children;
    }

    public void updateRemoteRelation(String flowName, String workActionName) {
        if (this.remoteDepends == null) {
            this.remoteDepends = new ArrayList<>();
        }
        RemoteWorkAction remoteWorkAction = new RemoteWorkAction();
        remoteWorkAction.setWorkFlowName(flowName);
        remoteWorkAction.setWorkActionName(workActionName);
        this.remoteDepends.add(remoteWorkAction);
    }

    public DependencyWorkAction findDependWorkAction(String workActionName) {
        if (this.dependWorkActions != null && this.dependWorkActions.size() > 0) {
            for (DependencyWorkAction dependWorkAction : this.dependWorkActions) {
                if (dependWorkAction.getWorkActionName().equals(workActionName)) {
                    return dependWorkAction;
                }
            }
        }
        return null;
    }

    public void updateDependRelation(String flowName, String workActionName) {
        if (this.dependWorkActions == null) {
            this.dependWorkActions = new ArrayList<>();
        }
        DependencyWorkAction dependencyWorkAction = new DependencyWorkAction();
        dependencyWorkAction.setWorkFlowName(flowName);
        dependencyWorkAction.setWorkActionName(workActionName);
        this.dependWorkActions.add(dependencyWorkAction);
    }

    public void removeDependRelation(String flowName, String oldKey) throws RelationshipException {
        if (this.dependWorkActions == null) {
            throw new RelationshipException("can not find this action: " + oldKey + " in workFlow: " + flowName);
        }
        for (int i = 0; i < this.dependWorkActions.size(); i++) {
            DependencyWorkAction dependencyWorkAction = this.dependWorkActions.get(i);
            if (dependencyWorkAction.getWorkFlowName().equals(flowName) &&
                    dependencyWorkAction.getWorkActionName().equals(oldKey)) {
                this.dependWorkActions.remove(i);
            }
        }
    }

    public void removeRemoteRelation(String flowName, String oldKey) throws RelationshipException {
        if (this.remoteDepends == null) {
            throw new RelationshipException("can not find this action: " + oldKey + " in workFlow: " + flowName);
        }
        for (int i = 0; i < this.remoteDepends.size(); i++) {
            RemoteWorkAction remoteWorkAction = this.remoteDepends.get(i);
            if (remoteWorkAction.getWorkFlowName().equals(flowName) &&
                    remoteWorkAction.getWorkActionName().equals(oldKey)) {
                this.remoteDepends.remove(i);
            }
        }
    }

    public DependencyWorkAction findDependWorkActionByFlowAndActionName(String flowName, String dependWorkActionName) {

        for (int i = 0; i < this.dependWorkActions.size(); i++) {
            if (flowName.equals(this.dependWorkActions.get(i).getWorkFlowName()) &&
                    dependWorkActionName.equals(this.dependWorkActions.get(i).getWorkActionName())) {
                return this.dependWorkActions.get(i);
            }
        }
        return null;
    }
}
