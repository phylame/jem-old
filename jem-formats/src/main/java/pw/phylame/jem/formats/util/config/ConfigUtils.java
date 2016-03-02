/*
 * Copyright 2014-2015 Peng Wan <phylame@163.com>
 *
 * This file is part of Jem.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pw.phylame.jem.formats.util.config;

import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Field;

import pw.phylame.jem.formats.util.ExceptionFactory;
import pw.phylame.jem.formats.util.MessageBundle;

/**
 * Utilities for maker and parser configurations
 */
public final class ConfigUtils {
    private ConfigUtils() {
    }

    public static <CF extends CommonConfig> CF defaultConfig(Class<CF> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new AssertionError("BUG: the constructor of config class is unavailable: " + clazz, e);
        }
    }

    public static <CF extends CommonConfig> CF fetchConfig(Map<String, Object> kw, String key, Class<CF> clazz)
            throws InvalidConfigException {
        if (kw == null || kw.isEmpty()) {
            return defaultConfig(clazz);
        }
        CF config = fetchObject(kw, key, null, clazz); // find the config object by key
        if (config != null) {
            return config;
        }
        config = defaultConfig(clazz);
        fetchFields(config, kw);
        return config;
    }

    private static void fetchFields(CommonConfig config, Map<String, Object> kw) throws InvalidConfigException {
        Field[] fields = config.getClass().getFields();
        for (Field field : fields) {
            ConfigKey configKey = field.getAnnotation(ConfigKey.class);
            if (configKey == null) {
                continue;
            }
            String key = configKey.value();
            Class<?> type = field.getType();
            try {
                Object defaultValue = field.get(config);
                Object value = ConfigUtils.fetchObject(kw, key, null, type);
                if (value == null) {
                    if (CommonConfig.class.isAssignableFrom(type)) {   // not found and field is CommonConfig
                        fetchFields((CommonConfig) defaultValue, kw);
                    }
                    value = defaultValue;
                }
                field.set(config, value);
            } catch (IllegalAccessException e) {
                throw new InvalidConfigException(key, null,
                        MessageBundle.getText("error.config.inaccessible", key, config.getClass().getName()));
            }
        }
        if (fields.length > 0) {
            config.adjust();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T fetchObject(Map<String, Object> kw, String key, Object defaultValue, Class<T> type)
            throws InvalidConfigException {
        Object o = kw.get(key);
        if (o == null) {
            return kw.containsKey(key) ? null : (T) defaultValue;
        }
        if (type.isInstance(o)) {   // found the item
            return (T) o;
        }
        ConfigParser<T> parser = getConfigParser(type);
        if (parser != null && o instanceof String) { // parse string value
            try {
                return parser.parse((String) o);
            } catch (RuntimeException e) {
                throw ExceptionFactory.invalidObjectArgument(e, key, o, type.getName());
            }
        } else {
            throw ExceptionFactory.invalidObjectArgument(key, o, type.getName());
        }
    }

    /**
     * Parses config from raw string.
     */
    public interface ConfigParser<T> {
        T parse(String str);
    }

    private static HashMap<Class<?>, ConfigParser<?>> parserMap = new HashMap<>();

    public static <T> void mapConfigParser(Class<T> type, ConfigParser<T> parser) {
        parserMap.put(type, parser);
    }

    @SuppressWarnings("unchecked")
    public static <T> ConfigParser<T> getConfigParser(Class<T> type) {
        return (ConfigParser<T>) parserMap.get(type);
    }

    static {
        mapConfigParser(Boolean.class, new ConfigParser<Boolean>() {
            @Override
            public Boolean parse(String str) {
                return Boolean.parseBoolean(str);
            }
        });
        mapConfigParser(Integer.class, new ConfigParser<Integer>() {
            @Override
            public Integer parse(String str) {
                return Integer.parseInt(str);
            }
        });
    }
}
