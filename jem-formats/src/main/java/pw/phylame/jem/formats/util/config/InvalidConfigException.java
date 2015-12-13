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

import pw.phylame.jem.util.JemException;

/**
 * Exception for invalid maker or parser configuration(s).
 */
public class InvalidConfigException extends JemException {
    private final String key;
    private final Object value;

    public InvalidConfigException(String key, Object value, String message) {
        super(message);
        this.key = key;
        this.value = value;
    }

    public InvalidConfigException(String key, Object value, String message, Throwable cause) {
        super(message, cause);
        this.key = key;
        this.value = value;
    }

    public InvalidConfigException(String key, Object value, Throwable cause) {
        super(cause);
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }
}
