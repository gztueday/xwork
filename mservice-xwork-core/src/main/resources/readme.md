bg-dis
云服务组对外服务
---------------------------------------------
#编译命令大全
格式：

编号) 项目名
编译命令
------------
1) logsearch
mvn clean package -DskipTests -Pp191 -pl "dis-logsearch/dis-logsearch-provider,dis-logsearch/dis-logsearch-web" -am

2)bi(官网)
mvn clean package -Pp8611 -DskipTests  -pl "dis-bi/dis-bi-com.banggood.dis.service,dis-bi/dis-bi-web" -am

2)bi(oa)
mvn clean package -Pp8613 -DskipTests  -pl "dis-bi/dis-bi-com.banggood.dis.service,dis-bi/dis-bi-web" -am

test-sql
//sql = "select sequence,email,name,password,disabled from pmc.`user` ";

3) dig-kylin
mvn clean package -DskipTests -Pp191 -pl "dis-kylin/dis-kylin-web" -am

4) activemq
mvn clean package -Pprod  -DskipTests  -pl "dis-activemq/dis-activemq-service-kylin" -am 
mvn clean package (-Ptest)  -DskipTests  -pl "dis-activemq/dis-activemq-service-kylin" -am 
指定主方法启动命令
java -cp dis-activemq-service-kylin-0.0.1-SNAPSHOT.jar com.banggood.dis.service.server.sync.Sync
nohup java -cp dis-activemq-service-kylin-0.0.1-SNAPSHOT.jar com.banggood.dis.service.server.sync.Sync  2>&1 >> /dev/null &
mvn clean test-compile  -pl "dis-activemq/dis-activemq-service-hive" -am 
mvn clean package -DskipTests -pl "dis-xwork/mservice-xwork-core" -am -X
(addWorkFlowInstanceByRemote)
