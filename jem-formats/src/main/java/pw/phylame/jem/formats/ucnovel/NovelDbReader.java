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

import java.io.Closeable;

import pw.phylame.jem.formats.util.ParserException;

/**
 * Reader for UC novel SQLite database.
 */
public interface NovelDbReader extends Closeable {
    String SQL_FETCH_NOVELS = "SELECT book_id FROM CATALOG_TABLE";
    String SQL_FETCH_INFO = "SELECT * FROM CATALOG_TABLE WHERE book_id='%s'";
    String SQL_FETCH_CHAPTER = "SELECT * FROM %s";

    String NOVEL_NAME = "novel_name";
    String NOVEL_AUTHOR = "novel_author";
    String NOVEL_EXPIRE_TIME = "expire_time";
    String NOVEL_UPDATE_TIME = "update_time";
    String NOVEL_TABLE_NAME = "catalog_table_name";

    String CHAPTER_ID = "chapter_id";
    String CHAPTER_NAME = "chapter_name";
    String CHAPTER_INDEX_START = "index_start";
    String CHAPTER_INDEX_END = "index_end";
    String CHAPTER_IS_NEW = "is_new_chapter";
    String CHAPTER_INDEX = "item_index";
    String CHAPTER_OFFLINE_PATH = "offline_file_path";
    String CHAPTER_CDN_URL = "cdn_url";
    String CHAPTER_CONTENT_KEY = "content_key";
    String CHAPTER_UPDATE_TIME = "update_time";

    /**
     * Initializes UC novel database.
     *
     * @param dbPath native path of the novel DB file
     * @throws ParserException if failed to init DB reader
     */
    void init(String dbPath) throws ParserException;

    /**
     * Fetches list of all novels in database.
     *
     * @return the array of novel id
     * @throws ParserException if failed to query database
     */
    String[] fetchNovels() throws ParserException;

    /**
     * Fetches details for specified novel.
     *
     * @param novelId id of the novel
     * @return new <tt>NovelInfo</tt> object or <tt>null</tt> if the <code>novelId</code> not present
     * @throws ParserException if failed to query database
     */
    NovelInfo fetchInfo(String novelId) throws ParserException;

    void fetchChapters(ChapterWatcher watcher, String novelTable) throws ParserException;
}
