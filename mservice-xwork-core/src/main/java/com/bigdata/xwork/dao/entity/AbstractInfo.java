package com.bigdata.xwork.dao.entity;

public abstract class AbstractInfo {
	
	/**
	 * 配置的用户
	 * */
	protected long configerid;
	
	/**
	 * 更新的用户
	 * */
	protected long updaterid;
	
	/**
	 * 提交的用户
	 * */
	protected long submiterid;


	protected boolean share;

	protected String descript;
	
	/**
	 * 配置参数
	 * */
	protected String configParamJSON;

	public String getDescript() {
		return descript;
	}

	public void setDescript(String descript) {
		this.descript = descript;
	}

	public long getConfigerid() {
		return configerid;
	}


	public void setConfigerid(long configerid) {
		this.configerid = configerid;
	}


	public long getUpdaterid() {
		return updaterid;
	}


	public void setUpdaterid(long updaterid) {
		this.updaterid = updaterid;
	}


	public long getSubmiter() {
		return submiterid;
	}


	public void setSubmiter(long submiterid) {
		this.submiterid = submiterid;
	}



	public String getConfigParamJSON() {
		return configParamJSON;
	}


	public void setConfigParamJSON(String configParamJSON) {
		this.configParamJSON = configParamJSON;
	}


	public boolean isShare() {
		return share;
	}


	public void setShare(boolean share) {
		this.share = share;
	}

}
