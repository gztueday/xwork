package com.bigdata.xwork.api.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by zouyi on 2018/3/28.
 */
public class LineResult implements Serializable {
    private List<String> content;
    private long line;

    public List<String> getContent() {
        return content;
    }

    public void setContent(List<String> content) {
        this.content = content;
    }

    public long getLine() {
        return line;
    }

    public void setLine(long line) {
        this.line = line;
    }
}
