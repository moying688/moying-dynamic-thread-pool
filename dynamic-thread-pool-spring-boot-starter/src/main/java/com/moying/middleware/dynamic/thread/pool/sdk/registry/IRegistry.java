package com.moying.middleware.dynamic.thread.pool.sdk.registry;

import com.moying.middleware.dynamic.thread.pool.sdk.domain.model.entity.ThreadPoolConfigEntity;

import java.util.List;

/**
 * @Author: moying
 * @CreateTime: 2025-05-02
 * @Description: 注册中心接口
 * 注册中心，用于注册线程池配置信息，用于动态线程池的配置管理
 */

public interface IRegistry {

    /**
     * 上报线程池配置信息
     * @param threadPoolConfigEntityList 线程池配置信息
     */
    void reportThreadPool(List<ThreadPoolConfigEntity> threadPoolConfigEntityList);

    /**
     * 上报线程池配置参数
     * @param threadPoolConfigEntity 线程池配置参数
     */
    void reportThreadPoolConfigParameter(ThreadPoolConfigEntity threadPoolConfigEntity);
}
