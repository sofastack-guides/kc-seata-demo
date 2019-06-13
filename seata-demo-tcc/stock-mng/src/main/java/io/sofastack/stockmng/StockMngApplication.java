package io.sofastack.stockmng;

import com.zaxxer.hikari.HikariDataSource;
import io.seata.rm.datasource.DataSourceProxy;
import io.seata.spring.annotation.GlobalTransactionScanner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

@SpringBootApplication
@ImportResource("classpath*:spring/*.xml")
public class StockMngApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockMngApplication.class, args);
    }


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
            return new GlobalTransactionScanner("kc-stock-mng", "my_test_tx_group");
        }
    }

}
