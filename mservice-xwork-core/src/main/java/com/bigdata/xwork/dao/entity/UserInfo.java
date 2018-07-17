package com.bigdata.xwork.dao.entity;

public class UserInfo {
	
	private long userid;
	
	private String userName;
	
	private String groupName;
	
	private long groupid;
	
	public long createTime;

	public long getUserid() {
		return userid;
	}

	public void setUserid(long userid) {
		this.userid = userid;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getGroupid() {
		return groupid;
	}

	public void setGroupid(long groupid) {
		this.groupid = groupid;
	}

}
