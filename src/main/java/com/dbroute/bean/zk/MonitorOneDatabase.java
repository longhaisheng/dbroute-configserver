package com.dbroute.bean.zk;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import com.dbroute.domain.DbZkNodeDO;
import com.dbroute.util.FileUtil;
import com.dbroute.util.ZKUtil;

/**
 * 监控某一个数据库
 * 
 * @author longhaisheng
 * 
 */
public class MonitorOneDatabase {

	private Logger log = Logger.getLogger(MonitorOneDatabase.class);

	private DbZkNodeDO dbZkNode;

	private ZooKeeper zk;

	private Properties oneDatabaseProperties = new Properties();

	private Properties zkProperties = new Properties();

	private String zk_root_path;

	private Integer schedule_delay;

	private ZKManager zkManager = new ZKManager();

	ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	public MonitorOneDatabase(String one_db_properties_name) throws Exception {
		InputStream inputStream = null;
		InputStream zookeeperInputStream = null;
		try {
			inputStream = MonitorOneDatabase.class.getResourceAsStream("/" + one_db_properties_name);
			zookeeperInputStream = MonitorOneDatabase.class.getResourceAsStream("/zookeeper.properties");
			oneDatabaseProperties.load(inputStream);
			zkProperties.load(zookeeperInputStream);
			inputStream.close();
			zookeeperInputStream.close();
		} catch (IOException e) {
			log.error(e.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		zk_root_path = zkProperties.getProperty("zk_root_path").trim();
		schedule_delay = Integer.valueOf(zkProperties.getProperty("schedule_delay").trim());
		this.setDbZkNode();
		zkManager.createZooKeeper();
		this.zk = zkManager.getZookeeper();
	}

	private void setDbZkNode() {
		String db_name = oneDatabaseProperties.getProperty("db_name").trim();
		String db_user_name = oneDatabaseProperties.getProperty("db_user_name").trim();
		String db_password = oneDatabaseProperties.getProperty("db_password").trim();
		String db_port = oneDatabaseProperties.getProperty("db_port", "3306").trim();

		String is_master = oneDatabaseProperties.getProperty("is_master", "false").trim();
		String is_slave = oneDatabaseProperties.getProperty("is_slave", "false").trim();
		String slaveWeight = oneDatabaseProperties.getProperty("slaveWeight", "1").trim();

		String app_name = oneDatabaseProperties.getProperty("app_name").trim();
		String db_host = oneDatabaseProperties.getProperty("db_host").trim();
		String in_php_config_name = oneDatabaseProperties.getProperty("in_php_config_name").trim();
		boolean dbIsDoubleMaster = Boolean.valueOf(oneDatabaseProperties.getProperty("db_is_double_master", "false").trim());

		dbZkNode = new DbZkNodeDO();
		dbZkNode.setDbName(db_name);
		dbZkNode.setUserName(db_user_name);
		dbZkNode.setPassWord(db_password);
		dbZkNode.setPort(db_port);

		dbZkNode.setMaster(Boolean.valueOf(is_master));
		dbZkNode.setSlave(Boolean.valueOf(is_slave));
		dbZkNode.setReadWeight(Integer.valueOf(slaveWeight));

		dbZkNode.setAppName(app_name);
		dbZkNode.setDbHost(db_host);
		dbZkNode.setInPhpConfigName(in_php_config_name);

		dbZkNode.setNodePath(dbZkNode.getAppName() + FileUtil.SPLIT_STR + dbZkNode.getInPhpConfigName());
		dbZkNode.setDbIsDoubleMaster(dbIsDoubleMaster);

	}

	private boolean dbIsAlive() {
		String driver = "com.mysql.jdbc.Driver";
		String url = "jdbc:mysql://" + getDbZkNode().getDbHost() + ":" + getDbZkNode().getPort() + "/" + getDbZkNode().getDbName();
		String userName = getDbZkNode().getUserName();
		String passWord = getDbZkNode().getPassWord();
		String name = null;
		Connection conn = null;
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(url, userName, passWord);
			String sql = "select 1 ";
			PreparedStatement statement = conn.prepareStatement(sql);
			ResultSet rs = statement.executeQuery(sql);
			while (rs.next()) {
				name = rs.getString(1);
				name = new String(name.getBytes("ISO-8859-1"), "GB2312");
			}
			rs.close();
		} catch (ClassNotFoundException e) {
			log.error("ClassNotFoundException for:" + e.getMessage());
		} catch (SQLException e) {
			log.error("SQLException for:" + e.getMessage());
		} catch (Exception e) {
			log.error("Exception for:" + e.getMessage());
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				log.error("conn SQLException for " + e.getMessage());
			}
		}
		if (null != name) {
			log.info(getDbZkNode().getDbHost() + ":" + getDbZkNode().getPort() + ZKManager.ZK_SEPARATOR + getDbZkNode().getDbName() + " db is alive ");
			return true;
		}
		return false;
	}

	private boolean createDbPath() throws Exception {// 创建数据库节点
		if (!this.getDbZkNode().isMaster() && !this.getDbZkNode().isSlave()) {
			log.info("database config error,master and slave 至少一个配置为true ");
			return false;
		}

		String path = this.zk_root_path + ZKManager.ZK_SEPARATOR + getDbZkNode().getNodePath();

		if (zk.exists(path, false) == null) {
			ZKUtil.createPath(zk, path, CreateMode.PERSISTENT, this.zkManager.getAcl());
		}

		String master_db_path = getDbZkNode().getMasterDbPath(path);
		String slave_db_path = getDbZkNode().getSlaveDbPath(path);
		boolean is_create = false;
		if (master_db_path != null && master_db_path != "") {
			if (zk.exists(master_db_path, false) == null) {
				CreateMode createMode = CreateMode.PERSISTENT;
				if (dbZkNode.isDbIsDoubleMaster()) {// 双master 建立临时结点
					createMode = CreateMode.EPHEMERAL;
				}
				String str = zk.create(master_db_path, null, this.zkManager.getAcl(), createMode);
				log.info(" create db_path success ,the path is : " + str + " mode is " + createMode.toString());
				is_create = true;
			} else {
				log.info(master_db_path + " path has exists ");
			}
		}
		if (slave_db_path != null && slave_db_path != "") {
			if (zk.exists(slave_db_path, false) == null) {
				String str = zk.create(slave_db_path, null, this.zkManager.getAcl(), CreateMode.EPHEMERAL);
				log.info(" create db_path success ,the path is : " + str);
				is_create = true;
			} else {
				log.info(slave_db_path + " path has exists ");
			}
		}
		return is_create;
	}

	public void check_db_is_alive() {
		boolean is = this.dbIsAlive();
		if (is) {
			try {
				this.createDbPath();
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		} else {
			String path = this.zk_root_path + ZKManager.ZK_SEPARATOR + getDbZkNode().getNodePath();
			String master_db_path = getDbZkNode().getMasterDbPath(path);
			String slave_db_path = getDbZkNode().getSlaveDbPath(path);
			try {
				if (this.dbZkNode.isDbIsDoubleMaster()) {// 双master can delete
					if (master_db_path != null && master_db_path != "" && zk.exists(master_db_path, false) != null) {
						zk.delete(master_db_path, -1);
						log.info(" delete success for master_db_path " + master_db_path);
					}
				}
				if (slave_db_path != null && slave_db_path != "" && zk.exists(slave_db_path, false) != null) {
					zk.delete(slave_db_path, -1);
					log.info(" delete success for slave_db_path " + slave_db_path);
				}
			} catch (InterruptedException e) {
				log.error(e.getMessage());
			} catch (KeeperException e) {
				log.error(e.getMessage());
			}
		}
	}

	public DbZkNodeDO getDbZkNode() {
		return dbZkNode;
	}

	public void setDbZkNode(DbZkNodeDO dbZkNode) {
		this.dbZkNode = dbZkNode;
	}

	public void close() throws InterruptedException {
		log.info("============ zk close ==================");
		this.zk.close();
	}

	public void watcherDB() {
		scheduler.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				check_db_is_alive();
			}
		}, 1, schedule_delay, TimeUnit.MILLISECONDS);
	}

	public static void main(String args[]) throws Exception {
		final MonitorOneDatabase monitor = new MonitorOneDatabase("sc_refund_0000.properties");
		monitor.watcherDB();
	}

}
