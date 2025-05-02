package com.moying.middleware.dynamic.thread.pool.sdk.domain;

import com.alibaba.fastjson.JSON;
import com.moying.middleware.dynamic.thread.pool.sdk.domain.model.entity.ThreadPoolConfigEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author: moying
 * @CreateTime: 2025-05-02
 * @Description: 动态线程池服务
 */

public class DynamicThreadPoolService implements IDynamicThreadPoolService{

    private final Logger logger = LoggerFactory.getLogger(DynamicThreadPoolService.class);


    private final String applicationName;
    private final Map<String, ThreadPoolExecutor> threadPoolExecutorMap;

    public DynamicThreadPoolService(String applicationName, Map<String, ThreadPoolExecutor> threadPoolExecutorMap) {
        this.applicationName = applicationName;
        this.threadPoolExecutorMap = threadPoolExecutorMap;
    }

    @Override
    public List<ThreadPoolConfigEntity> queryThreadPoolList() {
        Set<String> threadPoolBeanName = threadPoolExecutorMap.keySet();
        List<ThreadPoolConfigEntity> threadPoolVOS = new ArrayList<>(threadPoolBeanName.size());
        for (String beanName : threadPoolBeanName) {
            ThreadPoolExecutor executor = threadPoolExecutorMap.get(beanName);

            ThreadPoolConfigEntity threadPoolVO = buildThreadPoolVO(beanName, executor);
            threadPoolVOS.add(threadPoolVO);

            logger.info("动态线程池，查询线程池配置：{}", threadPoolVO);
        }

        return threadPoolVOS;
    }

    @Override
    public ThreadPoolConfigEntity queryThreadPoolConfigByName(String threadPoolName) {
        ThreadPoolExecutor executor = threadPoolExecutorMap.get(threadPoolName);
        if(null == executor){
            return new ThreadPoolConfigEntity(applicationName,threadPoolName);
        }
        // 线程池配置数据
        ThreadPoolConfigEntity threadPoolConfigVO = buildThreadPoolVO(threadPoolName, executor);

        if (logger.isDebugEnabled()) {
            logger.info("动态线程池，配置查询 应用名:{} 线程名:{} 池化配置:{}", applicationName, threadPoolName, JSON.toJSONString(threadPoolConfigVO));
        }

        return threadPoolConfigVO;
    }



    @Override
    public void updateThreadPoolConfig(ThreadPoolConfigEntity threadPoolConfigEntity) {
        if(null == threadPoolConfigEntity || !applicationName.equals(threadPoolConfigEntity.getAppName()))return ;
        ThreadPoolExecutor executor = threadPoolExecutorMap.get(threadPoolConfigEntity.getThreadPoolName());
        if(null == executor)return ;
        // 更新线程池配置
        executor.setCorePoolSize(threadPoolConfigEntity.getCorePoolSize());
        executor.setMaximumPoolSize(threadPoolConfigEntity.getMaximumPoolSize());
        logger.info("动态线程池，配置更新 应用名:{} 线程名:{} 池化配置:{}",
                applicationName, threadPoolConfigEntity.getThreadPoolName(), JSON.toJSONString(threadPoolConfigEntity));
    }

    private ThreadPoolConfigEntity buildThreadPoolVO(String threadPoolName, ThreadPoolExecutor executor) {
        ThreadPoolConfigEntity threadPoolVO = new ThreadPoolConfigEntity(applicationName,threadPoolName);
        threadPoolVO.setCorePoolSize(executor.getCorePoolSize());
        threadPoolVO.setMaximumPoolSize(executor.getMaximumPoolSize());
        threadPoolVO.setActiveCount(executor.getActiveCount());
        threadPoolVO.setPoolSize(executor.getPoolSize());
        threadPoolVO.setQueueType(executor.getQueue().getClass().getName());
        threadPoolVO.setQueueSize(executor.getQueue().size());
        threadPoolVO.setRemainingCapacity(executor.getQueue().remainingCapacity());
        return threadPoolVO;
    }
}
