package com.bigdata.xwork.dao.service;

import com.bigdata.xwork.action.impl.HiveAction;
import com.bigdata.xwork.query.HDFSDirQueryObject;
import com.bigdata.xwork.query.result.DirResult;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by zouyi on 2018/3/28.
 */
@Service
public class HDFSService {

    public static Logger logger = LoggerFactory.getLogger(HDFSService.class);

    public DirResult dir(HDFSDirQueryObject qo) throws IOException {

        FileStatus[] fileStatuses;
        FileSystem fileSystem = FileSystem.get(HiveAction.conf);
        String firstPath = null;
        DirResult dirResult = null;
        String path = qo.getPath();
        try {
            if (path == null || "".equals(path)) {
                fileStatuses = fileSystem.listStatus(new Path("/"));
                firstPath = "/";
            } else {
                fileStatuses = fileSystem.listStatus(new Path(path));
                firstPath = StringUtils.substringBeforeLast(path, "/");
                if ("".equals(firstPath)) {
                    firstPath = "/";
                }
            }
            List<DirResult.Path> fileList = new ArrayList<>(fileStatuses.length);
            for (FileStatus fileStatus : fileStatuses) {
                DirResult.Path p = new DirResult().createPath();
                p.setGroup(fileStatus.getGroup());
                p.setOwner(fileStatus.getOwner());
                p.setDirectory(fileStatus.isDirectory());
                p.setPremission(fileStatus.getPermission().toString());
                p.setPath(fileStatus.getPath().getName());
                p.setUpdateDate(new Date(fileStatus.getModificationTime()));
                p.setSize(fileStatus.getBlockSize());
                fileList.add(p);
            }
            List<DirResult.Path> data = new ArrayList<>(qo.getPageSize());
            for (int i = (qo.getCurrentPage() - 1) * qo.getPageSize(); i < (qo.getCurrentPage() - 1) * qo.getPageSize() + qo.getPageSize(); i++) {
                if (i <= fileList.size() - 1) {
                    data.add(fileList.get(i));
                }
            }
            dirResult = new DirResult(data, fileList.size(), qo.getCurrentPage(), qo.getPageSize(), firstPath);
        } finally {
            fileSystem.close();
        }
        return dirResult;
    }

    public void upload(String path, MultipartFile file) throws IOException {
        FileSystem fileSystem = null;
        try {
            fileSystem = FileSystem.get(HiveAction.conf);
            Path f = new Path(path);
            if (!fileSystem.exists(f)) {
                fileSystem.mkdirs(f);
            }
            if (fileSystem.isDirectory(f)) {
                //上传文件名
                String filename = file.getOriginalFilename();
                InputStream in = file.getInputStream();
                OutputStream os = fileSystem.create(new Path(path + filename));
                IOUtils.copyBytes(in, os, 4096, true);
            }
        } finally {
            if (fileSystem != null) {
                fileSystem.close();
            }
        }
    }
}
