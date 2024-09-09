package com.alibaba.csp.sentinel.dashboard.config.rule;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.DefaultFlowRuleDynamicRuleStore;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.DynamicRuleStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "rule.store", name = "type", havingValue = "default", matchIfMissing = true)
public class DefaultRuleConfiguration {

    @Bean("flowRuleDynamicRuleStore")
    public DynamicRuleStore<FlowRuleEntity> flowRuleDynamicRuleStore() {
        return new DefaultFlowRuleDynamicRuleStore();
    }

}
