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

package pw.phylame.jem.formats.pmab;

import java.util.Map;

import pw.phylame.jem.formats.util.config.ConfigUtils;
import pw.phylame.jem.formats.util.config.CommonConfig;
import pw.phylame.jem.formats.util.config.InvalidConfigException;

/**
 * Config for parse PMAB file.
 */
public class PmabParseConfig implements CommonConfig {
    public static final String CONFIG_SELF = "pmab.parse.config";  // PmabParseConfig
    public static final String TEXT_ENCODING = "pmab.parse.textEncoding"; // String
    public static final String USE_CHAPTER_ENCODING = "pmab.parse.useChapterEncoding";   // boolean
    public static final String DEFAULT_DATE_FORMAT = "pmab.parse.dateFormat"; // String

    /**
     * default encoding for chapter and intro text
     */
    public String textEncoding = PMAB.defaultEncoding;

    /**
     * PMAB 2: when intro encoding is not existed, use chapter encoding
     */
    public boolean useChapterEncoding = true;

    /**
     * default date format if the format in PMAB is unknown
     **/
    public String dateFormat = "yyyy-M-d H:m:S";

    @Override
    public void fetch(Map<String, Object> kw) throws InvalidConfigException {
        textEncoding = ConfigUtils.fetchString(kw, TEXT_ENCODING, textEncoding);
        useChapterEncoding = ConfigUtils.fetchBoolean(kw, USE_CHAPTER_ENCODING,
                useChapterEncoding);
        dateFormat = ConfigUtils.fetchString(kw, DEFAULT_DATE_FORMAT, dateFormat);
    }
}
