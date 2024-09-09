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

package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.config.rule.NacosPropertiesInject;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.aop.SentinelApiClientAspect;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.api.exception.NacosException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author no one
 * @Description 有2个实现，这是nacos 的实现类
 */
public class DynamicRuleNacosStore<T extends RuleEntity> extends DynamicRuleStore<T> {

    private static final Logger LOG = LoggerFactory.getLogger(SentinelApiClientAspect.class);


    private final NacosConfig nacosConfig;
    private final NacosPropertiesInject nacosPropertiesInject;

    public DynamicRuleNacosStore(final RuleType ruleType,
                                 final NacosConfig nacosConfig,
                                 final NacosPropertiesInject nacosProperties) {
        super.ruleType = ruleType;
        this.nacosConfig = nacosConfig;
        nacosPropertiesInject = nacosProperties;
    }

    /**
     * @Author no one
     * @Description 获取Naocs上面的 sentinel 规则,并且增加监听
     * @Date 2024-08-02 09:13
     * @param: appName
     * @return: java.util.List<T>
     **/
    @Override
    public List<T> getRules(final String appName) {
        String dataId = appName + NacosConfigUtil.PrefixType.getPrefix(ruleType);
        LOG.info("dataId:{}", dataId);
        String rules = null;
        try {
            rules = nacosConfig.get().getConfig(dataId,
                    nacosPropertiesInject.getGroup(),
                    nacosPropertiesInject.getTimeout());
        } catch (NacosException e) {
            LOG.error("get nacos config error ", e);
            throw new RuntimeException(e);
        }
        if (StringUtil.isEmpty(rules)) {
            return new ArrayList<>();
        }
        Converter<String, List<T>> decoder = RuleConfigUtil.getDecoder(ruleType.getClazz());
        List<T> rulesList = decoder.convert(rules);
        LOG.info("getRules: {}", JSON.toJSONString(rulesList));
        return rulesList;
    }

    /**
     * @Author no one
     * @Description 推送 sentinel 规则到Nacos上
     * @Date 2024-08-02 09:13
     * @param: app
     * @param: rules
     * @return: void
     **/
    @Override
    public void publish(final String app, final List<T> rules) throws Exception {
        AssertUtil.notEmpty(app, "app name cannot be empty");
        if (rules == null) {
            return;
        }
        String dataId = app + NacosConfigUtil.PrefixType.getPrefix(ruleType);
        Boolean result = nacosConfig.get().publishConfig(dataId,
                nacosPropertiesInject.getGroup(),
                RuleConfigUtil.getEncoder().convert(rules),
                ConfigType.JSON.getType()
        );
        LOG.info("publish result:{}", result);
    }

}
