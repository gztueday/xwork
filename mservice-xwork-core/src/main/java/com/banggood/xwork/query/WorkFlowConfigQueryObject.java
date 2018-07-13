package com.banggood.xwork.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * Created by zouyi on 2018/1/11.
 */
@ApiModel(value = "调度查询对象", description = "调度配置查询对象")
public class WorkFlowConfigQueryObject extends QueryObject {

    @ApiModelProperty(dataType = "String", value = "搜索关键字")
    protected String keyword;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(dataType = "date-time", value = "开始时间(默认一个星期前)")
    protected Date beginDate = DateUtil.getDayAgo(new Date(), 7);

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(dataType = "date-time", value = "结束时间(默认当天)")
    protected Date endDate = DateUtil.getDayAgo(new Date(), -1);

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
