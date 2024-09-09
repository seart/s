package com.alibaba.csp.sentinel.dashboard.config.rule;

/**
 * @Author no one
 * @Description 用于以后扩展用
 * @Date 2024-08-02 09:25
 */
public enum RuleStoreType {

    DEFAULT(DefaultRuleConfiguration.class),
    NACOS(NacosRuleConfiguration.class);

    private final Class<?> configurationClass;

    RuleStoreType(final Class<?> configurationClass) {
        this.configurationClass = configurationClass;
    }

    public Class<?> getConfigurationClass() {
        return configurationClass;
    }
}
