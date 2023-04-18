package com.negling.camunda.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class DatasourceConfig {
    @Primary
    @Bean("camundaDataSourceProperties")
    @ConfigurationProperties("camunda.datasource")
    public DataSourceProperties camundaDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("camundaBpmDataSource")
    @ConfigurationProperties("camunda.datasource.hikari")
    public DataSource camundaBpmDataSource() {
        return camundaDataSourceProperties().initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Primary
    @Bean("camundaBpmTransactionManager")
    public PlatformTransactionManager camundaTransactionManager() {
        return new DataSourceTransactionManager(camundaBpmDataSource());
    }

}
