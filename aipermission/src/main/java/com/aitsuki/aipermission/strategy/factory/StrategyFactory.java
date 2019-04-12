package com.aitsuki.aipermission.strategy.factory;

import com.aitsuki.aipermission.strategy.Strategy;

public interface StrategyFactory {

    /**
     * 返回默认的请求策略
     */
    Strategy getDefaultStrategy();

    /**
     * 根据请求的权限返回相应的策略
     */
    Strategy getStrategy(String[] permissions);
}
