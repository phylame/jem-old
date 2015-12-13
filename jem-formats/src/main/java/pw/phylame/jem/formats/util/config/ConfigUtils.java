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
import java.util.List;

import pw.phylame.jem.formats.util.ExceptionFactory;

/**
 * Utilities for maker and parser configurations
 */
public final class ConfigUtils {
    private ConfigUtils() {
    }

    /**
     * Parses config from raw string.
     */
    public interface ConfigParser<T> {
        T parse(String str);
    }

    public static String fetchString(Map<String, Object> kw, String key,
                                     String defaultValue)
            throws InvalidConfigException {
        return fetchObject(kw, key, defaultValue, String.class);
    }

    public static boolean fetchBoolean(Map<String, Object> kw, String key,
                                       boolean defaultValue)
            throws InvalidConfigException {
        Boolean b = fetchObject(kw, key, defaultValue, Boolean.class,
                BooleanParser.sharedParser());
        return (b != null) && b;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> fetchList(Map<String, Object> kw, String key,
                                        List<T> defaultValue,
                                        Class<T> clazz) throws InvalidConfigException {
        Object o = kw.get(key);
        if (o == null) {
            return kw.containsKey(key) ? null : defaultValue;
        }
        if (!(o instanceof List)) {
            throw ExceptionFactory.invalidObjectArgument(key, o,
                    "List<" + clazz.getName() + ">");
        }
        List list = (List) o;
        // just check one element
        if (!clazz.isInstance(list.get(0))) {
            throw ExceptionFactory.invalidObjectArgument(key, o,
                    "List<" + clazz.getName() + ">");
        }
        return (List<T>) list;
    }

    public static int fetchInteger(Map<String, Object> kw, String key,
                                   int defaultValue) throws InvalidConfigException {
        Integer n = fetchObject(kw, key, defaultValue, Integer.class,
                IntegerParser.sharedParser());
        return (n != null) ? n : 0;
    }

    public static <T> T fetchObject(Map<String, Object> kw, String key,
                                    T defaultValue, Class<T> clazz)
            throws InvalidConfigException {
        return fetchObject(kw, key, defaultValue, clazz, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> T fetchObject(Map<String, Object> kw, String key,
                                    T defaultValue, Class<T> clazz,
                                    ConfigParser<T> parser)
            throws InvalidConfigException {
        Object o = kw.get(key);
        if (o == null) {
            return kw.containsKey(key) ? null : defaultValue;
        }
        if (clazz.isInstance(o)) {
            return (T) o;
        } else if (parser != null && o instanceof String) {
            try {
                return parser.parse((String) o);
            } catch (RuntimeException e) {
                throw ExceptionFactory.invalidObjectArgument(e, key, o, clazz.getName());
            }
        } else {
            throw ExceptionFactory.invalidObjectArgument(key, o, clazz.getName());
        }
    }

    private static class BooleanParser implements ConfigParser<Boolean> {
        private static BooleanParser instance = null;

        static BooleanParser sharedParser() {
            if (instance == null) {
                instance = new BooleanParser();
            }
            return instance;
        }

        private BooleanParser() {
        }

        @Override
        public Boolean parse(String str) {
            return Boolean.parseBoolean(str);
        }
    }

    private static class IntegerParser implements ConfigParser<Integer> {
        private static IntegerParser instance = null;

        static IntegerParser sharedParser() {
            if (instance == null) {
                instance = new IntegerParser();
            }
            return instance;
        }

        private IntegerParser() {
        }

        @Override
        public Integer parse(String str) throws NumberFormatException {
            return Integer.parseInt(str);
        }
    }
}
