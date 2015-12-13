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

import pw.phylame.jem.core.Book;
import pw.phylame.jem.formats.util.MakerException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipOutputStream;

/**
 * Common Jem writer for e-book archived with ZIP.
 */
public abstract class
ZipBookMaker<CF extends ZipBookConfig> extends CommonMaker<CF> {

    public ZipBookMaker(String name, Class<CF> configClass, String configKey) {
        super(name, configClass, configKey);
    }

    public abstract void make(Book book, ZipOutputStream zipout, CF config)
            throws IOException, MakerException;

    @Override
    public void make(Book book, OutputStream output, CF config) throws IOException,
            MakerException {
        ZipOutputStream zipout = new ZipOutputStream(output);
        zipout.setMethod(config.zipMethod);
        zipout.setLevel(config.zipLevel);
        zipout.setComment(config.zipComment);
        make(book, zipout, config);
        zipout.close();
    }
}
