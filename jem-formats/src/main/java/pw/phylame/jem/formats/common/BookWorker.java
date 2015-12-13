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

package pw.phylame.jem.formats.common;

import java.util.Map;
import java.io.IOException;

import pw.phylame.jem.formats.util.ExceptionFactory;
import pw.phylame.jem.formats.util.config.ConfigUtils;
import pw.phylame.jem.formats.util.config.CommonConfig;
import pw.phylame.jem.formats.util.config.InvalidConfigException;

/**
 * Common e-book parser.
 */
abstract class BookWorker<CF extends CommonConfig> {
    private String name;

    @SuppressWarnings("unchecked")
    public BookWorker(String name) {
        this(name, (Class<CF>) NonConfig.class, null);
    }

    public BookWorker(String name, Class<CF> configClass, String configKey) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        if (configClass == null) {
            throw new NullPointerException("configClass");
        }
        if (configKey == null && configClass != NonConfig.class) {
            throw new NullPointerException("configKey");
        }
        this.name = name;
        this.configClass = configClass;
        this.configKey = configKey;
    }

    public String getName() {
        return name;
    }

    /**
     * Key for fetch <tt>CF</tt> object from config map.
     */
    protected String configKey;

    /**
     * Type of custom <tt>CF</tt> class.
     */
    protected Class<CF> configClass;

    // 1
    protected CF fetchConfig(Map<String, Object> kw) throws InvalidConfigException {
        if (configClass == NonConfig.class) {    // no config
            return null;
        }
        CF config = ConfigUtils.fetchObject(kw, configKey, null, configClass);
        if (config != null) {
            return config;
        }
        config = defaultConfig();
        config.fetch(kw);
        return config;
    }

    private CF defaultConfig() {
        try {
            return configClass.newInstance();
        } catch (InstantiationException e) {
            throw new AssertionError("config cannot be instantiated");
        } catch (IllegalAccessException e) {
            throw new AssertionError("constructor of config is inaccessible");
        }
    }

    CF fetchOrCreate(Map<String, Object> kw) throws InvalidConfigException {
        if (kw != null && !kw.isEmpty()) {
            return fetchConfig(kw);
        } else if (configClass != NonConfig.class) {
            return defaultConfig();
        } else {
            return null;
        }
    }

    protected IOException ioException(String msg, Object... args) {
        return ExceptionFactory.ioException(msg, args);
    }
}
