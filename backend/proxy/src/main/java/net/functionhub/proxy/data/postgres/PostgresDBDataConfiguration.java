package net.functionhub.proxy.data.postgres;


import jakarta.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import net.functionhub.proxy.props.PostgresDataSourceProps;
import net.functionhub.proxy.props.PropConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author Bizuwork Melesse
 * created on 8/29/23
 *
 */
@Configuration
@ComponentScan
@EntityScan
@EnableJpaRepositories(
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager",
        basePackages = { "net.functionhub.proxy.data.postgres.repo" }
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
        em.setPackagesToScan(new String[]{"net.functionhub.api.data.postgres.entity"});
        em.setPersistenceUnitName("functionHubDb");

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
