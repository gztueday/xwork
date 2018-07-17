package com.bigdata.xwork.query;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Date;

/**
 * Created by zouyi on 2018/3/14.
 */
public class VersionResult {

    private long version;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date date;

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
