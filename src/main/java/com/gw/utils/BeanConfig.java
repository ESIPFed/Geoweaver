package com.gw.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import com.gw.database.HostRepository;
import com.gw.server.Java2JupyterClientEndpoint;

@Configuration
// @EnableJpaRepositories
// @EnableTransactionManagement
public class BeanConfig {

//    @Bean(name = "Java2JupyterClientEndpoint")
//    @Scope("prototype")
//    public Java2JupyterClientEndpoint createPrototype() {
//        return new Java2JupyterClientEndpoint();
//    }
    // @Bean
    // public DataSource dataSource() {

    //     EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
    //     return builder.setType(EmbeddedDatabaseType.H2).build();
    // }

    // @Bean
    // public EntityManagerFactory entityManagerFactory() {

    // HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
    // vendorAdapter.setGenerateDdl(true);

    // LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
    // factory.setJpaVendorAdapter(vendorAdapter);
    // factory.setPackagesToScan("com.acme.domain");
    // factory.setDataSource(dataSource());
    // factory.afterPropertiesSet();

    // return factory.getObject();
    // }

    // @Bean
    // public PlatformTransactionManager transactionManager() {

    //     JpaTransactionManager txManager = new JpaTransactionManager();
    //     txManager.setEntityManagerFactory(entityManagerFactory());
    //     return txManager;
    // }
	
}
