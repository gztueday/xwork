package com.bigdata.xwork.hdfs;

import com.bigdata.xwork.core.service.XWorkApp;
import com.bigdata.xwork.dao.service.HDFSService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.IOException;

/**
 * Created by zouyi on 2018/3/28.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = XWorkApp.class)
@WebAppConfiguration
public class HdfsTest {

    @Autowired
    private HDFSService hdfsService;

    @Test
    public void dir() throws IOException {
//        this.hdfsService.dir("/");
    }

}
