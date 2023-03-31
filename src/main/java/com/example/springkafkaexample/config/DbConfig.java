package com.example.springkafkaexample.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.example.springkafkaexample.repository",
        entityManagerFactoryRef = "dbEntityManager",
        transactionManagerRef = "dbTransactionManager"
)
public class DbConfig {

    @Autowired
    private Environment env;

    public DbConfig() {
        super();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean dbEntityManager() {
        final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dbDataSource());
        em.setPackagesToScan("com.example.springkafkaexample.domain");
        em.setPersistenceUnitName("cbmReportPerm");

        final HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        final HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "none");
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Bean
    @ConfigurationProperties(prefix = "db.datasource")
    public DataSource dbDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class).build();
    }

    @Bean
    public PlatformTransactionManager dbTransactionManager() {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(dbEntityManager().getObject());
        return transactionManager;
    }

}
