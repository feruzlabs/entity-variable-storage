package com.evs.springboot.autoconfigure;

import com.evs.config.EVSConfig;
import com.evs.config.EVSFactory;
import com.evs.service.EntityInstanceService;
import com.evs.service.EntityService;
import com.evs.service.VariableService;
import com.evs.springboot.properties.EVSProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@ConditionalOnClass(EVSFactory.class)
@ConditionalOnProperty(prefix = "evs", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(EVSProperties.class)
public class EVSAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(EVSConfig.class)
    public EVSConfig evsConfig(EVSProperties properties) {
        EVSConfig config = new EVSConfig();
        config.setJdbcUrl(properties.getJdbcUrl());
        config.setUsername(properties.getUsername());
        config.setPassword(properties.getPassword());
        config.setMaximumPoolSize(properties.getMaximumPoolSize());
        config.setMinimumIdle(properties.getMinimumIdle());
        config.setAutoMigrate(properties.isAutoMigrate());
        return config;
    }

    @Bean
    @ConditionalOnMissingBean(EVSFactory.class)
    public EVSFactory evsFactory(EVSConfig config) {
        return new EVSFactory(config);
    }

    @Bean
    @ConditionalOnMissingBean(EntityService.class)
    public EntityService entityService(EVSFactory factory) {
        return factory.entityService();
    }

    @Bean
    @ConditionalOnMissingBean(EntityInstanceService.class)
    public EntityInstanceService entityInstanceService(EVSFactory factory) {
        return factory.entityInstanceService();
    }

    @Bean
    @ConditionalOnMissingBean(VariableService.class)
    public VariableService variableService(EVSFactory factory) {
        return factory.variableService();
    }
}
