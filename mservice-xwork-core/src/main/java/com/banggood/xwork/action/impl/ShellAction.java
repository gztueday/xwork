package com.banggood.xwork.action.impl;

import com.alibaba.fastjson.JSONObject;
import com.banggood.xwork.action.core.WorkActionBase;
import com.banggood.xwork.action.core.WorkActionParam;
import com.banggood.xwork.action.core.WorkActionTag;
import com.banggood.xwork.action.core.WorkActionType;
import com.banggood.xwork.core.exception.NoParamException;
import com.banggood.xwork.core.exception.ParamDataTypeException;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.util.*;

/**
 * 执行Shell的Action
 * 目前只接受传递命令字符串(String)
 * 两行命令间,以分号";"分隔开
 */
@SuppressWarnings("ALL")
@WorkActionTag(desc = "ShellAction", name = "ShellAction")
public class ShellAction extends WorkActionBase {

    private static Logger logger = LoggerFactory.getLogger(ShellAction.class);

    private static final String[] SHELL_EXEC = {"/bin/bash", "-c"};

    //    public static final String CONF_XWORK_SHELL_COMMAND = "xwork.shell.command";
    public static final String CONF_XWORK_SHELL_PATH = "path";
//    public static final String CONF_XWORK_SHELL_ENV = "xwork.shell.env";

    private List<String> execCommandList;

    private ProcessBuilder builder;
    private Process process;
    private Map<String, String> confEnvMapCp = null;
    private String commandCp = null;
    public Map<String, String> argumentMaps = null;

    public ShellAction() {
        super(WorkActionType.NORMORL);
        this.registerParam(CONF_XWORK_SHELL_PATH, new WorkActionParam(true, WorkActionParam.ParamDataType.MAP));
    }

    @Override
    public void initialize() {

    }

    /**
     * 替换参数的方法
     *
     * @param argumentMaps
     */
    @Override
    public void putActionsArguments(Map<String, String> argumentMaps) {
//        Map<String, WorkActionParam> parameters = this.getConfig().getParameters();
//        String command = parameters.get(CONF_XWORK_SHELL_COMMAND).getContent().toString();
//        for (String key : argumentMaps.keySet()) {
//            String k = "${" + key + "}";
//            String v = argumentMaps.get(key);
//            if (StringUtils.containsIgnoreCase(command, k)) {
//                parameters.get(CONF_XWORK_SHELL_COMMAND).setContent(StringUtils.replaceOnce(command, k, v));
//            }
//        }
        this.argumentMaps = argumentMaps;
    }


    @Override
    public void resume() {

    }

    @Override
    public void execute() throws Exception {
        Map<String, WorkActionParam> parameters = this.getConfig().getParameters();


        String json = parameters.get(CONF_XWORK_SHELL_PATH).getContent().toString();
        String path = JSONObject.parseObject(json).getString("env");
        FileSystem f = FileSystem.get(HiveAction.conf);
        FSDataInputStream open = f.open(new Path(path));
        byte[] buffer = new byte[4096];
        int length = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((length = open.read(buffer)) != -1) {
            bos.write(buffer, 0, length);
        }
        String command = new String(bos.toByteArray(), "UTF-8");


        Map<String, String> confEnvMap = null;

        if (this.argumentMaps != null && this.argumentMaps.size() > 0) {
            for (String key : this.argumentMaps.keySet()) {
                String k = "${" + key + "}";
                String v = argumentMaps.get(key);
                if (StringUtils.containsIgnoreCase(command, key)) {
                    String paramSql = StringUtils.replace(command, k, v);
                    command = paramSql;
                }
            }
        }
//        WorkActionParam workActionParam = parameters.get(CONF_XWORK_SHELL_ENV);
//        if (workActionParam != null) {
//            Object content = workActionParam.getContent();
//            confEnvMap = (Map) content;
//            this.confEnvMapCp = confEnvMap;
//        }

        this.commandCp = command;

        if (StringUtils.isNotEmpty(command)) {
            execCommandList = getExecCommandList(command);
        }
        builder = new ProcessBuilder(execCommandList);

        //加载环境变量
        //从配置中读取环境变量，并加载该环境变量到Java执行shell命令的子进程中
        Map<String, String> builderEnvMap = builder.environment();
        if (confEnvMap != null) {
            confEnvMap.forEach((key, value) -> builderEnvMap.put(key, value));
        }

        process = builder.start();
        String msg = this.getActionName() + " 运行的是shell脚本: 执行的地址: " + InetAddress.getLocalHost().getHostAddress() + " 该脚本的id为: " + this.getInstanceid();
        logger.info(msg);
        this.setWorkFlowLogger(msg);
        logger.info("等shell命令执行完后,才输出结果,在此阻塞..............");
        int exitValue = process.waitFor();

        InputStream inputStream = process.getInputStream();
        BufferedReader input = new BufferedReader(new InputStreamReader(inputStream));
        InputStream errorStream = process.getErrorStream();
        BufferedReader error = new BufferedReader(new InputStreamReader(errorStream));
        //等待输出进程结束
        int read = 0;
        try {
            read = errorStream.read();
        } catch (IOException e) {

        }
        outPut(input, true, false, 0);
        if (read > -1) {
            outPut(error, true, false, 1);
        }
        this.setWorkFlowLogger(this.getActionName() + " 运行成功");
    }

    @Override
    public void runDone() throws InterruptedException {

    }

    @Override
    public void kill() {
        if (process != null) {
            process.destroy();
            String msg = "shellAction, has been destroy :" + this.getActionName();
            logger.info(msg);
            this.setWorkFlowLogger(msg);
            try {
                this.getWorkFlow().getOutputStream().close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void pause() {
    }

    @Override
    public void fail() {
        this.kill();
    }

    @Override
    public void beforeStart() {
        //---------------------Q:what's the difference between beforeStart() and initalize()?
    }

    @Override
    protected void not2do() {
        if (process != null) {
            process.destroy();
            String msg = "shellAction , has been destroy :" + this.getActionName();
            logger.info(msg);
            this.setWorkFlowLogger(msg);
        }
    }

    @Override
    protected void close() {

    }

    @Override
    public String getLog() {
        return null;
    }

    @Override
    public Map<String, Object> getOutPut() {
        Map<String, Object> outPut = new HashMap<>();
        outPut.put("EnvMap", confEnvMapCp);
        outPut.put("command", commandCp);
        outPut.put("ShellActionRunTime:", new Date().toString());
        return outPut;
    }

    /**
     * 处理shell输出
     *
     * @param process
     */
    private Thread[] handlerShellOutput(Process process) {
        BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        Thread threadStdout = new Thread(() -> {
            try {
                outPut(input, true, false, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        threadStdout.setDaemon(true);
        threadStdout.setName("normal");
        threadStdout.start();

        Thread threadErrout = new Thread(() -> {
            try {
                outPut(error, true, false, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        threadErrout.setDaemon(true);
        threadErrout.setName("error");
        threadErrout.start();


        return new Thread[]{threadStdout, threadErrout};
    }

    private void outPut(BufferedReader input, boolean isStdOutput, boolean isNeedWriteToFile, int open)
            throws IOException {
        //获取文件配置
        File file = null;

        BufferedWriter writer = null;
        if (file != null) {
            writer = new BufferedWriter(new FileWriter(file));
        }
        if (isStdOutput) {
            String line;
            StringBuilder sb = new StringBuilder(50);
            while ((line = input.readLine()) != null) {
                String msg = "输出的内容为: " + line;
                logger.info(msg);
                sb.append(line).append(",");
                this.setWorkFlowLogger(msg);
                //写文件
                if (isNeedWriteToFile && writer != null) {
                    writer.write(line);
                    writer.newLine();
                }
                if (1 == open) {
                    throw new ShellException(sb.toString());
                }
            }
        }
    }

    private class ShellException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public ShellException(String message) {
            super(message);
        }

    }

    /**
     * 处理shell执行命令
     * 加上执行前缀
     *
     * @param shellCommand
     * @return
     */
    private List<String> getExecCommandList(String shellCommand) {
        List<String> execCommandList = new ArrayList<>();

        List<String> execList = Arrays.asList(SHELL_EXEC);
        for (String exec : execList) {
            execCommandList.add(exec);
        }

        execCommandList.add(shellCommand);

        return execCommandList;
    }

}
