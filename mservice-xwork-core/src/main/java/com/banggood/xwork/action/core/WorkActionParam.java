package com.banggood.xwork.action.core;

import java.io.Serializable;
import java.sql.Timestamp;

import com.banggood.xwork.core.exception.NoParamException;


/**
 * 所有配置都是字符串
 */
public class WorkActionParam  implements Serializable {

    /**
     * 评论
     */
    private String comment;

    /**
     * 操作者
     */
    private String updater;

    /**
     * 最后更新时间
     */
    private Timestamp lastUpdateTime;


    /**
     * 是否必填
     */
    private boolean required;


    /**
     * 参数类型
     */
    private ParamDataType type;


    /**
     * 是否设置了内容
     */
    private boolean setContent;


    /**
     * 只有三种类型，最终怎么转换，适配器开发者决定
     */
    public enum ParamDataType {

        LIST, MAP, STRING

    }


    private Object content;


    public WorkActionParam() {

    }

    public WorkActionParam(boolean required, ParamDataType type) {
        this.required = required;
        this.type = type;
    }


    /**
     * 实例化时使用
     */
    public void updateContent(Object content) {
        this.content = content;
    }


    /**
     * 更新配置时使用
     *
     * @throws NoParamException
     */
    public void updateContent(Object content, String updater, String comment) throws NoParamException {
        if (comment != null && comment.trim().length() > 0) {
            this.comment = comment;
        }

        if (updater == null) {
            throw new NoParamException("updater must be config");
        }

        this.updater = updater;
        this.content = content;

    }


    public String getComment() {
        return comment;
    }


    public void setComment(String comment) {
        this.comment = comment;
    }


    public String getUpdater() {
        return updater;
    }


    public void setUpdater(String updater) {
        this.updater = updater;
    }


    public Timestamp getLastUpdateTime() {
        return lastUpdateTime;
    }


    public void setLastUpdateTime(Timestamp lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }


    public Object getContent() {
        return content;
    }


    public void setContent(Object content) {
        this.content = content;
        this.setContent = true;
    }


    public boolean isRequired() {
        return required;
    }


    public void setRequired(boolean required) {
        this.required = required;
    }


    public ParamDataType getType() {
        return type;
    }


    public void setType(ParamDataType type) {
        this.type = type;
    }


    public boolean isSetContent() {
        return setContent;
    }


}
