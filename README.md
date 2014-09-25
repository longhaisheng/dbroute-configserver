dbroute-configserver
====================

dbroute动态配置管理

1、实现 php dbroute 框架配置项的动态管理,动态生成 php的config.php.<br>
2、动态实现加减 mysql slave数据库.<br>
3、动态加减mysql 双master数据库.<br>
4、实时监控mysql数据库是否宕机.<br>
5、数据库宕机，php应用自动识别，及时将宕机的数据库从配置文件中删除，切换毫秒级别.<br>
6、生成新的配置文件的同时，将备份老的php配置文件.<br>
7、监控数据库 开启 xml配置文件中的 MonitorAllDatabases类 配置.<br>
8、生成php配置文件 开启 xml配置文件中的 ZKManager 即可,(添加监控数据库配置后,可能要修改ZKManager在xml配置文件中的属性).<br>
9、监控多个数据库在resources文件夹下添加相应properties配置文件，并在all_dbs_prop_conf.properties添加新增的配置文件名.<br>
10、见意监控数据库和生成php配置文件分别打成两个war包,生成php配置文件的和php应用跑在同一个物理机或虚拟机上,监控数据库节点的可线上布署两至三台java应用.<br>
