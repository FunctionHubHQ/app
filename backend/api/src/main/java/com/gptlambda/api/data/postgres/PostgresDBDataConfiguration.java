package com.gptlambda.api.data.postgres;


import com.gptlambda.api.props.PostgresDataSourceProps;
import com.gptlambda.api.props.PropConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Bizuwork Melesse
 * created on 2/13/21
 *
 */
@Configuration
@ComponentScan
@EntityScan
@EnableJpaRepositories(
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager",
        basePackages = { "com.gptlambda.api.data.postgres.repo" }
)
@EnableTransactionManagement
@Import(PropConfiguration.class)
@RequiredArgsConstructor
public class PostgresDBDataConfiguration {

    private final PostgresDataSourceProps dataSourceProps;

    @Primary
    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            Environment env, @Qualifier("postgresDbSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan(new String[]{"com.gptlambda.api.data.postgres.entity"});
        em.setPersistenceUnitName("gptlambdaDb");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        // JPA & Hibernate
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQL10Dialect");
        properties.put("hibernate.show-sql", true);
        int batchSize = 20;
        properties.put("hibernate.jdbc.batch_size", batchSize);
        properties.put("hibernate.order_inserts", true);
        properties.put("hibernate.order_updates", true);
        properties.put("hibernate.jdbc.batch_versioned_data", true);
        em.setJpaPropertyMap(properties);
        em.afterPropertiesSet();
        return em;
    }


    @Primary
    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Primary
    @Bean(name = "postgresDbSource")
    public DataSource postgresDbSource() {
        return DataSourceBuilder.create()
                .driverClassName(dataSourceProps.getDriverClassName())
                .url(dataSourceProps.getUrl())
                .username(dataSourceProps.getUsername())
                .password(dataSourceProps.getPassword())
                .build();
    }

}
