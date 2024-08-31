package com.alibaba.csp.sentinel.dashboard.config;

import com.alibaba.csp.sentinel.dashboard.config.RuleConfiguration.RuleConfigurationImportSelector;
import com.alibaba.csp.sentinel.dashboard.config.rule.RuleStoreType;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @Author  no one
 * @Description     将 RuleStoreType 中的2个实现类 生成 bean 放入到 spring 容器中
 * @Date 2024-08-02 09:25
 */
@Configuration
@Import({RuleConfigurationImportSelector.class})
public class RuleConfiguration {


    static class RuleConfigurationImportSelector implements ImportSelector {
        @Override
        public String[] selectImports(AnnotationMetadata importingClassMetadata) {
            RuleStoreType[] types = RuleStoreType.values();
            String[] imports = new String[types.length];
            for (int i = 0; i < types.length; i++) {
                imports[i] = types[i].getConfigurationClass().getName();
            }
            return imports;
        }
    }

}
