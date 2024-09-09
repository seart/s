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
package com.alibaba.csp.sentinel.dashboard.discovery;

import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.config.rule.NacosRuleConfiguration;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.Constant;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.DynamicRuleStore;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.DynamicRuleStoreFactory;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.RuleType;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author leyou
 */
@Component
public class SimpleMachineDiscovery implements MachineDiscovery {

    private final Logger logger = LoggerFactory.getLogger(SimpleMachineDiscovery.class);

    @Autowired
    private SentinelApiClient sentinelApiClient;

    @Autowired
    private DynamicRuleStoreFactory factory;

    @Autowired
    private ApplicationContext context;


    private final ConcurrentMap<String, AppInfo> apps = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Integer> customPullData = new ConcurrentHashMap<>();
    private final AtomicInteger defaultDataSource = new AtomicInteger();

    private synchronized void initData(MachineInfo machineInfo) {
        String app = machineInfo.getApp();
        String ip = machineInfo.getIp();
        Integer port = machineInfo.getPort();
        String key = String.format("%s-%s-%s", app, ip, port);
        Integer exists = customPullData.get(key);
        if (Objects.isNull(exists)) {
            logger.info("app :{} first into ip :{}, port:{}", app, ip, port);
            // 新的应用接入，需要进行初始化
            customPullData.putIfAbsent(key, 1);
            Map<String, String> paramMap = Maps.newHashMap();
            paramMap.put("app", app);
            paramMap.put("ip", ip);
            paramMap.put("port", String.valueOf(port));

            // 如果属性不存在，则返回默认值
            String appTypeWithDefault = System.getProperty("csp.sentinel.app.type", "0");
            logger.info("sentinel app type:{}", appTypeWithDefault);
            Arrays.stream(RuleType.values())
                    .forEach(x -> {
                        DynamicRuleStore<?> dynamicRuleStore = factory.getDynamicRuleStoreByType(x);
                        List rules;
                        boolean gatewaySign = StringUtils.equalsIgnoreCase(appTypeWithDefault, "1");
                        try {
                            rules = dynamicRuleStore.getRules(app);
                        } catch (Exception e) {
                            logger.error("get nacos rules error ", e);
                            throw new RuntimeException(e);
                        }
                        if (CollectionUtils.isEmpty(rules)) {
                            return;
                        }
                        boolean result = StringUtils.equalsIgnoreCase(x.getName(), Constant.FLOW) || StringUtils.equalsIgnoreCase(x.getName(), Constant.DEGRADE) ||
                                StringUtils.equalsIgnoreCase(x.getName(), Constant.AUTHORITY) || StringUtils.equalsIgnoreCase(x.getName(), Constant.SYSTEM);
                        if (result) {
                            sentinelApiClient.setRules(app, ip, port, x.getName(), rules);
                        } else if (StringUtils.equalsIgnoreCase(Constant.PARAM_FLOW, x.getName())) {
                            sentinelApiClient.customSetParamFlowRuleOfMachine(app, ip, port, rules);
                        } else if (StringUtils.equalsIgnoreCase(Constant.GW_FLOW, x.getName()) && gatewaySign) {
                            sentinelApiClient.customModifyGatewayFlowRules(app, ip, port, rules);
                        } else if (StringUtils.equalsIgnoreCase(Constant.GW_API_GROUP, x.getName()) && gatewaySign) {
                            sentinelApiClient.customModifyApis(app, ip, port, rules);
                        }
                    });
        }
    }

    public synchronized void customeRemoveMachine(String app, String ip, int port) {
        String key = String.format("%s-%s-%s", app, ip, port);
        customPullData.remove(key);
    }


    @Override
    public long addMachine(MachineInfo machineInfo) {
        AssertUtil.notNull(machineInfo, "machineInfo cannot be null");
        AppInfo appInfo = apps.computeIfAbsent(machineInfo.getApp(), o -> new AppInfo(machineInfo.getApp(), machineInfo.getAppType()));
        appInfo.addMachine(machineInfo);

        readDataFromDataSource(machineInfo);
        return 1;
    }

    /**
     * @Author no one
     * @Description 如果用的是nacos 外部数据源，需要读取原始数据,否则就不需要执行
     * @Date 2024-09-09 17:37
     * @param: machineInfo
     * @param: nacosConfig
     * @return: void
     */
    private void readDataFromDataSource(MachineInfo machineInfo) {
        if (defaultDataSource.get() == Constant.DatasourceSign.initSign) {
            try {
                // 这个类中存放了生成AOP对象类
                context.getBean(NacosRuleConfiguration.class);
                defaultDataSource.set(Constant.DatasourceSign.nacosSign);
            } catch (Exception e) {
                // if not use nacos config,Ignore
                logger.error(" the data store use default ");
                defaultDataSource.set(Constant.DatasourceSign.defaultSign);
            }
        }
        if (defaultDataSource.get() == Constant.DatasourceSign.nacosSign) {
            initData(machineInfo);
        }
    }

    @Override
    public boolean removeMachine(String app, String ip, int port) {
        AssertUtil.assertNotBlank(app, "app name cannot be blank");
        AppInfo appInfo = apps.get(app);
        if (appInfo != null) {
            customeRemoveMachine(app, ip, port);
            return appInfo.removeMachine(ip, port);
        }
        return false;
    }

    @Override
    public List<String> getAppNames() {
        return new ArrayList<>(apps.keySet());
    }

    @Override
    public AppInfo getDetailApp(String app) {
        AssertUtil.assertNotBlank(app, "app name cannot be blank");
        return apps.get(app);
    }

    @Override
    public Set<AppInfo> getBriefApps() {
        return new HashSet<>(apps.values());
    }

    @Override
    public void removeApp(String app) {
        AssertUtil.assertNotBlank(app, "app name cannot be blank");
        apps.remove(app);
    }

}
