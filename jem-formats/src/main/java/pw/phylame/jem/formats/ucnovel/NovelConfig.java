/*
 * Copyright 2014-2016 Peng Wan <phylame@163.com>
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

package pw.phylame.jem.formats.ucnovel;

import pw.phylame.jem.formats.util.config.AbstractConfig;
import pw.phylame.jem.formats.util.config.ConfigKey;

public class NovelConfig extends AbstractConfig {
    public static final String CONFIG_SELF = "parse.uc.config";
    public static final String NOVEL_ID = "parse.uc.novelId";
    public static final String READER_CONFIG = "parse.uc.readerConfig";
    public static final String NOVEL_FOLDER = "parse.uc.novelFolder";

    @ConfigKey(NOVEL_ID)
    public String novelId;

    @ConfigKey(READER_CONFIG)
    public String readerConfig = UCNovelParser.DEFAULT_CONFIG_FILE;

    @ConfigKey(NOVEL_FOLDER)
    public String novelFolder = null;
}
