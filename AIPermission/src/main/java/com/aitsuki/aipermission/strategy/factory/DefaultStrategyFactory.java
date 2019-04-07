package com.aitsuki.aipermission.strategy.factory;

import com.aitsuki.aipermission.strategy.DefaultStrategy;
import com.aitsuki.aipermission.strategy.Strategy;

public class DefaultStrategyFactory implements StrategyFactory {

    @Override
    public Strategy getDefaultStrategy() {
        return new DefaultStrategy();
    }

    @Override
    public Strategy getStrategy(String[] permissions) {
        return new DefaultStrategy();
    }
}
