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

import java.util.regex.Pattern;

import pw.phylame.jem.formats.util.MessageBundle;
import pw.phylame.jem.formats.util.config.ConfigKey;
import pw.phylame.jem.formats.util.config.AbstractConfig;

/**
 * Config for parse TXT file.
 */
public class TxtParseConfig extends AbstractConfig {
    public static final String CONFIG_SELF = "txt.parse.config";
    public static final String ENCODING = "txt.parse.encoding";
    public static final String PATTERN = "txt.parse.pattern";
    public static final String PATTERN_FLAGS = "txt.parse.patternFlags";
    public static final String TRIM_CHAPTER_TITLE = "txt.parse.trimChapterTitle";

    /**
     * Text encoding of input file
     */
    @ConfigKey(ENCODING)
    public String encoding = TXT.defaultEncoding;

    /**
     * Chapter title regex pattern
     */
    @ConfigKey(PATTERN)
    public String pattern = MessageBundle.getText("txt.parse.pattern");

    /**
     * Regex pattern flag.
     */
    @ConfigKey(PATTERN_FLAGS)
    public int patternFlags = Pattern.MULTILINE;

    /**
     * Remove leading and tailing space of chapter title.
     */
    @ConfigKey(TRIM_CHAPTER_TITLE)
    public boolean trimChapterTitle = true;
}
