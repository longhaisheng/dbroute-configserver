package com.dbroute.bean.zk;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * 监控所有数据库
 * 
 * @author longhaisheng
 * 
 */
public class MonitorAllDatabases {

	private Logger log = Logger.getLogger(MonitorAllDatabases.class);

	private Properties all_dbs_prop_conf_props = new Properties();

	List<MonitorOneDatabase> list = new ArrayList<MonitorOneDatabase>();

	public MonitorAllDatabases() throws Exception {
		InputStream inputStream = null;
		try {
			inputStream = MonitorAllDatabases.class.getResourceAsStream("/all_dbs_prop_conf.properties");
			all_dbs_prop_conf_props.load(inputStream);
			inputStream.close();
		} catch (IOException e) {
			log.error(e.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage());
		}

	}

	/**
	 * 监控所有数据库
	 * @throws Exception
	 */
	public void watcherAll() throws Exception {
		String all_db_properties = all_dbs_prop_conf_props.getProperty("all_dbs_prop_conf_name").trim();
		if (all_db_properties != null && all_db_properties != "") {
			String[] all_files = all_db_properties.split(",");
			for (String str : all_files) {
				if (str.endsWith(".properties")) {
					MonitorOneDatabase oneMonitor = new MonitorOneDatabase(str);
					list.add(oneMonitor);
					oneMonitor.watcherDB();
				}
			}
		}
	}

	public void close() {
		if (!list.isEmpty()) {
			for (MonitorOneDatabase oneMonitor : list) {
				try {
					oneMonitor.close();
				} catch (InterruptedException e) {
					log.error(e.getMessage());
				}
			}
		}
	}

	public static void main(String args[]) throws Exception {
		final MonitorAllDatabases monitor = new MonitorAllDatabases();
		monitor.watcherAll();
	}
}
