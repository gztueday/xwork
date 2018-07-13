package com.banggood.xwork.action.impl;

import com.banggood.xwork.action.core.WorkActionBase;
import com.banggood.xwork.action.core.WorkActionParam;
import com.banggood.xwork.action.core.WorkActionTag;
import com.banggood.xwork.action.core.WorkActionType;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.filerep.KettleFileRepositoryMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

/**
 * 执行Kettle的Action
 */
@WorkActionTag(desc = "KettleAction", name = "KettleAction")
public class KettleAction extends WorkActionBase {

    private final static Logger logger = LoggerFactory.getLogger(KettleAction.class);

    private final String KETTLE_PATH = "path";

    public KettleAction() {
        super(WorkActionType.NORMORL);
        this.registerParam(KETTLE_PATH, new WorkActionParam(false, WorkActionParam.ParamDataType.STRING));
    }

    @Override
    public void initialize() {

    }

    @Override
    public void putActionsArguments(Map<String, String> argumentMaps) {

    }

    @Override
    protected void resume() {

    }

    private Job job = null;

    @Override
    public void execute() throws Exception {
        Map<String, WorkActionParam> parameters = this.getConfig().getParameters();
        String path = parameters.get(KETTLE_PATH).getContent().toString();//kettle文件路劲
        FileSystem f = FileSystem.get(HiveAction.conf);
        f.copyToLocalFile(new Path(path), new Path("/opt/modules/xwork/kettle/dw_etl/job"));
        String job = StringUtils.substringBetween(path, "/", ".kjb");

        // 初始化
        KettleEnvironment.init();
        RepositoriesMeta repositoriesMeta = new RepositoriesMeta();

        KettleFileRepositoryMeta meta = new KettleFileRepositoryMeta();

        meta.setBaseDirectory("/Users/zouyi/Downloads/kettle/dw_etl");
        meta.setName("dw_etl");
        meta.setDescription("dw_etl");
        repositoriesMeta.addRepository(meta);
        RepositoryMeta repositoryMeta = repositoriesMeta.findRepository("dw_etl");


        PluginRegistry registry = PluginRegistry.getInstance();

        Repository repository = registry.loadClass(
                RepositoryPluginType.class,
                repositoryMeta,
                Repository.class);

        repository.init(repositoryMeta);

        repository.connect("admin", "admin");

        String msg = " KettleAction: this job repository has been ignited !";
        logger.info(msg);
        this.setWorkFlowLogger(msg);

        RepositoryDirectoryInterface directory = repository.loadRepositoryDirectoryTree();
        directory = directory.findDirectory("job");
        msg = "KettleAction: this job directory is job";
        logger.info(msg);
        this.setWorkFlowLogger(msg);
        //job文件名
        JobMeta jobMeta = repository.loadJob(job, directory, null, null);

        this.job = new Job(repository, jobMeta);

        msg = "KettleAction: " + this.job.getJobname() + " start !";
        logger.info(msg);
        this.setWorkFlowLogger(msg);

        this.job.start();
        this.job.waitUntilFinished();
        Result result = this.job.getResult();
        System.out.println(result);
        if (this.job.getErrors() > 0) {
            msg = "KettleAction: Job Error: " + this.job.getErrors();
            logger.error(msg);
            this.setWorkFlowLogger(msg);
            throw new Exception("There are errors during job exception!");
        } else {
            msg = "KettleAction: Kitchen has been succeeded!";
            logger.info(msg);
            this.setWorkFlowLogger(msg);
        }
        msg = this.getActionName() + " is KettleAction, execute success , address:" + InetAddress.getLocalHost().getHostAddress();
        logger.info(msg);
        this.setWorkFlowLogger(msg);
        //this.job.setFinished(true);
        //this.job.eraseParameters();
        //jobMeta.eraseParameters();
        //org.apache.log4j.Logger.getLogger("org.pentaho.di").addAppender(new FileAppender(new SimpleLayout(), "/Users/zouyi/Downloads/workFlow_Log/loggern.log"));
    }

    @Override
    protected void runDone() throws InterruptedException {

    }

    @Override
    protected void kill() {
        if (this.job != null && !this.job.isStopped()) {
            // this.job.stop();
            this.job.stopAll();
            String msg = "kettleAction is kill succeeded";
            logger.info(msg);
            this.setWorkFlowLogger(msg);
            try {
                this.getWorkFlow().getOutputStream().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
    protected void close() {

    }

    @Override
    public String getLog() {
        return null;
    }

    @Override
    public Map<String, Object> getOutPut() {
        return null;
    }
}
