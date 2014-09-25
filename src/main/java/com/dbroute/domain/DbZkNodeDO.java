package com.dbroute.domain;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.dbroute.util.FileUtil;

public class DbZkNodeDO implements Serializable {

	private static final long serialVersionUID = -4917646160903443667L;

	/** mmall@@@db_pay_info_config @@@前为应用名 后为 php配置文件中的数据库配置项变量名 */
	private String nodePath;

	/** 数据库名 */
	private String dbName;

	/** 数据库端口 */
	private String port;

	/** 数据库用户名 */
	private String userName;

	/** 数据库密码 */
	private String passWord;

	/** 数据库地址 */
	private String dbHost;

	/** 数据库是否是主库 */
	private boolean isMaster;

	/** 数据库是否是从库 */
	private boolean isSlave;

	/** 读库权重 */
	private int readWeight = 1;

	/** 数据库是被使用在哪个app中 */
	private String appName;

	/** 数据库在php配置文件中被哪个变量名引用 如 single_db_pay_info_config */
	private String inPhpConfigName;

	/** 此库是否属于双master */
	private boolean dbIsDoubleMaster;

	public String getNodePath() {
		return nodePath;
	}

	public String getDbName() {
		return dbName;
	}

	public String getPort() {
		return port;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassWord() {
		return passWord;
	}

	public boolean isMaster() {
		return isMaster;
	}

	public boolean isSlave() {
		return isSlave;
	}

	public int getReadWeight() {
		return readWeight;
	}

	public String getAppName() {
		return appName;
	}

	public String getInPhpConfigName() {
		return inPhpConfigName;
	}

	public void setNodePath(String nodePath) {
		this.nodePath = nodePath;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}

	public void setMaster(boolean isMaster) {
		this.isMaster = isMaster;
	}

	public void setSlave(boolean isSlave) {
		this.isSlave = isSlave;
	}

	public void setReadWeight(int readWeight) {
		this.readWeight = readWeight;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public void setInPhpConfigName(String inPhpConfigName) {
		this.inPhpConfigName = inPhpConfigName;
	}

	public String getDbHost() {
		return dbHost;
	}

	public void setDbHost(String dbHost) {
		this.dbHost = dbHost;
	}

	public boolean isDbIsDoubleMaster() {
		return dbIsDoubleMaster;
	}

	public void setDbIsDoubleMaster(boolean dbIsDoubleMaster) {
		this.dbIsDoubleMaster = dbIsDoubleMaster;
	}

	public String getMasterDbPath(String parentPath) {
		if (this.isMaster) {
			String ip = this.get_db_ip();
			String db_path = parentPath;
			db_path = db_path + "/" + "master" + FileUtil.SPLIT_STR + this.dbName + FileUtil.SPLIT_STR + ip;
			return db_path;
		}
		return "";
	}

	public String get_db_ip() {// 取数据库的ip
		String ip = "";
		if (this.getDbHost().equals("127.0.0.1") || this.getDbHost().endsWith("localhost")) {
			InetAddress addr;
			try {
				addr = InetAddress.getLocalHost();
				ip = addr.getHostAddress();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		} else {
			ip = this.getDbHost();
		}
		return ip;
	}

	public String getSlaveDbPath(String parentPath) {
		if (this.isSlave) {
			String ip = "";
			InetAddress addr;
			try {
				addr = InetAddress.getLocalHost();
				ip = addr.getHostAddress();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			String db_path = parentPath;
			db_path = db_path + "/" + "slave" + FileUtil.SPLIT_STR + this.dbName + FileUtil.SPLIT_STR + ip;
			if (this.isSlave && this.getReadWeight() > 0) {
				db_path = db_path + "_" + this.getReadWeight();
			}
			return db_path;
		}
		return "";
	}

}
