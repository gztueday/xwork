package com.banggood.xwork.action.impl;

import com.banggood.xwork.action.core.*;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hive.jdbc.HiveStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 执行hive的Action
 *
 * @author zouyi
 */
@WorkActionTag(desc = "HiveAction", name = "HiveAction")
public class HiveAction extends WorkActionBase {

    private final String HIVE_ACTION_SQL_PATH = "HSql.Path";
    private final String HiveSQLException = "Query was cancelled";
    private Connection conn = null;
    private InputStream in = null;
    private Statement st = null;
    private ResultSet rs = null;
    private FileSystem fs = null;
    private ByteArrayOutputStream bos = null;
    private boolean isCloed = false;
    private String sql = null;
    public static String hiveUrl;
    public static Configuration conf = null;
    public static Logger logger = LoggerFactory.getLogger(HiveAction.class);
    public Map<String, String> argumentMaps = null;

    public HiveAction() {
        super(WorkActionType.NORMORL);
        this.registerParam(HIVE_ACTION_SQL_PATH, new WorkActionParam(true, WorkActionParam.ParamDataType.STRING));
    }

    @Override
    public void initialize() {

    }

    @Override
    public void putActionsArguments(Map<String, String> argumentMaps) {
        this.argumentMaps = argumentMaps;
    }

    @Override
    protected void resume() {

    }

    @Override
    public void execute() throws Exception {

        Map<String, WorkActionParam> parameters = this.getConfig().getParameters();
        String sqlPath = parameters.get(HIVE_ACTION_SQL_PATH).getContent().toString();
        if (sqlPath == null) {
            String msg = "请在配置里面设置hdfs的执行路径";
            this.setWorkFlowLogger(msg);
            throw new RuntimeException(msg);
        }
        Path srcPath = new Path(sqlPath);
        String msg = "hive的连接地址为: " + HiveAction.hiveUrl;
        logger.info(msg);
        this.setWorkFlowLogger(msg);
        try {
            this.fs = FileSystem.get(HiveAction.conf);

            this.bos = new ByteArrayOutputStream();

            this.in = fs.open(srcPath);
        } catch (ConnectException e) {
            this.setWorkFlowLogger("链接hdfs报错: " + e.getLocalizedMessage());
            logger.error(e.getMessage(), e);
            throw new RuntimeException("链接hdfs报错: " + e.getLocalizedMessage());
        }
        byte[] buffer = new byte[4096];
        int length = 0;
        while ((length = in.read(buffer)) != -1) {
            bos.write(buffer, 0, length);
        }

        this.sql = new String(bos.toByteArray(), "UTF-8");


        if (this.argumentMaps != null && this.argumentMaps.size() > 0) {
            for (String key : this.argumentMaps.keySet()) {
                String k = "${" + key + "}";
                String v = argumentMaps.get(key);
                if (StringUtils.containsIgnoreCase(this.sql, key)) {
                    String paramSql = StringUtils.replace(this.sql, k, v);
                    this.sql = paramSql;
                }
            }
        }

        this.conn = DriverManager.getConnection(HiveAction.hiveUrl, "", "");

        this.st = conn.createStatement();
        logger.info("Hive's sql: {}", sql);
        this.setWorkFlowLogger(msg);
        WorkExecutorPool.submitWork(() -> {
            try {
                while (((HiveStatement) this.st).hasMoreLogs() && !this.isCloed) {
                    //这个方法会阻塞
                    ((HiveStatement) this.st).getQueryLog()
                            .forEach((info) -> logger.info("HIVE " + info));
                    Thread.sleep(1000);
                }
            } catch (InterruptedException | SQLException e) {
                logger.error(e.getMessage(), e);
            }
        });

        try {
            //这个方法会阻塞的,运行完才会执行下一步
            if (this.st.execute(sql)) {
                if (!this.isCloed) {
                    this.rs = st.getResultSet();
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    StringBuffer sb = new StringBuffer(50);
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        sb.append(columnName).append(",");
                    }
                    msg = "hive sql: columns:" + sb.toString();
                    logger.info(msg);
                    this.setWorkFlowLogger(msg);
                    while (this.rs.next()) {
                        sb.delete(0, sb.length());
                        for (int i = 1; i <= columnCount; i++) {
                            sb.append(" ").append(rs.getString(i));
                        }
                        msg = "hive sql: ResultSet: " + sb.toString();
                        logger.info(msg);
                        this.setWorkFlowLogger(msg);
                    }
                }
            } else {
                if (!this.isCloed) {
                    msg = "该SQL语句影响的记录有" + this.st.getUpdateCount() + "条";
                    logger.info(msg);
                    this.setWorkFlowLogger(msg);
                }
            }
        } catch (SQLException e) {
            if (HiveSQLException.equals(e.getMessage())) {
                this.close();
                msg = "this hiveSql has been closed , this Hsql is " + sql;
                logger.info(msg);
                this.setWorkFlowLogger(msg);
                this.setWorkFlowLogger(e.getLocalizedMessage());
            } else {
                this.setWorkFlowLogger(e.getLocalizedMessage());
                throw new SQLException(e);
            }
        }
        if (!this.isCloed) {
            this.close();
            msg = "该hive sql已经关闭了 " + System.getProperty("line.separator") + sql;
            logger.info(msg);
            this.setWorkFlowLogger(msg);
        }
        msg = this.getActionName() + " hive 运行成功,运行地址为: " + InetAddress.getLocalHost().getHostAddress();
        logger.info(msg);
        this.setWorkFlowLogger(msg);
    }

    @Override
    protected void kill() {
        try {
            if (!this.conn.isClosed()) {
                this.isCloed = true;
                this.st.cancel();
                String msg = " 该sql已经被取消了 " + sql;
                logger.info(msg);
                this.setWorkFlowLogger(msg);
                this.close();
                this.getWorkFlow().getOutputStream().close();
            }
        } catch (SQLException | IOException e) {
            logger.error(e.getMessage(), e);
            this.setWorkFlowLogger(e.getLocalizedMessage());
        }
    }

    @Override
    public Map<String, Object> getOutPut() {
        Map<String, Object> outPut = new HashMap<>();
        outPut.put("hive action的运行时间:", new java.util.Date().toString());
        outPut.put("Hsql:", sql);
        return outPut;
    }

    @Override
    protected void close() {
        //从小关到大
        if (this.rs != null) {
            try {
                this.rs.close();
                rs = null;
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
            } finally {
                if (this.st != null) {
                    try {
                        this.st.close();
                        st = null;
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        if (this.conn != null) {//conn
                            try {
                                this.conn.close();
                                conn = null;
                            } catch (SQLException e) {
                                logger.error(e.getMessage(), e);
                            } finally {
                                if (this.fs != null) {
                                    try {
                                        this.fs.close();
                                        fs = null;
                                    } catch (IOException e) {
                                        logger.error(e.getMessage(), e);
                                    } finally {
                                        IOUtils.closeStream(this.in);
                                        IOUtils.closeStream(this.bos);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void runDone() {

    }

    @Override
    protected void pause() {

    }

    @Override
    protected void fail() {
        this.kill();
    }

    @Override
    public void beforeStart() {


    }

    @Override
    protected void not2do() {
        this.kill();
    }

    @Override
    public String getLog() {
        return null;
    }

}
