package com.bigdata.mservice.xwork.rest;

import com.alibaba.fastjson.JSONObject;
import com.banggood.xwork.api.entity.EventResponse;
import com.banggood.xwork.dao.service.HDFSService;
import com.banggood.xwork.query.HDFSDirQueryObject;
import com.banggood.xwork.query.result.DirResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.hadoop.fs.FileStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by zouyi on 2018/3/28.
 */
@RestController
@RequestMapping("/hdfs")
@Api(value = "hdfs链接", description = "hdfs系统链接"
        , produces = "application/json", protocols = "http")
public class HDFSController {

    @Autowired
    private HDFSService hdfsService;
    private final static Logger logger = LoggerFactory.getLogger(HDFSController.class);

    @RequestMapping(value = {"/file/dir"}, method = {RequestMethod.POST})
    @ApiOperation(value = "获取hdfs上的目录", code = 200, httpMethod = "POST", response = Boolean.class)
    public DirResult dir(@RequestBody String qoStr) {
        HDFSDirQueryObject qo = null;
        try {
            if (qoStr == null) {
                qo = new HDFSDirQueryObject();
            } else {
                qo = JSONObject.parseObject(qoStr, HDFSDirQueryObject.class);
            }
            DirResult dir = hdfsService.dir(qo);
            return dir;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    @RequestMapping(value = {"/file/upload"}, method = {RequestMethod.POST})
    @ApiImplicitParam(value = "路径", name = "path", dataType = "String"
            , required = false, paramType = "query")
    @ApiOperation(value = "获取hdfs上的目录", code = 200, httpMethod = "POST", response = Boolean.class)
    public EventResponse upload(@RequestParam("path") String path, @RequestParam("file") MultipartFile file) {
        EventResponse eventResponse = new EventResponse();
        if (!file.isEmpty()) {
            try {
                if ("".equals(path)) {
                    path = "/";
                }
                hdfsService.upload(path, file);
                eventResponse.setDesc("上传文件成功");
                eventResponse.setStatus(HttpServletResponse.SC_OK);
                return eventResponse;
            } catch (Exception e) {
                eventResponse.setDesc(e.getMessage());
                eventResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                logger.error(e.getMessage(), e);
                return eventResponse;
            }
        } else {
            eventResponse.setDesc("上传的文件为空");
            eventResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return eventResponse;
        }
    }
}
