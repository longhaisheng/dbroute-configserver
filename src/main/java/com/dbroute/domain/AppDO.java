package com.dbroute.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class AppDO implements Serializable{

	private static final long serialVersionUID = 7494688350687540656L;

	/** 应用名 */
	private String appName;

	/** 应用名下所有 php应用中 逻辑库表变量 */
	private List<DbConfigVariableDO> dbConfigs = new ArrayList<DbConfigVariableDO>();

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public List<DbConfigVariableDO> getDbConfigs() {
		return dbConfigs;
	}

	public void setDbConfigs(List<DbConfigVariableDO> dbConfigs) {
		this.dbConfigs = dbConfigs;
	}

}
