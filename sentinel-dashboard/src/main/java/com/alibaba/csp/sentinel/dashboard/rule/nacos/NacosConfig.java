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

import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;

import java.util.Properties;

/**
 * @Author no one
 * @Description 获取ConfigServer 类
 * @Date 2024-08-02 09:25
 */

public class NacosConfig {

    private ConfigService nacosClient;

    public NacosConfig(String serverAddr, String namespace, String group, Long timeout, String username, String password) throws Exception {
        initNacosConfig(serverAddr, namespace, group, timeout, username, password);
    }

    private void initNacosConfig(String serverAddr,
                                 String namespace,
                                 String group,
                                 Long timeout,
                                 String username,
                                 String password
    ) throws Exception {
        Properties properties = new Properties();
        properties.setProperty("serverAddr", serverAddr);
        properties.setProperty("namespace", namespace);
        properties.setProperty("group", group);
        properties.setProperty("timeout", String.valueOf(timeout));
        properties.setProperty("username", username);
        properties.setProperty("password", password);
        nacosClient = ConfigFactory.createConfigService(properties);
    }

    public ConfigService get() {
        return nacosClient;
    }
}
