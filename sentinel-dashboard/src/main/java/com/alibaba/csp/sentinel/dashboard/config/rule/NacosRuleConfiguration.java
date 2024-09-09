package com.alibaba.csp.sentinel.dashboard.config.rule;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.ApiDefinitionEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.*;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.DynamicRuleNacosStore;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.DynamicRuleStore;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosConfig;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.RuleType;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.aop.SentinelApiClientAspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConditionalOnProperty(prefix = "rule.store", name = "type", havingValue = "nacos")
@EnableConfigurationProperties(NacosPropertiesInject.class)
public class NacosRuleConfiguration {

    @Autowired
    private NacosPropertiesInject nacosPropertiesInject;

    @Bean
    public NacosConfig nacosConfigSev() throws Exception {
        return new NacosConfig(nacosPropertiesInject.getServerAddr(),
                nacosPropertiesInject.getNamespace(),
                nacosPropertiesInject.getGroup(),
                nacosPropertiesInject.getTimeout(),
                nacosPropertiesInject.getUsername(),
                nacosPropertiesInject.getPassword());
    }


    @Bean
    public DynamicRuleStore<FlowRuleEntity> flowRuleDynamicRuleStore(NacosConfig nacosConfigSev) {
        return new DynamicRuleNacosStore<>(
                RuleType.FLOW, nacosConfigSev, nacosPropertiesInject
        );
    }

    @Bean
    public DynamicRuleStore<DegradeRuleEntity> degradeRuleDynamicRuleStore(NacosConfig nacosConfigSev) {
        return new DynamicRuleNacosStore<>(
                RuleType.DEGRADE, nacosConfigSev, nacosPropertiesInject
        );
    }

    @Bean
    public DynamicRuleStore<ParamFlowRuleEntity> paramFlowRuleDynamicRuleStore(NacosConfig nacosConfigSev) {
        return new DynamicRuleNacosStore<>(
                RuleType.PARAM_FLOW, nacosConfigSev, nacosPropertiesInject
        );
    }

    @Bean
    public DynamicRuleStore<SystemRuleEntity> systemRuleDynamicRuleStore(NacosConfig nacosConfigSev) {
        return new DynamicRuleNacosStore<>(
                RuleType.SYSTEM, nacosConfigSev, nacosPropertiesInject
        );
    }

    @Bean
    public DynamicRuleStore<AuthorityRuleEntity> authorityRuleDynamicRuleStore(NacosConfig nacosConfigSev) {
        return new DynamicRuleNacosStore<>(
                RuleType.AUTHORITY, nacosConfigSev, nacosPropertiesInject
        );
    }

    @Bean
    public DynamicRuleStore<GatewayFlowRuleEntity> gatewayFlowRuleDynamicRuleStore(NacosConfig nacosConfigSev) {
        return new DynamicRuleNacosStore<>(
                RuleType.GW_FLOW, nacosConfigSev, nacosPropertiesInject
        );
    }

    @Bean
    public DynamicRuleStore<ApiDefinitionEntity> apiDefinitionDynamicRuleStore(NacosConfig nacosConfigSev) {
        return new DynamicRuleNacosStore<>(
                RuleType.GW_API_GROUP, nacosConfigSev, nacosPropertiesInject
        );
    }

    @Bean
    public SentinelApiClientAspect sentinelApiClientAspect() {
        return new SentinelApiClientAspect();
    }

}

