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

package pw.phylame.imabw;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Jem;

import java.io.File;

/**
 * Describes current book task.
 */
public class Task {
    private static Log LOG = LogFactory.getLog(Task.class);

    private Book book;

    /** Source book file */
    private File source = null;

    /** Cached PMAB book file */
    private File cache = null;

    /** Delete cache file when calling cleanup */
    private boolean deleteCache = false;

    /** Format of source file */
    private String format;
    private boolean modified = false;

    private Task() {
    }

    private Task(Book book, File source, File cache, String format) {
        this.book = book;
        this.source = source;
        this.cache = cache;
        this.format = format;
    }

    public static Task newBook(Book book) {
        return new Task(book, null, null, Jem.PMAB_FORMAT);
    }

    public static Task fromFile(Book book, File source, String format) {
        return new Task(book, source, null, format);
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public File getSource() {
        return source;
    }

    public void setSource(File source) {
        this.source = source;
    }

    public File getCache() {
        return cache;
    }

    public void setCache(File cache, boolean autoDelete) {
        this.cache = cache;
        this.deleteCache = autoDelete;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public void cleanup() {
        book.cleanup();
        if (cache != null && deleteCache) {
            if (! cache.delete()) {
                LOG.debug("cannot delete PMAB cache file: "+cache.getAbsolutePath());
            }
        }
    }
}
