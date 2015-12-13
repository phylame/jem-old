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

package pw.phylame.jem.formats.ucnovel;

import pw.phylame.jem.util.JemException;

/**
 * UC novel SQLite database reader.
 */
public interface NovelDbReader {

    /**
     * Initializes UC novel database.
     * @param dbPath URL of DB file
     * @param bookId book_id of novel
     * @exception JemException if failed to init DB reader
     */
    void init(String dbPath, String bookId) throws JemException;

    /**
     * Reads novel details from specified file.
     * @return <tt>NovelDetails</tt>
     */
    NovelDetails fetchDetails() throws JemException;

    void watchChapter(ChapterWatcher watcher, String dbTable) throws JemException;

    /**
     * Clean up all DB resources.
     */
    void cleanup();
}
