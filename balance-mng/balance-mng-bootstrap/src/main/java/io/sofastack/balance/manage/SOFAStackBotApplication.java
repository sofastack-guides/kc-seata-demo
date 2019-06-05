package io.sofastack.balance.manage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.context.annotation.ImportResource;

/**
 * @author xuanbei
 * @since 2019/6/3
 */
@ImportResource({"classpath*:META-INF/balance-mng/*.xml"})
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, JdbcTemplateAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class})
public class SOFAStackBotApplication {
    public static void main(String[] args) {
        SpringApplication.run(SOFAStackBotApplication.class, args);
    }
}
