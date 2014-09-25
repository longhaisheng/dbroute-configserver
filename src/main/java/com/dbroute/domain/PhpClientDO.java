package com.dbroute.domain;

import java.io.Serializable;

/**
 * @author haisheng
 * 
 */
public class PhpClientDO implements Serializable{

	private static final long serialVersionUID = 6017307067824418423L;

	/** 应用名 */
	private String appName;

	/** php配置文件中的数据库配置项名 */
	private String dbConfigName;

	/** 生成php配置文件的路径 */
	private String generateFilePath;

	/** mmall@@@db_pay_info_config @@@前为应用名 后为 php配置文件中的数据库配置项名 */
	private String nodePath;

	/** nodePath的子节点,如 mmall@@@db_pay_info_config/base_config */
	private String childPath;

	public String getAppName() {
		return appName;
	}

	public String getDbConfigName() {
		return dbConfigName;
	}

	public String getGenerateFilePath() {
		return generateFilePath;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public void setDbConfigName(String dbConfigName) {
		this.dbConfigName = dbConfigName;
	}

	public void setGenerateFilePath(String generateFilePath) {
		this.generateFilePath = generateFilePath;
	}

	public String getNodePath() {
		return nodePath;
	}

	public String getChildPath() {
		return childPath;
	}

	public void setNodePath(String nodePath) {
		this.nodePath = nodePath;
	}

	public void setChildPath(String childPath) {
		this.childPath = childPath;
	}

}
