package com.moying.middleware.dynamic.thread.pool.sdk.trigger.job;

import com.alibaba.fastjson.JSON;
import com.moying.middleware.dynamic.thread.pool.sdk.domain.IDynamicThreadPoolService;
import com.moying.middleware.dynamic.thread.pool.sdk.domain.model.entity.ThreadPoolConfigEntity;
import com.moying.middleware.dynamic.thread.pool.sdk.registry.IRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

/**
 * @Author: moying
 * @CreateTime: 2025-05-02
 * @Description: 线程池数据上报定时任务
 * 定时任务，用于定时上报线程池配置信息，用于动态线程池的配置管理
 */

public class ThreadPoolDataReportJob {

    private final Logger logger  = LoggerFactory.getLogger(ThreadPoolDataReportJob.class);


    public final IDynamicThreadPoolService dynamicThreadPoolService;

    private  final IRegistry registry;

    public ThreadPoolDataReportJob(IDynamicThreadPoolService dynamicThreadPoolService, IRegistry registry) {
        this.dynamicThreadPoolService = dynamicThreadPoolService;
        this.registry = registry;
    }

    // 定时任务，每20秒执行一次
    @Scheduled(cron = "0/20 * * * * ?")
    public void execReportThreadPoolList(){
        List<ThreadPoolConfigEntity> threadPoolConfigEntities = dynamicThreadPoolService.queryThreadPoolList();
        registry.reportThreadPool(threadPoolConfigEntities);
        logger.info("动态线程池，上报线程池信息：{}", JSON.toJSONString(threadPoolConfigEntities));

        for (ThreadPoolConfigEntity threadPoolConfigEntity : threadPoolConfigEntities) {
            registry.reportThreadPoolConfigParameter(threadPoolConfigEntity);
            logger.info("动态线程池，上报线程池配置：{}", JSON.toJSONString(threadPoolConfigEntity));
        }
    }
}
