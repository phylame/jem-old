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

package pw.phylame.jem.formats.util.xml;

import java.util.Map;

import pw.phylame.jem.formats.util.config.ConfigUtils;
import pw.phylame.jem.formats.util.config.CommonConfig;
import pw.phylame.jem.formats.util.config.InvalidConfigException;

/**
 * Config for rendering XML.
 */
public class XmlConfig implements CommonConfig {
    public static final String CONFIG_SELF = "xml.render.config";
    public static final String ENCODING = "xml.render.encoding";
    public static final String STANDALONE = "xml.render.standalone";
    public static final String LINE_SEPARATOR = "xml.render.lineSeparator";
    public static final String INDENT_STRING = "xml.render.indentString";

    public String encoding = "UTF-8";
    public boolean standalone = true;
    public String lineSeparator = "\n";
    public String indentString = "\t";

    public static XmlConfig fetchInstance(Map<String, Object> kw) throws InvalidConfigException {
        return ConfigUtils.fetchConfig(kw, CONFIG_SELF, XmlConfig.class);
    }

    @Override
    public void fetch(Map<String, Object> kw) throws InvalidConfigException {
        encoding = ConfigUtils.fetchString(kw, ENCODING, encoding);
        standalone = ConfigUtils.fetchBoolean(kw, STANDALONE, standalone);
        lineSeparator = ConfigUtils.fetchString(kw, LINE_SEPARATOR, lineSeparator);
        indentString = ConfigUtils.fetchString(kw, INDENT_STRING, indentString);
    }
}
