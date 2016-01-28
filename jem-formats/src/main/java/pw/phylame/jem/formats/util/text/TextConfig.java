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

package pw.phylame.jem.formats.util.text;

import java.util.Map;

import pw.phylame.jem.formats.util.MessageBundle;
import pw.phylame.jem.formats.util.config.ConfigUtils;
import pw.phylame.jem.formats.util.config.CommonConfig;
import pw.phylame.jem.formats.util.config.InvalidConfigException;

/**
 * Config for rendering text.
 */
public class TextConfig implements CommonConfig {
    public static final String CONFIG_SELF = "text.render.config";  // RenderConfig
    public static final String WRITE_TITLE = "text.render.writeTitle";  // boolean
    public static final String JOIN_TITLES = "text.render.joinTitles";  // boolean
    public static final String PREFIX_TEXT = "text.render.prefixText";  // String
    public static final String TITLE_SEPARATOR = "text.render.titleSeparator";  // String
    public static final String WRITE_INTRO = "text.render.writeIntro";  // boolean
    public static final String INTRO_SEPARATOR = "text.render.introSeparator";  // String
    public static final String FORMAT_PARAGRAPH = "text.render.formatParagraph";    // boolean
    public static final String PARAGRAPH_PREFIX = "text.render.paragraphPrefix";    // String
    public static final String SKIP_EMPTY_LINE = "text.render.skipEmptyLine";   // boolean
    public static final String LINE_SEPARATOR = "text.render.lineSeparator";    // String
    public static final String SUFFIX_TEXT = "text.render.suffixText";  // String
    public static final String PADDING_LINE = "text.render.paddingLine";  // String
    public static final String HTML_CONVERTER = "text.render.textConverter";    // TextConverter

    /**
     * Write chapter title before chapter text.
     */
    public boolean writeTitle = true;

    /**
     * Join chapter title chain with next specified separator.
     */
    public boolean joinTitles = false;

    /**
     * Separator for joining title chain.
     */
    public String titleSeparator = " ";

    /**
     * Text added before chapter text and behind of chapter title.
     */
    public String prefixText = null;

    /**
     * Write intro text before chapter text.
     */
    public boolean writeIntro = true;

    /**
     * Separator between intro text and chapter text.
     */
    public String introSeparator = "-------";

    /**
     * Process lines in text (prepend paragraph prefix to line).
     */
    public boolean formatParagraph = false;

    /**
     * Paragraph prefix used when formatParagraph is enable.
     */
    public String paragraphPrefix = MessageBundle.getText("text.render.paragraphPrefix");

    /**
     * Skip empty line, (enable when formatParagraph is enable).
     */
    public boolean skipEmptyLine = true;

    /**
     * Line separator.
     */
    public String lineSeparator = System.getProperty("line.separator");

    /**
     * Text added at end of chapter text (before paddingLine),
     * append line separator to chapter text.
     */
    public String suffixText = null;

    /**
     * Add a addition line separator after chapter text.
     */
    public boolean paddingLine = true;

    /**
     * The <tt>TextConverter</tt> for converting HTML to plain text. If not
     * specify, the origin HTML will be rendered.
     */
    public TextConverter textConverter = null;

    /**
     * Fetches config object from Jem maker arguments.
     * <p>If not config found in <tt>kw</tt>, a default config will be returned.
     *
     * @param kw the maker arguments
     * @return <tt>RenderConfig</tt> object
     * @throws InvalidConfigException if invalid config found
     */
    public static TextConfig fetchInstance(Map<String, Object> kw) throws InvalidConfigException {
        return ConfigUtils.fetchConfig(kw, CONFIG_SELF, TextConfig.class);
    }

    @Override
    public void fetch(Map<String, Object> kw) throws InvalidConfigException {
        prefixText = ConfigUtils.fetchString(kw, PREFIX_TEXT, prefixText);
        writeTitle = ConfigUtils.fetchBoolean(kw, WRITE_TITLE, writeTitle);
        joinTitles = ConfigUtils.fetchBoolean(kw, JOIN_TITLES, joinTitles);
        titleSeparator = ConfigUtils.fetchString(kw, TITLE_SEPARATOR, titleSeparator);
        writeIntro = ConfigUtils.fetchBoolean(kw, WRITE_INTRO, writeIntro);
        introSeparator = ConfigUtils.fetchString(kw, INTRO_SEPARATOR, introSeparator);
        formatParagraph = ConfigUtils.fetchBoolean(kw, FORMAT_PARAGRAPH, formatParagraph);
        paragraphPrefix = ConfigUtils.fetchString(kw, PARAGRAPH_PREFIX, paragraphPrefix);
        skipEmptyLine = ConfigUtils.fetchBoolean(kw, SKIP_EMPTY_LINE, skipEmptyLine);
        lineSeparator = ConfigUtils.fetchString(kw, LINE_SEPARATOR, lineSeparator);
        paddingLine = ConfigUtils.fetchBoolean(kw, PADDING_LINE, paddingLine);
        suffixText = ConfigUtils.fetchString(kw, SUFFIX_TEXT, suffixText);
        textConverter = ConfigUtils.fetchObject(kw, HTML_CONVERTER, textConverter, TextConverter.class);
    }
}
