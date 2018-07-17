package com.bigdata.xwork.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@ApiModel(value = "workFlow实例", description = "搜索workFlow实例列表")
public class WorkFlowInstanceQueryObject extends QueryObject {
    @ApiModelProperty(dataType = "string", value = "搜索关键字")
    private String keyword;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(dataType = "date-time", value = "开始时间(默认一个星期前)")
    private Date beginDate = DateUtil.getDayAgo(new Date(), 100);

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(dataType = "date-time", value = "结束时间(默认当天)")
    private Date endDate = DateUtil.getDayAgo(new Date(), -1);

    @ApiModelProperty(dataType = "boolean", value = "false(执行中任务morning),true(已经完成的任务)")
    private String status ;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
