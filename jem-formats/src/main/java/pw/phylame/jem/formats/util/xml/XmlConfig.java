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

import pw.phylame.jem.formats.util.config.ConfigKey;
import pw.phylame.jem.formats.util.config.AbstractConfig;

/**
 * Config for rendering XML.
 */
public class XmlConfig extends AbstractConfig {
    public static final String CONFIG_SELF = "xml.render.config";
    public static final String ENCODING = "xml.render.encoding";
    public static final String STANDALONE = "xml.render.standalone";
    public static final String LINE_SEPARATOR = "xml.render.lineSeparator";
    public static final String INDENT_STRING = "xml.render.indentString";

    @ConfigKey(ENCODING)
    public String encoding = "UTF-8";

    @ConfigKey(STANDALONE)
    public boolean standalone = true;

    @ConfigKey(LINE_SEPARATOR)
    public String lineSeparator = "\n";

    @ConfigKey(INDENT_STRING)
    public String indentString = "\t";
}
