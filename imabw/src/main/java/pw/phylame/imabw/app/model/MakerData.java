/*
 * Copyright 2014-2015 Peng Wan <phylame@163.com>
 *
 * This file is part of Imabw.
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

package pw.phylame.imabw.app.model;

import java.io.File;
import java.util.Map;

import pw.phylame.jem.core.Book;
import pw.phylame.imabw.app.Worker;
import pw.phylame.jem.core.BookHelper;

/**
 * Warps arguments of Jem maker.
 */
public class MakerData {
    public Book book;
    public File file;
    public String format;
    public Map<String, Object> arguments;

    public MakerData(Book book, File file, String format) {
        this(book, file, format, Worker.sharedInstance().getDefaultMakerArguments(format));
    }

    public MakerData(Book book, File file, String format, Map<String, Object> arguments) {
        this.book = book;
        this.file = file;
        this.format = BookHelper.nameOfExtension(format);
        if (this.format == null) {
            this.format = format;
        }
        this.arguments = arguments;
    }
}
