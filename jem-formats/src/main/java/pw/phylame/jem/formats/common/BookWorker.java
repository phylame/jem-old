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

import pw.phylame.jem.formats.util.config.ConfigUtils;
import pw.phylame.jem.formats.util.config.CommonConfig;
import pw.phylame.jem.formats.util.config.InvalidConfigException;

/**
 * Common e-book parser.
 */
abstract class BookWorker<CF extends CommonConfig> {
    /**
     * Name of the worker.
     */
    private final String name;

    /**
     * Key for fetch <tt>CF</tt> object from config map.
     */
    private final String configKey;

    /**
     * Type of custom <tt>CF</tt> class.
     */
    private final Class<CF> configClass;

    BookWorker(String name, String configKey, Class<CF> configClass) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        if (configKey != null && configClass == null) {
            throw new IllegalArgumentException("'configKey' is valid but 'configClass' not");
        }
        this.name = name;
        this.configKey = configKey;
        this.configClass = configClass;
    }

    public final String getName() {
        return name;
    }

    // 1
    protected final CF fetchConfig(Map<String, Object> kw) throws InvalidConfigException {
        if (configKey == null) {   // no config required
            return null;
        }
        return ConfigUtils.fetchConfig(kw, configKey, configClass);
    }
}
