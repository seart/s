/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.csp.sentinel.dashboard.rule.nacos.aop;

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.Constant;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.DynamicRuleStore;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.DynamicRuleStoreFactory;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.RuleType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author no one
 * @Description AOP 对 7种类型的规则进行增强，拦截 SentinelApiClient 类中调用的方法
 * 原始的处理方式是将数据持久化到内存中（保持不变），现在增加了 推数据和拉数据 通过增强向 nacos 进行推/拉
 * @Date 2024-08-02 09:14
 */
@Aspect
public class SentinelApiClientAspect {

    private static final Logger LOG = LoggerFactory.getLogger(SentinelApiClientAspect.class);

    @SuppressWarnings("PMD.ThreadPoolCreationRule")
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(
            new NamedThreadFactory("sentinel-dashboard-aspect"));

    @Resource
    private DynamicRuleStoreFactory factory;

    private Object fetchRules(ProceedingJoinPoint pjp, RuleType ruleType) throws Throwable {
        DynamicRuleStore<?> dynamicRuleStore = factory.getDynamicRuleStoreByType(ruleType);
        // 直接从nacos 拉去数据
        Object[] args = pjp.getArgs();
        String app = (String) args[0];
        String ip = (String) args[1];
        Integer port = (Integer) args[2];
        List rules = dynamicRuleStore.getRules(app);
        LOG.info("rules----------------------:{}", rules);
        if (CollectionUtils.isEmpty(rules)) {
            // 因为nacos 没找到数据，执行原来的操作，以防止返回类型不兼容报错
            return pjp.proceed();
        }
        if (StringUtils.equalsIgnoreCase(ruleType.getName(), Constant.PARAM_FLOW) || StringUtils.equalsIgnoreCase(ruleType.getName(), Constant.GW_API_GROUP) || StringUtils.equalsIgnoreCase(ruleType.getName(), Constant.GW_FLOW)) {
            return CompletableFuture.completedFuture(rules);
        }
        return rules;
    }


    @SuppressWarnings("unchecked")
    private Object publishRules(ProceedingJoinPoint pjp, RuleType ruleType) throws Throwable {
        DynamicRuleStore<RuleEntity> dynamicRuleStore = factory.getDynamicRuleStoreByType(ruleType);
        Object[] args = pjp.getArgs();
        String app = (String) args[0];
        List<RuleEntity> rules = (List<RuleEntity>) args[3];
        Object orginResult;
        try {
            orginResult = pjp.proceed();
        } catch (Exception e) {
            LOG.error("publishRules rules error ", e);
            throw new RuntimeException(e);
        }
        dynamicRuleStore.publish(app, rules);
        return orginResult;
    }


    /**
     * 拉取流控规则配置
     */
    @Around("fetchFlowRuleOfMachinePointcut()")
    public Object fetchFlowRuleOfMachine(final ProceedingJoinPoint pjp) throws Throwable {
        return fetchRules(pjp, RuleType.FLOW);
    }

    /**
     * 推送流控规则配置
     */
    @SuppressWarnings("unchecked")
    @Around("setFlowRuleOfMachineAsyncPointcut()")
    public Object setFlowRuleOfMachineAsync(final ProceedingJoinPoint pjp) throws Throwable {
        return publishRules(pjp, RuleType.FLOW);
    }


    /**
     * 拉取网关流控规则配置
     */
    @Around("fetchGatewayFlowRulesPointcut()")
    public Object fetchGatewayFlowRules(final ProceedingJoinPoint pjp) throws Throwable {
        return fetchRules(pjp, RuleType.GW_FLOW);
    }

    /**
     * 推送网关流控规则配置
     */
    @Around("modifyGatewayFlowRulesPointcut()")
    public Object modifyGatewayFlowRules(final ProceedingJoinPoint pjp) throws Throwable {
        return publishRules(pjp, RuleType.GW_FLOW);
    }


    /**
     * 拉取 api 分组规则配置
     */
    @Around("fetchApisPointcut()")
    public Object fetchApis(final ProceedingJoinPoint pjp) throws Throwable {
        return fetchRules(pjp, RuleType.GW_API_GROUP);
    }

    /**
     * 推送 api 分组规则配置
     */
    @Around("modifyApisPointcut()")
    public Object modifyApis(final ProceedingJoinPoint pjp) throws Throwable {
        return publishRules(pjp, RuleType.GW_API_GROUP);
    }


    /**
     * 拉取熔断规则配置
     */
    @Around("fetchDegradeRuleOfMachinePointcut()")
    public Object fetchDegradeRuleOfMachine(final ProceedingJoinPoint pjp) throws Throwable {
        return fetchRules(pjp, RuleType.DEGRADE);
    }

    /**
     * 推送熔断规则配置
     */
    @Around("setDegradeRuleOfMachinePointcut()")
    public Object setDegradeRuleOfMachine(final ProceedingJoinPoint pjp) throws Throwable {
        return publishRules(pjp, RuleType.DEGRADE);
    }


    /**
     * 拉取热点规则配置
     */
    @Around("fetchParamFlowRulesOfMachinePointcut()")
    public Object fetchParamFlowRulesOfMachine(final ProceedingJoinPoint pjp) throws Throwable {
        return fetchRules(pjp, RuleType.PARAM_FLOW);
    }

    /**
     * 推送热点规则配置
     */
    @Around("setParamFlowRuleOfMachinePointcut()")
    public Object setParamFlowRuleOfMachine(final ProceedingJoinPoint pjp) throws Throwable {
        return publishRules(pjp, RuleType.PARAM_FLOW);
    }


    /**
     * 拉取系统规则配置
     */
    @Around("fetchSystemRuleOfMachinePointcut()")
    public Object fetchSystemRuleOfMachine(final ProceedingJoinPoint pjp) throws Throwable {
        return fetchRules(pjp, RuleType.SYSTEM);
    }

    /**
     * 推送系统规则配置
     */
    @Around("setSystemRuleOfMachinePointcut()")
    public Object setSystemRuleOfMachine(final ProceedingJoinPoint pjp) throws Throwable {
        return publishRules(pjp, RuleType.SYSTEM);
    }


    /**
     * 拉取授权规则规则配置
     */
    @Around("fetchAuthorityRulesOfMachinePointcut()")
    public Object fetchAuthorityRulesOfMachine(final ProceedingJoinPoint pjp) throws Throwable {
        return fetchRules(pjp, RuleType.AUTHORITY);
    }

    /**
     * 推送授权规则配置
     */
    @Around("setAuthorityRuleOfMachinePointcut()")
    public Object setAuthorityRuleOfMachine(final ProceedingJoinPoint pjp) throws Throwable {
        return publishRules(pjp, RuleType.AUTHORITY);
    }


    @Pointcut("execution(public * com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient.fetchFlowRuleOfMachine(..))")
    public void fetchFlowRuleOfMachinePointcut() {
    }

    @Pointcut("execution(public * com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient.setFlowRuleOfMachineAsync(..))")
    public void setFlowRuleOfMachineAsyncPointcut() {
    }

    @Pointcut("execution(public * com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient.fetchGatewayFlowRules(..))")
    public void fetchGatewayFlowRulesPointcut() {
    }

    @Pointcut("execution(public * com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient.modifyGatewayFlowRules(..))")
    public void modifyGatewayFlowRulesPointcut() {
    }

    @Pointcut("execution(public * com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient.fetchApis(..))")
    public void fetchApisPointcut() {
    }

    @Pointcut("execution(public * com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient.modifyApis(..))")
    public void modifyApisPointcut() {
    }

    @Pointcut("execution(public * com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient.fetchDegradeRuleOfMachine(..))")
    public void fetchDegradeRuleOfMachinePointcut() {
    }

    @Pointcut("execution(public * com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient.setDegradeRuleOfMachine(..))")
    public void setDegradeRuleOfMachinePointcut() {
    }

    @Pointcut("execution(public * com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient.fetchParamFlowRulesOfMachine(..))")
    public void fetchParamFlowRulesOfMachinePointcut() {
    }

    @Pointcut("execution(public * com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient.setParamFlowRuleOfMachine(..))")
    public void setParamFlowRuleOfMachinePointcut() {
    }

    @Pointcut("execution(public * com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient.fetchSystemRuleOfMachine(..))")
    public void fetchSystemRuleOfMachinePointcut() {
    }

    @Pointcut("execution(public * com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient.setSystemRuleOfMachine(..))")
    public void setSystemRuleOfMachinePointcut() {
    }

    @Pointcut("execution(public * com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient.fetchAuthorityRulesOfMachine(..))")
    public void fetchAuthorityRulesOfMachinePointcut() {
    }

    @Pointcut("execution(public * com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient.setAuthorityRuleOfMachine(..))")
    public void setAuthorityRuleOfMachinePointcut() {
    }
}
