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

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.fastjson.JSON;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class RuleConfigUtil {

    private static final Map<Class<?>, Object> DECODER_MAP = new HashMap<>();

    private RuleConfigUtil() {
    }

    public static Converter<Object, String> getEncoder() {
        return JSON::toJSONString;
    }

    @SuppressWarnings("unchecked")
    public static synchronized <T extends RuleEntity> Converter<String, List<T>> getDecoder(Class<T> clazz) {
        Object decoder = DECODER_MAP.computeIfAbsent(clazz, (Function<Class<?>, Converter<String, List<T>>>) targetClass -> source -> JSON.parseArray(source, clazz));
        return (Converter<String, List<T>>) decoder;
    }

}
