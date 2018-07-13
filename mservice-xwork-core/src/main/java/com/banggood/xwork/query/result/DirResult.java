package com.banggood.xwork.query.result;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Date;
import java.util.List;

/**
 * Created by zouyi on 2018/3/28.
 */
public class DirResult extends PageResult {

    public DirResult(List listData, Integer totalCount, Integer currentPage, Integer pageSize, String firstPath) {
        super(listData, totalCount, currentPage, pageSize);
        this.firstPath = firstPath;
    }

    private String firstPath;

    public DirResult() {
    }

    public String getFirstPath() {
        return firstPath;
    }

    public void setFirstPath(String firstPath) {
        this.firstPath = firstPath;
    }

    public  Path createPath() {
        return new Path();
    }


    public class Path {
        private String path;
        private boolean isDirectory;
        private String owner;
        private String group;
        private String premission;
        private long size;
        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        private Date updateDate;

        public Date getUpdateDate() {
            return updateDate;
        }

        public void setUpdateDate(Date updateDate) {
            this.updateDate = updateDate;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public boolean isDirectory() {
            return isDirectory;
        }

        public void setDirectory(boolean directory) {
            isDirectory = directory;
        }

        public String getOwner() {
            return owner;
        }

        public void setOwner(String owner) {
            this.owner = owner;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public String getPremission() {
            return premission;
        }

        public void setPremission(String premission) {
            this.premission = premission;
        }
    }


}
