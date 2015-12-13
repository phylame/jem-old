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

import pw.phylame.jem.formats.util.MessageBundle;
import pw.phylame.jem.formats.util.config.ConfigUtils;
import pw.phylame.jem.formats.util.config.CommonConfig;
import pw.phylame.jem.formats.util.config.InvalidConfigException;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Config for parse TXT file.
 */
public class TxtParseConfig implements CommonConfig {
    public static final String CONFIG_SELF = "txt.parse.config";   // TxtParseConfig
    public static final String TEXT_ENCODING = "txt.parse.encoding";      // String
    public static final String CHAPTER_PATTERN = "txt.parse.pattern";     // String
    public static final String PATTERN_FLAGS = "txt.parse.patternFlags";  // int
    public static final String TRIM_CHAPTER_TITLE = "txt.parse.trimChapterTitle";   // boolean

    /**
     * Text encoding of input file
     */
    public String encoding = TXT.defaultEncoding;

    /**
     * Chapter title regex pattern
     */
    public String pattern = MessageBundle.getText("txt.parse.pattern");

    /**
     * Regex pattern flag.
     */
    public int patternFlags = Pattern.MULTILINE;

    /**
     * Remove leading and tailing space of chapter title.
     */
    public boolean trimChapterTitle = true;

    @Override
    public void fetch(Map<String, Object> kw) throws InvalidConfigException {
        encoding = ConfigUtils.fetchString(kw, TEXT_ENCODING, encoding);
        pattern = ConfigUtils.fetchString(kw, CHAPTER_PATTERN, pattern);
        patternFlags = ConfigUtils.fetchInteger(kw, PATTERN_FLAGS, patternFlags);
        trimChapterTitle = ConfigUtils.fetchBoolean(kw, TRIM_CHAPTER_TITLE,
                trimChapterTitle);
    }
}
