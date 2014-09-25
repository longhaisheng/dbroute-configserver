package com.dbroute.mvc.zk;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletRequest;

import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.dbroute.bean.zk.ZKManager;
import com.dbroute.domain.AppDO;
import com.dbroute.domain.DbConfigVariableDO;
import com.dbroute.util.FileUtil;

@Controller
@RequestMapping("/zklist")
public class ZkController {

	private Logger log = Logger.getLogger(ZkController.class);

	private ZKManager zkManager = new ZKManager();

	@ModelAttribute
	public void initZK() {
		try {
			zkManager.createZooKeeper();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	@RequestMapping(method = RequestMethod.GET)
	public String list(Model model) {// all app names
		List<String> appNames = new ArrayList<String>();
		// appNames.add("mmall");
		// appNames.add("order");
		// appNames.add("orderGoods");
		// appNames.add("payInfo");

		try {
			String rootPath = zkManager.getZkRootPath();
			log.info(" ZkController : " + rootPath);

			List<String> list = zkManager.getZookeeper().getChildren(rootPath, false);
			for (String str : list) {
				if (str.indexOf(FileUtil.SPLIT_STR) > 0) {
					String[] strArray = str.split(FileUtil.SPLIT_STR);
					appNames.add(strArray[0]);
				}
			}
			// zk.getZookeeper().close();
		} catch (KeeperException e) {
			log.error(e.getMessage());
		} catch (InterruptedException e) {
			log.error(e.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		model.addAttribute("all_app_names", appNames);
		return "zk/app_name";
	}

	public List<AppDO> getList(String str_app_name) {// 根据根节点 取所有数据,封装为对象
		List<AppDO> apps = new ArrayList<AppDO>();
		Set<String> set = new HashSet<String>();
		List<DbConfigVariableDO> dbConfigList = new ArrayList<DbConfigVariableDO>();
		try {
			String rootPath = zkManager.getZkRootPath();
			List<String> list = zkManager.getZookeeper().getChildren(rootPath, false);
			if (!list.isEmpty()) {
				for (String str : list) {
					List<String> masterDbs = new ArrayList<String>();
					List<String> slaveDbs = new ArrayList<String>();
					log.info("+++++++++++++++++++++++++++++++++:::" + str);
					if (str.startsWith(str_app_name + "@@@") && str.indexOf(FileUtil.SPLIT_STR) > 0) {
						DbConfigVariableDO dbConfigVariable = new DbConfigVariableDO();
						String[] strArray = str.split(FileUtil.SPLIT_STR);
						String appName = strArray[0];
						String dbConfigName = strArray[1];
						set.add(appName);

						List<String> newList = zkManager.getZookeeper().getChildren(rootPath + "/" + str, false);
						if (!newList.isEmpty()) {
							for (String newStr : newList) {
								if (newStr.startsWith("master@@@")) {
									masterDbs.add(newStr);
								}
								if (newStr.startsWith("slave@@@")) {
									slaveDbs.add(newStr);
								}
							}
						}

						if (zkManager.getZookeeper().exists(rootPath + "/" + str + "/" + "base_config", false) != null) {
							byte[] base_config_bytes = zkManager.getZookeeper().getData(rootPath + "/" + str + "/" + "base_config", false, null);
							if (base_config_bytes != null) {
								String base_config_str = new String(base_config_bytes);
								dbConfigVariable.setBaseConfig(base_config_str);
							}
						}

						dbConfigVariable.setDbConfigName(dbConfigName);
						dbConfigVariable.setMasterDbs(masterDbs);
						dbConfigVariable.setSlaveDbs(slaveDbs);
						dbConfigVariable.setAppName(appName);

						dbConfigList.add(dbConfigVariable);

					}
				}
			}

			if (!set.isEmpty()) {
				for (String app_name : set) {
					AppDO app = new AppDO();
					app.setAppName(app_name);

					List<DbConfigVariableDO> app_all_db_configs = new ArrayList<DbConfigVariableDO>();
					for (DbConfigVariableDO db_config_var : dbConfigList) {
						if (app_name.equals(db_config_var.getAppName())) {
							app_all_db_configs.add(db_config_var);
						}
					}
					app.setDbConfigs(app_all_db_configs);

					apps.add(app);
				}
			}

			// zk.getZookeeper().close();
		} catch (KeeperException e) {
			log.error(e.getMessage());
		} catch (InterruptedException e) {
			log.error(e.getMessage());
		}

		return apps;

		// ////////
		// AppDO app = new AppDO();
		// app.setAppName("mmall");
		// List<DbConfigVariableDO> dbConfigs = new
		// ArrayList<DbConfigVariableDO>();
		//
		// DbConfigVariableDO db_pay_info_varialbe = new DbConfigVariableDO();
		// db_pay_info_varialbe.setBaseConfig("db_pay_info_contents");
		// db_pay_info_varialbe.setDbConfigName("single_db_pay_info_config");
		//
		// List<String> db_pay_info_master_dbs = new ArrayList<String>();
		// db_pay_info_master_dbs.add("master@@@sc_refund_0000@@@192.168.19.15");
		// db_pay_info_master_dbs.add("master@@@sc_refund_0001@@@192.168.19.16");
		// db_pay_info_master_dbs.add("master@@@sc_refund_0002@@@192.168.117");
		// db_pay_info_varialbe.setMasterDbs(db_pay_info_master_dbs);
		//
		// List<String> db_pay_info_slave_dbs = new ArrayList<String>();
		// db_pay_info_slave_dbs.add("slave@@@sc_refund_0000@@@192.168.19.15");
		// db_pay_info_slave_dbs.add("slave@@@sc_refund_0001@@@192.168.19.16");
		// db_pay_info_slave_dbs.add("slave@@@sc_refund_0002@@@192.168.19.17");
		// db_pay_info_varialbe.setSlaveDbs(db_pay_info_slave_dbs);
		//
		// DbConfigVariableDO order_varialbe = new DbConfigVariableDO();
		// order_varialbe.setBaseConfig("contents");
		// order_varialbe.setDbConfigName("single_db_sc_refund_config");
		//
		// List<String> order_master_dbs = new ArrayList<String>();
		// order_master_dbs.add("master@@@sc_refund_0000@@@192.168.19.15");
		// order_master_dbs.add("master@@@sc_refund_0001@@@192.168.19.16");
		// order_master_dbs.add("master@@@sc_refund_0002@@@192.168.19.17");
		// order_varialbe.setMasterDbs(order_master_dbs);
		//
		// List<String> order_slave_dbs = new ArrayList<String>();
		// order_slave_dbs.add("slave@@@sc_refund_0000@@@192.168.19.15");
		// order_slave_dbs.add("slave@@@sc_refund_0001@@@192.168.19.16");
		// order_slave_dbs.add("slave@@@sc_refund_0002@@@192.168.19.17");
		// order_varialbe.setSlaveDbs(order_slave_dbs);
		//
		// dbConfigs.add(db_pay_info_varialbe);
		// dbConfigs.add(order_varialbe);
		//
		// app.setDbConfigs(dbConfigs);
		//
		// List<AppDO> app_list = new ArrayList<AppDO>();
		// app_list.add(app);
		// return app_list;

	}

	@RequestMapping(method = RequestMethod.GET, value = "/app/{appName}")
	public String app(@PathVariable String appName, Model model) {
		List<AppDO> app_list = this.getList(appName);
		model.addAttribute("app_list", app_list);
		return "zk/app_info";
	}

	@RequestMapping(method = RequestMethod.POST, value = "/app/{appName}")
	public String form(@PathVariable String appName, ServletRequest request, Model model) throws UnsupportedEncodingException {
		// ServletRequest request = ((ServletRequestAttributes)
		// RequestContextHolder.getRequestAttributes()).getRequest();// 用于ajax中获取

		Map<String, String[]> map = request.getParameterMap();
		Set<Entry<String, String[]>> set = map.entrySet();
		Iterator<Entry<String, String[]>> it = set.iterator();
		log.info("++++++++++++++++++++++++++++++++++++++++++++++" + request.getCharacterEncoding());
		List<String> master_dbs = new ArrayList<String>();
		List<String> slave_dbs = new ArrayList<String>();

		String app_name = request.getParameter("app_name").trim();
		String db_config_name = request.getParameter("db_config_name").trim();
		String base_config = request.getParameter("base_config").trim();
		base_config = new String(base_config.getBytes("ISO-8859-1"), "utf-8");
		// log.info("app_name" + app_name);
		// log.info("db_config_name" + db_config_name);
		log.info("++++++++++++++++++++++++base_config========================\n" + base_config);
		log.info("++++++++++++++++++++++++base_config========================\n");

		while (it.hasNext()) {
			Entry<String, String[]> entry = it.next();
			String key = entry.getKey();

			log.info(" key :" + key);
			for (String str : entry.getValue()) {
				if (key.startsWith("master_")) {
					master_dbs.add(str);
					log.info(str);
				}
				if (key.startsWith("slave_")) {
					slave_dbs.add(str);
					log.info(str);
				}
			}
		}

		String rootPath = zkManager.getZkRootPath();
		String base_config_path = rootPath + "/" + app_name + FileUtil.SPLIT_STR + db_config_name + "/" + "base_config";
		try {
			if (zkManager.getZookeeper().exists(base_config_path, false) != null) {
				zkManager.getZookeeper().setData(base_config_path, base_config.getBytes(), -1);
				model.addAttribute("success", "update success! ");
			}
		} catch (KeeperException e) {
			log.info(e.getMessage());
		} catch (InterruptedException e) {
			log.info(e.getMessage());
		}

		List<AppDO> app_list = this.getList(appName);
		model.addAttribute("app_list", app_list);
		return "zk/app_info";
	}

}
