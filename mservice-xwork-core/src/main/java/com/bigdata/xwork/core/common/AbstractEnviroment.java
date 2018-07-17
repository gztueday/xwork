package com.bigdata.xwork.core.common;

import com.bigdata.xwork.action.core.WorkActionBase;
import com.bigdata.xwork.action.core.WorkActionParam;
import com.bigdata.xwork.action.core.WorkActionParam.ParamDataType;
import com.bigdata.xwork.action.function.FunctionDescriptor;
import com.bigdata.xwork.core.common.FunctionUtils.MatchType;
import com.bigdata.xwork.core.exception.MatchException;
import com.bigdata.xwork.core.exception.NoParamException;
import com.bigdata.xwork.core.exception.ParamDataTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 存储配置信息
 */

@SuppressWarnings("ALL")
public abstract class AbstractEnviroment implements Serializable {

    /**
     * 是否共享
     */
    private boolean share;

    /**
     * 运行参数
     */
    private WorkConfig runParam = new WorkConfig();

    /**
     * 配置参数
     */
    private WorkConfig config = new WorkConfig();

    /**
     * 配置参数
     */
    public abstract void initialize();

    private Map<String, Map<String, String>> arguments = new HashMap<>();

    public Map<String, Map<String, String>> getArguments() {
        return arguments;
    }

    public void setArguments(Map<String, Map<String, String>> arguments) {
        this.arguments = arguments;
    }

    private final static Logger logger = LoggerFactory.getLogger(WorkActionBase.class);

    /**
     * 配置注册参数，规定哪些参数是必须的，哪些mutil value
     */
    public void registerParam(String key, WorkActionParam paramConfig) {
        this.config.putWorkActionParam(key, paramConfig);
        //this.parameters.put(key, param);
    }

    /**
     * 前端配置参数的时候调用
     */
    public void putToConfig(String key, Object value) throws NoParamException, ParamDataTypeException {
        if (!this.config.getParameters().containsKey(key)) {
            throw new NoParamException("no such config for key:" + key + " in action " + this.getClass().getName());
        } else {
            WorkActionParam param = this.config.getParameters().get(key);
            checkDataType(param, value);
            param.setContent(value);
        }
    }

    public void putLong(String key, long value) {
        this.runParam.putLong(key, value);
    }

    public void putObject(String key, Object value) {
        this.runParam.put(key, value);
    }

    /**
     * 将配置的参数转换成实际运行的参数
     *
     * @throws NoParamException
     */
    public void decodeParam(WorkConfig runParam) throws NoParamException {
        Map<String, WorkActionParam> param = runParam.getParameters();
        Iterator<Entry<String, WorkActionParam>> iter = this.config.getParameters().entrySet().iterator();

        while (iter.hasNext()) {
            Entry<String, WorkActionParam> entry = iter.next();

            WorkActionParam config = entry.getValue();
            String key = entry.getKey();

            if (config.getType() == ParamDataType.STRING) {
                transfer(config.getContent().toString(), param, key);
            } else if (config.getType() == ParamDataType.LIST) {
                List<String> list = this.config.getList(key);
                for (String content : list) {
                    transfer(content, param, key);
                }
            } else if (config.getType() == ParamDataType.MAP) {
                Map<String, Object> map = this.config.getMap(key);
                Iterator<Entry<String, Object>> innerIter = map.entrySet().iterator();
                while (innerIter.hasNext()) {
                    Entry<String, Object> innerEntry = innerIter.next();
                    String innerKey = innerEntry.getKey();
                    String innerContent = innerEntry.getValue().toString();
                    transfer(innerContent, param, innerKey);
                }
            }
        }
    }

    private void transfer(String config, Map<String, WorkActionParam> param, String key) throws NoParamException {
        try {
            if (FunctionUtils.checkParamConfig(config) == MatchType.MATCHFUNCTION) {
                FunctionDescriptor fd = FunctionUtils.matchFunction(config);
                String value = FunctionUtils.invokeTimeFunction(fd);
                this.putObject(key, value);
            } else {
                String varible = FunctionUtils.matchVariable(config);
                if (param.containsKey(varible)) {
                    this.runParam.putObject(varible, param.get(varible).getContent());
                } else {
                    logger.warn("cann not find param value for key: {} may be it comes from fathers' output", varible);
                }
            }
        } catch (MatchException e) {

            logger.info("try to get value from config value for key: {}", key);
            if (this.config.getString(key) != null) {
                this.runParam.putObject(key, this.config.getString(key));

            } else {
                logger.warn("can not get value finally");
            }
            // throw new RuntimeException(e);
        }
    }

    public void putString(String key, String value) {
        this.runParam.putString(key, value);
    }

    public void putInteger(String key, Integer value) {
        this.runParam.putInteger(key, value);
    }

    public void putFloat(String key, Float value) {
        this.runParam.putFloat(key, value);
    }

    /**
     * 检查配置信息
     */
    public void doCheckConfig() throws NoParamException, ParamDataTypeException {
        Iterator<Entry<String, WorkActionParam>> iter = this.config.getParameters().entrySet().iterator();

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
                throw new ParamDataTypeException("content data type is " + value.getClass().getCanonicalName() + " but it's expected as list");
            }
        } else if (param.getType() == ParamDataType.MAP) {

            if (!(value instanceof Map)) {
                throw new ParamDataTypeException("content data type is " + value.getClass().getCanonicalName() + " but it's expected as map");
            }
        } else if (param.getType() == ParamDataType.STRING) {
            if (!(value instanceof String)) {
                throw new ParamDataTypeException("content data type is " + value.getClass().getCanonicalName() + " but it's expected as String");
            }
        } else {
            throw new ParamDataTypeException("unsupport data type");
        }
    }

    public void setRunParam(WorkConfig runParam) {
        this.runParam = runParam;
    }

    public void setConfig(WorkConfig config) {
        this.config = config;
    }

    public WorkConfig getRunParam() {
        return runParam;
    }

    public WorkConfig getConfig() {
        return config;
    }

    public boolean isShare() {
        return share;
    }

    public void setShare(boolean share) {
        this.share = share;
    }

}
