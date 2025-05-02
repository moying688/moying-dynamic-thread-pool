package com.moying.middleware.dynamic.thread.pool.sdk.domain;

import com.moying.middleware.dynamic.thread.pool.sdk.domain.model.entity.ThreadPoolConfigEntity;

import java.util.List;

/**
 * @Author: moying
 * @CreateTime: 2025-05-02
 * @Description: 动态线程池服务
 */

public interface IDynamicThreadPoolService {

    List<ThreadPoolConfigEntity> queryThreadPoolList();

    ThreadPoolConfigEntity queryThreadPoolConfigByName(String threadPoolName);

    void updateThreadPoolConfig(ThreadPoolConfigEntity threadPoolConfigEntity);


}
