package com.dbroute.bean.zk;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;

import com.dbroute.util.FileUtil;

/**
 * @author longhaisheng
 * 
 */
public class ZKManager {

	private Logger log = Logger.getLogger(ZKManager.class);

	public static final String ZK_SEPARATOR = "/";

	private ZooKeeper zookeeper;

	/** zk 根路径 */
	private String zkRootPath;

	/** acl 权限 */
	private List<ACL> acl = new ArrayList<ACL>();

	/** php应用节点列表 详见xml中的配置 */
	private List<String> phpClient = new ArrayList<String>();

	/** 生成配置文件路径的map key为应用名,value为生成路径 */
	private Map<String, String> generatePath = new HashMap<String, String>();

	private Properties zkProperties = new Properties();

	/** zk 是否注册watcher */
	private boolean phpClientWatcher = false;

	public ZKManager() {
		InputStream zookeeperInputStream = null;
		try {
			zookeeperInputStream = ZKManager.class.getResourceAsStream("/zookeeper.properties");
			zkProperties.load(zookeeperInputStream);
			zookeeperInputStream.close();
		} catch (IOException e) {
			log.error(e.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		zkRootPath = zkProperties.getProperty("zk_root_path").trim();
		log.info("zkmanager : " + zkRootPath);
	}

	public void setPhpClient(List<String> phpClient) {
		this.phpClient = phpClient;
	}

	public List<String> getPhpClient() {
		return phpClient;
	}

	public void init() {
		Map<String, String> generatePathMap = this.getGeneratePath();
		List<String> phpClient = this.getPhpClient();
		if (!phpClient.isEmpty() && !generatePathMap.isEmpty()) {
			for (String path : phpClient) {
				boolean find_generate_path = false;
				for (Map.Entry<String, String> map : generatePathMap.entrySet()) {
					String[] strs = path.split(FileUtil.SPLIT_STR);
					String app_name = strs[0];
					if (app_name.equals(map.getKey())) {
						find_generate_path = true;
					}
				}
				if (!find_generate_path) {
					log.error("初始化错误,未找到对应的项目生成路径!");
				}
			}
		}
		try {
			List<String> php_client_watcher_paths = this.getPhpClient();
			if (!php_client_watcher_paths.isEmpty()) {
				this.createZooKeeper();
				for (String phpClientPath : php_client_watcher_paths) {// watcher
					String base_config_path = this.getZkRootPath() + ZK_SEPARATOR + phpClientPath + "/base_config";
					if (this.getZookeeper().exists(base_config_path, false) == null) {
						this.getZookeeper().create(base_config_path, null, this.acl, CreateMode.PERSISTENT);
					}
					this.getZookeeper().getChildren(this.getZkRootPath() + ZK_SEPARATOR + phpClientPath, true);
					this.getZookeeper().getData(base_config_path, true, null);
				}
			}
		} catch (KeeperException e) {
			log.error(e.getMessage());
		} catch (InterruptedException e) {
			log.error(e.getMessage());
		} catch (IOException e) {
			log.error(e.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage());
		}

	}

	public void close() {
		try {
			this.getZookeeper().close();
		} catch (InterruptedException e) {
			log.error(e.getMessage());
		}
	}

	public void reConnection() throws Exception {
		if (this.getZookeeper().getState() == States.CLOSED) {
			if (this.getZookeeper() != null) {
				this.getZookeeper().close();
				this.setZookeeper(null);
			}
			this.createZooKeeper();
		}
	}

	public void createZooKeeper() throws Exception {
		if (zookeeper == null) {
			String authString = zkProperties.getProperty("zk_user_name").trim() + ":" + zkProperties.getProperty("zk_pass_word").trim();
			zookeeper = new ZooKeeper(zkProperties.getProperty("zk_connect_string").trim(), Integer.parseInt(zkProperties.getProperty("zk_session_timeout", "6000").trim()), null);

			String charset = zkProperties.getProperty("zk_charset", "utf-8");
			if (charset == null) {
				zookeeper.addAuthInfo("digest", authString.getBytes());
			} else {
				zookeeper.addAuthInfo("digest", authString.getBytes(charset));
			}
			acl.add(new ACL(ZooDefs.Perms.ALL, new Id("digest", DigestAuthenticationProvider.generateDigest(authString))));
			acl.add(new ACL(ZooDefs.Perms.READ, Ids.ANYONE_ID_UNSAFE));
			// boolean isCheckParentPath =
			// Boolean.parseBoolean(zkProperties.getProperty("is_check_parent_path",
			// "true"));
			if (this.isPhpClientWatcher()) {
				zookeeper.register(new PhpClientWatcher(this));
			} else {
				zookeeper.register(new Watcher() {
					@Override
					public void process(WatchedEvent event) {
						log.info("event ==========" + event.getType());
						if (event.getState() == KeeperState.SyncConnected) {
							log.info("zk SyncConnected successed ");
						} else if (event.getState() == KeeperState.Expired) {
							log.info("zk server is Expired ");
							try {
								reConnection();
							} catch (Exception e) {
								log.error(e.getMessage(), e);
							}
						} else {
							log.info("event type and state " + event.getType() + ":" + event.getState() + " path is " + event.getPath());
						}
					}
				});
			}
		}
	}

	private void setZookeeper(ZooKeeper zk) {
		this.zookeeper = zk;
	}

	public ZooKeeper getZookeeper() {
		return zookeeper;
	}

	public String getZkRootPath() {
		return zkRootPath;
	}

	public void setZkRootPath(String rootPath) {
		this.zkRootPath = rootPath;
	}

	public Map<String, String> getGeneratePath() {
		return generatePath;
	}

	public void setGeneratePath(Map<String, String> generatePath) {
		this.generatePath = generatePath;
	}

	public List<ACL> getAcl() {
		return acl;
	}

	public void setAcl(List<ACL> acl) {
		this.acl = acl;
	}

	public boolean isPhpClientWatcher() {
		return phpClientWatcher;
	}

	public void setPhpClientWatcher(boolean phpClientWatcher) {
		this.phpClientWatcher = phpClientWatcher;
	}

}
