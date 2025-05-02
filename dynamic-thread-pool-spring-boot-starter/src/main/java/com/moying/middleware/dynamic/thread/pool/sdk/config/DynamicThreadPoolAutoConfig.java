package com.moying.middleware.dynamic.thread.pool.sdk.config;


import com.alibaba.fastjson.JSON;
import com.moying.middleware.dynamic.thread.pool.sdk.domain.DynamicThreadPoolService;
import com.moying.middleware.dynamic.thread.pool.sdk.domain.IDynamicThreadPoolService;
import com.moying.middleware.dynamic.thread.pool.sdk.domain.model.entity.ThreadPoolConfigEntity;
import com.moying.middleware.dynamic.thread.pool.sdk.domain.model.valobj.RegistryEnumVO;
import com.moying.middleware.dynamic.thread.pool.sdk.registry.IRegistry;
import com.moying.middleware.dynamic.thread.pool.sdk.registry.redis.RedisRegistry;
import com.moying.middleware.dynamic.thread.pool.sdk.trigger.job.ThreadPoolDataReportJob;
import com.moying.middleware.dynamic.thread.pool.sdk.trigger.listener.ThreadPoolConfigAdjustListener;
import org.apache.commons.lang.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 动态配置入口
 * @author moying
 */

@Configuration
@EnableScheduling
@EnableConfigurationProperties(DynamicThreadPoolAutoProperties.class) // 开启配置文件注入
public class DynamicThreadPoolAutoConfig {


    private final Logger logger = LoggerFactory.getLogger(DynamicThreadPoolAutoConfig.class);



    private  String applicationName;

    @Bean("redissonClient")
    public RedissonClient redissonClient(DynamicThreadPoolAutoProperties properties){
        Config config = new Config();
        config.setCodec(JsonJacksonCodec.INSTANCE);
        config.useSingleServer()
                .setAddress("redis://" + properties.getHost() + ":" + properties.getPort())
                .setPassword(properties.getPassword()) // 设置密码
                .setConnectionPoolSize(properties.getPoolSize()) // 设置连接池大小
                .setConnectionMinimumIdleSize(properties.getMinIdleSize()) // 设置最小空闲连接数
                .setIdleConnectionTimeout(properties.getIdleTimeout()) // 设置空闲连接超时时间
                .setConnectTimeout(properties.getConnectTimeout()) // 设置连接超时时间
                .setRetryAttempts(properties.getRetryAttempts()) // 设置重试次数
                .setRetryInterval(properties.getRetryInterval()) // 设置重试间隔时间
                .setPingConnectionInterval(properties.getPingInterval()) // 设置心跳检测时间
                .setKeepAlive(properties.isKeepAlive()); // 设置保持连接状态

        RedissonClient redissonClient = Redisson.create(config);

        logger.info("动态线程池，注册器（redis）链接初始化完成。{} {} {}", properties.getHost(), properties.getPoolSize(), !redissonClient.isShutdown());

        return redissonClient;
    }

    @Bean
    public IRegistry redisRegistry(RedissonClient redissonClient){
        return new RedisRegistry(redissonClient);
    }

    @Bean
    public ThreadPoolDataReportJob threadPoolDataReportJob(IDynamicThreadPoolService dynamicThreadPoolService, IRegistry registry){
        return new ThreadPoolDataReportJob(dynamicThreadPoolService, registry);
    }

    @Bean("dynamicThreadPoolService")
    public DynamicThreadPoolService dynamicThreadPoolService(ApplicationContext applicationContext,
                                                             Map<String,ThreadPoolExecutor> threadPoolExecutorMap,
                                                             RedissonClient redissonClient){
         applicationName = applicationContext.getEnvironment().getProperty("spring.application.name");

        if(StringUtils.isBlank(applicationName)){
            applicationName = "default";
            logger.warn("动态线程池，spring.application.name 未配置，使用默认值：{}", applicationName);
        }

        // 获取缓存数据,设置本地线程池配置
        Set<String> threadPoolKeys = threadPoolExecutorMap.keySet();
        for (String threadPoolKey : threadPoolKeys) {
            ThreadPoolConfigEntity threadPoolConfigEntity = redissonClient.<ThreadPoolConfigEntity>getBucket(
                    RegistryEnumVO.THREAD_POOL_CONFIG_PARAMETER_LIST_KEY.getKey() + "_" + applicationName + "_" + threadPoolKey).get();
            if (null == threadPoolConfigEntity) continue;
            ThreadPoolExecutor executor = threadPoolExecutorMap.get(threadPoolKey);
            executor.setCorePoolSize(threadPoolConfigEntity.getCorePoolSize());
            executor.setMaximumPoolSize(threadPoolConfigEntity.getMaximumPoolSize());
            logger.info("动态线程池，初始化线程池配置：{}", JSON.toJSONString(threadPoolConfigEntity));
        }


        return new DynamicThreadPoolService(applicationName, threadPoolExecutorMap);
    }

    @Bean
    public ThreadPoolConfigAdjustListener threadPoolConfigAdjustListener(IDynamicThreadPoolService dynamicThreadPoolService, IRegistry registry){
        return new ThreadPoolConfigAdjustListener(dynamicThreadPoolService, registry);
    }
    @Bean(name = "dynamicThreadPoolRedisTopic")
    public RTopic threadPoolConfigAdjustListener(RedissonClient redissonClient,ThreadPoolConfigAdjustListener threadPoolConfigAdjustListener){
        RTopic topic = redissonClient.getTopic(RegistryEnumVO.DYNAMIC_THREAD_POOL_REDIS_TOPIC.getKey() + "_" + applicationName);
        topic.addListener(ThreadPoolConfigEntity.class,threadPoolConfigAdjustListener);
        return topic;
    }

}
