package com.dbroute.domain;

import java.io.Serializable;
import java.util.List;

public class DbConfigVariableDO implements Serializable{

	private static final long serialVersionUID = 5323374695827116555L;

	/**  php应用名 */
	private String appName;
	
	/**  php应用中配置项名 */
	private String dbConfigName;
	
	/**  基础配置信息 */
	private String baseConfig;

	/**  主库配置信息 */
	private List<String> masterDbs;

	/**  从库配置信息 */
	private List<String> slaveDbs;

	public String getDbConfigName() {
		return dbConfigName;
	}

	public void setDbConfigName(String dbConfigName) {
		this.dbConfigName = dbConfigName;
	}

	public String getBaseConfig() {
		return baseConfig;
	}

	public void setBaseConfig(String baseConfig) {
		this.baseConfig = baseConfig;
	}

	public List<String> getMasterDbs() {
		return masterDbs;
	}

	public void setMasterDbs(List<String> masterDbs) {
		this.masterDbs = masterDbs;
	}

	public List<String> getSlaveDbs() {
		return slaveDbs;
	}

	public void setSlaveDbs(List<String> slaveDbs) {
		this.slaveDbs = slaveDbs;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	
}
