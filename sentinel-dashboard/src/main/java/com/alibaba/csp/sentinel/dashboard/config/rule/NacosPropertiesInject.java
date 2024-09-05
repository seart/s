package com.alibaba.csp.sentinel.dashboard.config.rule;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Author  no one
 * @Description    读取properties 文件数据
 * @Date 2024-08-02 09:25
 */
@ConfigurationProperties(prefix = "nacos.sentinel")
public class NacosPropertiesInject {

    private String group;

    private String namespace;

    private String serverAddr;

    private Long timeout;

    private String username;
    private String password;

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    public String getServerAddr() {
        return serverAddr;
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public String getGroup() {
        return group;
    }


    public void setGroup(String group) {
        this.group = group;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
