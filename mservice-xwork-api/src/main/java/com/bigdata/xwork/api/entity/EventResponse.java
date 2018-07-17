package com.bigdata.xwork.api.entity;

import java.io.Serializable;

public class EventResponse implements Serializable{
	//1,有错误  0,通过
	private int status;
	
	private String desc;
	
	private String json;

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getJson() {
		return json;
	}

	public void setJson(String json) {
		this.json = json;
	}

}
