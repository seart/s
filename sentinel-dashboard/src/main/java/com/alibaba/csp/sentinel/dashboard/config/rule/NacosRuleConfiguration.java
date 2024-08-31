package com.alibaba.csp.sentinel.dashboard.config.rule;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.ApiDefinitionEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.*;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.DynamicRuleNacosStore;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.DynamicRuleStore;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.RuleType;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.aop.SentinelApiClientAspect;
import com.alibaba.nacos.api.config.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConditionalOnProperty(prefix = "rule.store", name = "type", havingValue = "nacos")
@EnableConfigurationProperties(NacosPropertiesInject.class)
public class NacosRuleConfiguration {

    @Autowired
    @Qualifier("nacosConfigSev")
    private ConfigService nacosConfigSev;

    @Autowired
    private NacosPropertiesInject nacosPropertiesInject;


    @Bean
    public DynamicRuleStore<FlowRuleEntity> flowRuleDynamicRuleStore() {
        return new DynamicRuleNacosStore<>(
                RuleType.FLOW, nacosConfigSev, nacosPropertiesInject
        );
    }

    @Bean
    public DynamicRuleStore<DegradeRuleEntity> degradeRuleDynamicRuleStore() {
        return new DynamicRuleNacosStore<>(
                RuleType.DEGRADE, nacosConfigSev, nacosPropertiesInject
        );
    }

    @Bean
    public DynamicRuleStore<ParamFlowRuleEntity> paramFlowRuleDynamicRuleStore() {
        return new DynamicRuleNacosStore<>(
                RuleType.PARAM_FLOW, nacosConfigSev, nacosPropertiesInject
        );
    }

    @Bean
    public DynamicRuleStore<SystemRuleEntity> systemRuleDynamicRuleStore() {
        return new DynamicRuleNacosStore<>(
                RuleType.SYSTEM, nacosConfigSev, nacosPropertiesInject
        );
    }

    @Bean
    public DynamicRuleStore<AuthorityRuleEntity> authorityRuleDynamicRuleStore() {
        return new DynamicRuleNacosStore<>(
                RuleType.AUTHORITY, nacosConfigSev, nacosPropertiesInject
        );
    }

    @Bean
    public DynamicRuleStore<GatewayFlowRuleEntity> gatewayFlowRuleDynamicRuleStore() {
        return new DynamicRuleNacosStore<>(
                RuleType.GW_FLOW, nacosConfigSev, nacosPropertiesInject
        );
    }

    @Bean
    public DynamicRuleStore<ApiDefinitionEntity> apiDefinitionDynamicRuleStore() {
        return new DynamicRuleNacosStore<>(
                RuleType.GW_API_GROUP, nacosConfigSev, nacosPropertiesInject
        );
    }

    @Bean
    public SentinelApiClientAspect sentinelApiClientAspect() {
        return new SentinelApiClientAspect();
    }

}

