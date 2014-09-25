package com.dbroute.bean.zk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;

import com.dbroute.domain.PhpClientDO;
import com.dbroute.util.FileUtil;

public class PhpClientWatcher implements Watcher {

	private Logger log = Logger.getLogger(PhpClientWatcher.class);

	private ZKManager zkmanager;

	private List<PhpClientDO> phpClientList = new ArrayList<PhpClientDO>();

	public PhpClientWatcher(ZKManager zkmanager) {
		this.zkmanager = zkmanager;
		this.setPhpClientList(zkmanager);
	}

	private void setPhpClientList(ZKManager zkmanager) {
		List<String> phpClients = zkmanager.getPhpClient();
		Map<String, String> generatePathMap = this.zkmanager.getGeneratePath();
		String zkRootPath = zkmanager.getZkRootPath();
		if (!phpClients.isEmpty()) {
			for (String path : phpClients) {
				PhpClientDO client = new PhpClientDO();
				String[] strs = path.split(FileUtil.SPLIT_STR);
				client.setAppName(strs[0]);
				client.setDbConfigName(strs[1]);
				client.setGenerateFilePath(generatePathMap.get(strs[0]));
				client.setChildPath(zkRootPath + ZKManager.ZK_SEPARATOR + path + "/base_config");
				client.setNodePath(zkRootPath + ZKManager.ZK_SEPARATOR + path);
				phpClientList.add(client);
			}
		}
	}

	@Override
	public void process(WatchedEvent event) {
		log.info("已经触发了" + event.getType() + "事件！");
		String event_path = event.getPath();
		ZooKeeper zookeeper = zkmanager.getZookeeper();
		if (event.getType() == Event.EventType.NodeDataChanged) {
			try {
				if (!this.phpClientList.isEmpty()) {
					for (PhpClientDO client : this.phpClientList) {
						String baseConfigPath = client.getChildPath();
						if (baseConfigPath.equals(event_path)) {
							String app_db_name_path = client.getNodePath();
							String db_config_name = client.getDbConfigName();
							List<String> list = new ArrayList<String>();
							byte[] bytes = new byte[0];
							if (zookeeper.exists(baseConfigPath, false) != null) {
								bytes = zookeeper.getData(baseConfigPath, true, null);
							}
							if (zookeeper.exists(app_db_name_path, false) != null) {
								list = zookeeper.getChildren(app_db_name_path, true);
							}
							log.info("NodeDataChanged path is : " + event_path);
							String str = FileUtil.listToString(list, bytes, db_config_name);
							FileUtil.writeFile(client.getGenerateFilePath(), "<?php\n" + str);
						}
					}
				}
			} catch (KeeperException e) {
				log.error(e.getMessage());
			} catch (InterruptedException e) {
				log.error(e.getMessage());
			}
		}
		if (event.getType() == Event.EventType.NodeChildrenChanged) {
			try {
				if (!this.phpClientList.isEmpty()) {
					for (PhpClientDO client : this.phpClientList) {
						String app_db_name_path = client.getNodePath();
						if (app_db_name_path.equals(event_path)) {
							String baseConfigPath = client.getChildPath();
							String db_config_name = client.getDbConfigName();
							List<String> list = new ArrayList<String>();
							byte[] bytes = new byte[0];
							if (zookeeper.exists(baseConfigPath, false) != null) {
								bytes = zookeeper.getData(baseConfigPath, true, null);
							}
							if (zookeeper.exists(app_db_name_path, false) != null) {
								list = zookeeper.getChildren(app_db_name_path, true);
							}
							log.info("NodeChildrenChanged path is : " + event_path);
							String str = FileUtil.listToString(list, bytes, db_config_name);
							FileUtil.writeFile(client.getGenerateFilePath(), "<?php\n" + str);
						}
					}
				}
			} catch (KeeperException e) {
				log.error(e.getMessage());
			} catch (InterruptedException e) {
				log.error(e.getMessage());
			}
		} else if (event.getState() == KeeperState.SyncConnected) {
			log.info("收到ZK连接成功事件！");
		} else if (event.getState() == KeeperState.Expired) {
			log.error("会话超时，等待重新建立ZK连接...");
			try {
				zkmanager.reConnection();

				if (!this.phpClientList.isEmpty()) {
					for (PhpClientDO client : this.phpClientList) {
						if (zookeeper.exists(client.getNodePath(), false) != null) {
							this.zkmanager.getZookeeper().getChildren(client.getNodePath(), true);
						}
						if (zookeeper.exists(client.getChildPath(), false) != null) {
							this.zkmanager.getZookeeper().getData(client.getChildPath(), true, null);
						}
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
	}

	public static void main(String args[]) {
		// PhpClientWatcher client = new PhpClientWatcher(new ZKManager());
		List<String> list = new ArrayList<String>();
		list.add("master@@@db_pay_info_0001@@@192.168.1.1_3");
		list.add("master@@@db_pay_info_0001@@@192.168.1.2_2");
		list.add("master@@@db_pay_info_0002@@@192.168.1.1_3");
		list.add("master@@@db_pay_info_0002@@@192.168.1.2_2");
		list.add("slave@@@db_pay_info_0001@@@192.168.1.3_3");
		list.add("slave@@@db_pay_info_0001@@@192.168.1.4_3");
		list.add("slave@@@db_pay_info_0002@@@192.168.1.3_3");
		list.add("slave@@@db_pay_info_0002@@@192.168.1.4_3");
		System.out.println(FileUtil.listToString(list, null, "mmall@@@db_pay_info_config"));

		String node_path = "mmall@@@db_pay_info_config";

		PhpClientDO php = new PhpClientDO();
		String[] strs = node_path.split(FileUtil.SPLIT_STR);
		php.setAppName(strs[0]);
		php.setDbConfigName(strs[1]);
		php.setGenerateFilePath("/app/" + strs[0] + "/includes/db_config.php");
		php.setChildPath(node_path + "/base_config");
		php.setNodePath(node_path);

		System.out.println(php.getGenerateFilePath());
		FileUtil.writeFile("E:\\db_config.php", "<?php\n" + FileUtil.listToString(list, null, php.getDbConfigName()));

		// String baseConfig =
		// "host=192.168.1.1;user_name=lhs001;pass_word=123456;consistent_hash_separate_string='[0,256w]=sc_refund_0000;[256w,512w]=sc_refund_0001;[512,768]=sc_refund_0002;[768,1024]=sc_refund_0003;'";
		// baseConfig="$a['host']='192.168.1.1';$a['user_name']=lhs001;$a['pass_word']=123456;consistent_hash_separate_string='[0,256w]=sc_refund_0000;[256w,512w]=sc_refund_0001;[512,768]=sc_refund_0002;[768,1024]=sc_refund_0003;'";
	}

}
