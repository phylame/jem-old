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

package pw.phylame.jem.formats.common;

import pw.phylame.jem.formats.util.config.CommonConfig;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

/**
 * Common parser for e-book file archived with ZIP.
 */
public abstract class
ZipBookParser<CF extends CommonConfig> extends CommonParser<ZipFile, CF> {
    public ZipBookParser(String name) {
        super(name);
    }

    public ZipBookParser(String name, Class<CF> configClass, String configKey) {
        super(name, configClass, configKey);
    }

    @Override
    protected ZipFile openInput(File file, CF config) throws IOException {
        return new ZipFile(file);
    }
}
