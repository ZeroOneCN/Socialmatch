package com.soical.server.config;

import com.soical.server.common.BusinessException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * 事务配置类
 */
@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
public class TransactionConfig {

    /**
     * 自定义事务管理器
     */
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        // 设置事务同步
        transactionManager.setNestedTransactionAllowed(true);
        transactionManager.setRollbackOnCommitFailure(true);
        transactionManager.setGlobalRollbackOnParticipationFailure(false);
        return transactionManager;
    }
} 