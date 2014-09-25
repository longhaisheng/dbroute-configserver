package com.dbroute.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class FileUtil {

	private static Logger log = Logger.getLogger(FileUtil.class);

	public static final String SPLIT_STR = "@@@";

	public static boolean writeFile(String file_path, String content) {
		FileOutputStream outputStream = null;
		FileLock fileLock = null;
		FileChannel channel = null;
		try {

			String folder_path = file_path.substring(0, file_path.lastIndexOf(File.separator));
			File file = new File(folder_path);
			mkDir(file);
			File config_file = new File(file_path);
			if (config_file.exists()) {
				SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
				String file_name = df.format(new Date());
				String bat_file_name = folder_path + File.separator + file_name + ".php";
				config_file.renameTo(new File(bat_file_name));
			}
			outputStream = new FileOutputStream(config_file);
			channel = outputStream.getChannel();
			fileLock = channel.lock();
			outputStream.write(content.getBytes());
			outputStream.flush();
		} catch (Exception e) {
			log.error(e.getMessage());
		} finally {
			if (fileLock != null) {
				try {
					fileLock.release();
					fileLock = null;
				} catch (IOException e) {
					log.error(e.getMessage());
				}
			}
			if (channel != null) {
				try {
					channel.close();
					channel = null;
				} catch (IOException e) {
					log.error(e.getMessage());
				}
			}
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					log.error(e.getMessage());
				}
			}
		}
		return true;
	}

	public static String listToString(List<String> list, byte[] bytes, String db_config_name) {

		StringBuilder stringBuilder = new StringBuilder();
		Map<String, String> masterMap = new HashMap<String, String>();
		Map<String, String> slaveMap = new LinkedHashMap<String, String>();

		if (!list.isEmpty()) {
			for (String str : list) {
				if (str.startsWith("master" + SPLIT_STR)) {
					String[] array = str.split(SPLIT_STR);
					String db_name = array[1];
					String ip = array[2];
					if (masterMap.get(db_name) != null) {
						masterMap.put(db_name, masterMap.get(db_name) + "," + ip);
					} else {
						masterMap.put(db_name, ip);
					}
				} else if (str.startsWith("slave" + SPLIT_STR)) {
					String[] array = str.split(SPLIT_STR);
					String db_name = array[1];
					String ip = array[2];
					if (slaveMap.get(db_name) != null) {
						slaveMap.put(db_name, slaveMap.get(db_name) + "," + ip);
					} else {
						slaveMap.put(db_name, ip);
					}
				}
			}
		}
		if (bytes != null) {
			stringBuilder.append(new String(bytes));
			stringBuilder.append("\n");
		}

		if (db_config_name != null && db_config_name != "") {
			if (!masterMap.isEmpty()) {
				String master_dbs = "$master_" + db_config_name + "_dbs";
				stringBuilder.append(master_dbs + " = array();\n");
				for (Map.Entry<String, String> master : masterMap.entrySet()) {
					stringBuilder.append(master_dbs);
					stringBuilder.append("['");
					stringBuilder.append(master.getKey());
					stringBuilder.append("']");
					stringBuilder.append("='");
					stringBuilder.append(master.getValue());
					stringBuilder.append("';\n");
				}
				stringBuilder.append("$" + db_config_name + "['db_host']=" + master_dbs + ";\n\n");
			}
			if (!slaveMap.isEmpty()) {
				String slave_dbs = "$slave_" + db_config_name + "_dbs";
				stringBuilder.append(slave_dbs + " = array();\n");
				for (Map.Entry<String, String> slave : slaveMap.entrySet()) {
					stringBuilder.append(slave_dbs);
					stringBuilder.append("['");
					stringBuilder.append(slave.getKey());
					stringBuilder.append("']");
					stringBuilder.append("='");
					stringBuilder.append(slave.getValue());
					stringBuilder.append("';\n");
				}
				stringBuilder.append("$" + db_config_name + "['read_db_hosts']=" + slave_dbs + ";\n");
			}
		}
		return stringBuilder.toString();

	}

	public static void mkDir(File file) {
		if (file.getParentFile().exists()) {
			file.mkdir();
		} else {
			mkDir(file.getParentFile());
			file.mkdir();
		}
	}

	public static void main(String args[]) {
		String content = "喏 嘉茂喏 ";
		writeFile("f:\\gen\\a.txt", content);

	}

}
