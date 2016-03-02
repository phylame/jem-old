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

import java.io.*;
import java.util.Map;

import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Maker;
import pw.phylame.jem.util.JemException;
import pw.phylame.jem.formats.util.MakerException;
import pw.phylame.jem.formats.util.config.CommonConfig;

/**
 * Common Jem maker.
 */
public abstract class CommonMaker<CF extends CommonConfig> extends BookWorker<CF> implements Maker {
    protected CommonMaker(String name, String configKey, Class<CF> configClass) {
        super(name, configKey, configClass);
    }

    public abstract void make(Book book, OutputStream output, CF config) throws IOException, MakerException;

    @Override
    public final void make(Book book, File file, Map<String, Object> arguments) throws IOException, JemException {
        CF config = fetchConfig(arguments);
        try (OutputStream output = new BufferedOutputStream(new FileOutputStream(file))) {
            make(book, output, config);
        }
    }
}
