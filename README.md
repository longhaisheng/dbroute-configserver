dbroute-configserver
====================

dbroute动态配置管理

1、实现 php dbroute 框架配置项的动态管理,动态生成 php的config.php.<br>
2、动态实现加减 mysql slave数据库.<br>
3、动态加减mysql 双master数据库.<br>
4、实时监控mysql数据库是否宕机.<br>
5、数据库宕机，php应用自动识别，及时将宕机的数据库从配置文件中删除，切换毫秒级别.<br>
6、生成新的配置文件的同时，将备份老的php配置文件.<br>
