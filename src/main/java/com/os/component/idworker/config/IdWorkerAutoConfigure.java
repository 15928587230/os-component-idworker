package com.os.component.idworker.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.os.component.idworker.dao.SegmentDao;
import com.os.component.idworker.dao.SegmentMapper;
import com.os.component.idworker.service.IdGen;
import com.os.component.idworker.service.SegmentService;
import com.os.component.idworker.service.impl.SegmentIdGenImpl;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

import java.sql.SQLException;

/**
 * ID生成器配置
 *
 * @author pengjunjie
 */
@ConditionalOnProperty(
        prefix = "component.idworker",
        name = "enabled",
        havingValue = "true"
)
@EnableConfigurationProperties(IdWorkerProperties.class)
public class IdWorkerAutoConfigure {
    private IdWorkerProperties properties;

    public IdWorkerAutoConfigure(IdWorkerProperties properties) {
        this.properties = properties;
    }

    @Bean
    @Order(1)
    @ConditionalOnMissingBean({IdGen.class})
    public IdGen idGen() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(properties.getJdbcUrl());
        dataSource.setUsername(properties.getUsername());
        dataSource.setPassword(properties.getPassword());
        try {
            dataSource.init();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("development", transactionFactory, dataSource);
        org.apache.ibatis.session.Configuration configuration =
                new org.apache.ibatis.session.Configuration(environment);
        configuration.addMapper(SegmentMapper.class);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
        SegmentDao segmentDao = new SegmentDao();
        segmentDao.setSqlSessionFactory(sqlSessionFactory);
        SegmentIdGenImpl idGen = new SegmentIdGenImpl();
        idGen.setSegmentDao(segmentDao);
        // init buffer
        idGen.init();
        return idGen;
    }

    @Bean
    @Order(2)
    @ConditionalOnMissingBean({SegmentService.class})
    public SegmentService segmentService() {
        return new SegmentService();
    }
}
