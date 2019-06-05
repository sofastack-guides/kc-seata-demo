package io.sofastack.stockmng;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.context.annotation.ImportResource;

@ImportResource({"classpath*:META-INF/stock-mng/*.xml"})
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, JdbcTemplateAutoConfiguration.class,
		DataSourceTransactionManagerAutoConfiguration.class})
public class StockMngApplication {

	public static void main(String[] args) {
		SpringApplication.run(StockMngApplication.class, args);
	}

}
