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

import java.util.Map;

import pw.phylame.jem.formats.util.text.TextConfig;
import pw.phylame.jem.formats.util.config.ConfigUtils;
import pw.phylame.jem.formats.util.config.CommonConfig;
import pw.phylame.jem.formats.util.config.InvalidConfigException;

/**
 * Config for making TXT book.
 */
public class TxtMakeConfig implements CommonConfig {
    public static final String CONFIG_SELF = "txt.make.config"; // RenderConfig
    public static final String TEXT_ENCODING = "txt.make.encoding"; // String
    public static final String HEADER_TEXT = "txt.make.header";    // String
    public static final String ADDITION_LINE = "txt.make.additionLine"; // String
    public static final String FOOTER_TEXT = "txt.make.footer";    // String

    /**
     * Render config for rendering book text.
     *
     * @see TextConfig
     */
    public TextConfig textConfig = new TextConfig();

    /**
     * Encoding for converting book text.
     */
    public String encoding = TXT.defaultEncoding;

    /**
     * Text appended to header of TXT file.
     */
    public String headerText = null;

    /**
     * Add addition end line separator for each chapter
     */
    public boolean additionLine = true;

    /**
     * Text appended to footer of TXT file.
     */
    public String footerText = null;

    @Override
    public void fetch(Map<String, Object> kw) throws InvalidConfigException {
        textConfig = TextConfig.fetchInstance(kw);
        encoding = ConfigUtils.fetchString(kw, TEXT_ENCODING, encoding);
        headerText = ConfigUtils.fetchString(kw, HEADER_TEXT, headerText);
        additionLine = ConfigUtils.fetchBoolean(kw, ADDITION_LINE, additionLine);
        footerText = ConfigUtils.fetchString(kw, FOOTER_TEXT, footerText);
    }
}
