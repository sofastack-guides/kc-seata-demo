# kc-seata-demo
SEATA Demo for SOFAStack Cloud Native Workshop on KubeCon China 2019

## AT 模式
#### 1、引入maven依赖

将下面的依赖引入到父工程的pom文件中（kc-seata-demo/pom.xml）:
```xml
...
<properties>
    ...
    <seata.version>0.6.1</seata.version>
    <netty4.version>4.1.24.Final</netty4.version>
</properties>
...
<dependencyManagement>
    <dependencies>
        ...

        <dependency>
            <groupId>io.seata</groupId>
            <artifactId>seata-all</artifactId>
            <version>${seata.version}</version>
        </dependency>

        <dependency>
            <groupId>io.seata</groupId>
            <artifactId>seata-server</artifactId>
            <version>${seata.version}</version>
            <exclusions>
                <exclusion>
                    <groupId>javax.servlet</groupId>
                    <artifactId>servlet-api</artifactId>
                </exclusion>
            </exclusions>
        </dependency>

        <dependency>
            <groupId>io.netty</groupId>
            <artifactId>netty-all</artifactId>
            <version>${netty4.version}</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

将下面的依赖引入到 stock-mng 工程的pom文件中（kc-seata-demo/stock-mng/pom.xml）:
```xml
<dependencies>
....
    <dependency>
        <groupId>io.seata</groupId>
        <artifactId>seata-all</artifactId>
    </dependency>

    <dependency>
        <groupId>io.netty</groupId>
        <artifactId>netty-all</artifactId>
    </dependency>
<dependencies>
```

将下面的依赖引入到 balance-mng-impl 工程的pom文件中（kc-seata-demo/balance-mng/balance-mng-impl/pom.xml）:
```xml
<dependencies>
....
    <dependency>
        <groupId>io.seata</groupId>
        <artifactId>seata-all</artifactId>
    </dependency>

    <dependency>
        <groupId>io.seata</groupId>
        <artifactId>seata-server</artifactId>
    </dependency>

    <dependency>
        <groupId>io.netty</groupId>
        <artifactId>netty-all</artifactId>
    </dependency>
<dependencies>
```

#### 2、使用Seata的DataSourceProxy代理实际的数据源，并配置GlobalTransactionScanner扫描@GlobalTransaction注解

将下面的java代码段加到 BalanceMngApplication 和 StockMngApplication 类的main方法下面:
```java
...
import io.seata.rm.datasource.DataSourceProxy;
import io.seata.spring.annotation.GlobalTransactionScanner;
...

@Configuration
public static class DataSourceConfig {

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public DataSource dataSource(DataSourceProperties properties) {
        HikariDataSource dataSource = createDataSource(properties, HikariDataSource.class);
        if (StringUtils.hasText(properties.getName())) {
            dataSource.setPoolName(properties.getName());
        }
        return new DataSourceProxy(dataSource);
    }

    @SuppressWarnings("unchecked")
    protected static <T> T createDataSource(DataSourceProperties properties,
                                            Class<? extends DataSource> type) {
        return (T) properties.initializeDataSourceBuilder().type(type).build();
    }

    @Bean
    @Primary
    public GlobalTransactionScanner globalTransactionScanner(){
        return new GlobalTransactionScanner("kc-balance-mng", "my_test_tx_group");
    }
}
```
注意上面的dataSource方法返回的是DataSourceProxy代理的数据源

#### 3、配置@GlobalTransactional注解使分布式事务生效:

在StockMngImpl类的purchase方法上加入@GlobalTransactional注解:
```java
...
import io.seata.spring.annotation.GlobalTransactional;
...

/**
 *
 * 购买商品
 */
@Override
@GlobalTransactional(timeoutMills = 300000, name = "kc-book-store-tx")
public void purchase(String userName, String productCode, int count) {
  ...
}
```

#### 4、配置Seata server:
简单起见，将Seata server和BalanceMngApplication一起启动，在BalanceMngApplication类中加入启动Seata server的代码:
```java
...
public static void main(String[] args) {

    startSeatServer();

    SpringApplication.run(BalanceMngApplication.class, args);
}

/**
 * The seata server.
 */
static Server server = null;

private static void startSeatServer(){

    new Thread(new Runnable() {

        public void run() {
            server = new Server();
            try {
                server.main(new String[] {"8091", StoreMode.FILE.name(), "127.0.0.1"});
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }).start();

}
...
```

演示的Seata server使用本地文件作为存储，将下面两个文件复制到balance-mng-bootstrap和stock-mng工程的/src/main/resources目录下:
  文件名：file.conf
  文件内容：  
```yaml
transport {
  # tcp udt unix-domain-socket
  type = "TCP"
  #NIO NATIVE
  server = "NIO"
  #enable heartbeat
  heartbeat = true
  #thread factory for netty
  thread-factory {
    boss-thread-prefix = "NettyBoss"
    worker-thread-prefix = "NettyServerNIOWorker"
    server-executor-thread-prefix = "NettyServerBizHandler"
    share-boss-worker = false
    client-selector-thread-prefix = "NettyClientSelector"
    client-selector-thread-size = 1
    client-worker-thread-prefix = "NettyClientWorkerThread"
    # netty boss thread size,will not be used for UDT
    boss-thread-size = 1
    #auto default pin or 8
    worker-thread-size = 8
  }
}
service {
  #vgroup->rgroup
  vgroup_mapping.my_test_tx_group = "default"
  #only support single node
  default.grouplist = "127.0.0.1:8091"
  #degrade current not support
  enableDegrade = false
  #disable
  disable = false
}
client {
  async.commit.buffer.limit = 10000
  lock {
    retry.internal = 10
    retry.times = 30
  }
}

## transaction log store
store {
  ## store mode: file、db
  mode = "file"

  ## file store
  file {
    dir = "file_store/seata"
  }
}
```

  文件名：registry.conf
  文件内容：
```yaml
registry {
  # file 、nacos 、eureka、redis、zk
  type = "file"

  nacos {
    serverAddr = "localhost"
    namespace = "public"
    cluster = "default"
  }
  eureka {
    serviceUrl = "http://localhost:1001/eureka"
    application = "default"
    weight = "1"
  }
  redis {
    serverAddr = "localhost:6379"
    db = "0"
  }
  zk {
    cluster = "default"
    serverAddr = "127.0.0.1:2181"
    session.timeout = 6000
    connect.timeout = 2000
  }
  file {
    name = "file.conf"
  }
}

config {
  # file、nacos 、apollo、zk
  type = "file"

  nacos {
    serverAddr = "localhost"
    namespace = "public"
    cluster = "default"
  }
  apollo {
    app.id = "fescar-server"
    apollo.meta = "http://192.168.1.204:8801"
  }
  zk {
    serverAddr = "127.0.0.1:2181"
    session.timeout = 6000
    connect.timeout = 2000
  }
  file {
    name = "file.conf"
  }
}
```

#### 5、创建undo_log表:
在balance_db和stock_db两个数据库中都创建undo_log表:
```sql
CREATE TABLE `undo_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch_id` bigint(20) NOT NULL,
  `xid` varchar(100) NOT NULL,
  `rollback_info` longblob NOT NULL,
  `log_status` int(11) NOT NULL,
  `log_created` datetime NOT NULL,
  `log_modified` datetime NOT NULL,
  `ext` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
```

#### 6、启动Seata server和stock-mng、balance-mng应用:
1. 运行BalanceMngApplication类的main方法(包含启动Seata server)
2. 运行StockMngApplication类的main方法
3. 浏览器打开 http://127.0.0.1:8080/

