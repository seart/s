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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * @Author no one
 * @Description 获取 dataId 的名称
 * @Date 2024-08-02 09:11
 */
public final class NacosConfigUtil {

    private static final Logger LOG = LoggerFactory.getLogger(NacosConfigUtil.class);


    public static final String FLOW_DATA_ID_POSTFIX = "-flow-rules";
    public static final String DEGRADE_RULES = "-degrade-rules";
    public static final String PARAM_FLOW_RULES = "-param-flow-rules";
    public static final String SYSTEM_RULES = "-system-rules";
    public static final String AUTHORITY_RULES = "-authority-rules";
    public static final String GW_FLOW_RULES = "-gw-flow-rules";
    public static final String GW_API_GROUP_RULES = "-gw-api-group-rules";

    /**
     * cc for `cluster-client`
     */
    public static final String CLIENT_CONFIG_DATA_ID_POSTFIX = "-cc-config";
    /**
     * cs for `cluster-server`
     */
    public static final String SERVER_TRANSPORT_CONFIG_DATA_ID_POSTFIX = "-cs-transport-config";
    public static final String SERVER_FLOW_CONFIG_DATA_ID_POSTFIX = "-cs-flow-config";
    public static final String SERVER_NAMESPACE_SET_DATA_ID_POSTFIX = "-cs-namespace-set";


    enum PrefixType {

        /**
         * 流控规则
         */
        FLOW("flow", FLOW_DATA_ID_POSTFIX),
        /**
         * 熔断规则
         */
        DEGRADE("degrade", DEGRADE_RULES),
        /**
         * 热点规则
         */
        PARAM_FLOW("param-flow", PARAM_FLOW_RULES),
        /**
         * 系统规则
         */
        SYSTEM("system", SYSTEM_RULES),
        /**
         * 授权规则
         */
        AUTHORITY("authority", AUTHORITY_RULES),
        /**
         * 网关流控规则
         */
        GW_FLOW("gw-flow", GW_FLOW_RULES),
        /**
         * api 分组
         */
        GW_API_GROUP("gw-api-group", GW_API_GROUP_RULES);

        private final String name;
        private String prefix;

        PrefixType(String name, String prefix) {
            this.name = name;
            this.prefix = prefix;
        }

        public String getName() {
            return name;
        }

        public String getPrefix() {
            return prefix;
        }

        public static String getPrefix(RuleType ruleType) {
            LOG.info("ruletype:{}", ruleType.getName());
            return Arrays.stream(PrefixType.values())
                    .filter(type -> type.name.equals(ruleType.getName()))
                    .findAny().get().getPrefix();
        }
    }
}
