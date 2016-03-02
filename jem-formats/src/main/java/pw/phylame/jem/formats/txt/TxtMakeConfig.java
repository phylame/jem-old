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

package pw.phylame.jem.formats.txt;

import pw.phylame.jem.formats.util.text.TextConfig;
import pw.phylame.jem.formats.util.config.ConfigKey;
import pw.phylame.jem.formats.util.config.AbstractConfig;

/**
 * Config for making TXT book.
 */
public class TxtMakeConfig extends AbstractConfig {
    public static final String CONFIG_SELF = "txt.make.config";
    public static final String TEXT_CONFIG = "txt.make.textConfig";
    public static final String ENCODING = "txt.make.encoding";
    public static final String HEADER = "txt.make.header";
    public static final String ADDITION_LINE = "txt.make.additionLine";
    public static final String FOOTER = "txt.make.footer";

    /**
     * Render config for rendering book text.
     *
     * @see TextConfig
     */
    @ConfigKey(TEXT_CONFIG)
    public TextConfig textConfig = new TextConfig();

    /**
     * Encoding for converting book text.
     */
    @ConfigKey(ENCODING)
    public String encoding = TXT.defaultEncoding;

    /**
     * Text appended to header of TXT file.
     */
    @ConfigKey(HEADER)
    public String header = null;

    /**
     * Add addition end line separator for each chapter
     */
    @ConfigKey(ADDITION_LINE)
    public boolean additionLine = true;

    /**
     * Text appended to footer of TXT file.
     */
    @ConfigKey(FOOTER)
    public String footer = null;
}
