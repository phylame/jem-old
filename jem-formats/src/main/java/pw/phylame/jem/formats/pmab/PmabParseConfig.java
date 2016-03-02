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

import pw.phylame.jem.formats.common.ZipParseConfig;
import pw.phylame.jem.formats.util.config.ConfigKey;

/**
 * Config for parse PMAB file.
 */
public class PmabParseConfig extends ZipParseConfig {
    public static final String CONFIG_SELF = "pmab.parse.config";
    public static final String TEXT_ENCODING = "pmab.parse.textEncoding";
    public static final String USE_CHAPTER_ENCODING = "pmab.parse.useChapterEncoding";
    public static final String DATE_FORMAT = "pmab.parse.dateFormat";

    /**
     * default encoding for chapter and intro text
     */
    @ConfigKey(TEXT_ENCODING)
    public String textEncoding = PMAB.defaultEncoding;

    /**
     * PMAB 2: when intro encoding is not existed, use chapter encoding
     */
    @ConfigKey(USE_CHAPTER_ENCODING)
    public boolean useChapterEncoding = true;

    /**
     * default date format if the format in PMAB is unknown
     **/
    @ConfigKey(DATE_FORMAT)
    public String dateFormat = "yyyy-M-d H:m:S";
}
