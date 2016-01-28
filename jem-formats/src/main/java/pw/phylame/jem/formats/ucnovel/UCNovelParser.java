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

import java.net.URL;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Enumeration;

import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.core.Parser;
import pw.phylame.jem.formats.util.BufferedRandomAccessFile;
import pw.phylame.jem.formats.util.MessageBundle;
import pw.phylame.jem.formats.util.ParserException;
import pw.phylame.jem.formats.util.SourceCleaner;
import pw.phylame.jem.util.*;

/**
 * Jem Parser for UC Browser Novels.
 */
public class UCNovelParser implements Parser, ChapterWatcher {
    public static final String KEY_BOOK_ID = "uc_book_id";

    static final String DB_READER_CONFIG = "META-INF/pw-jem/ucnovel-reader";
    static final String CATALOG_FILE_NAME = "com.UCMobile_catalog";
    static final String TEXT_ENCODING = "UTF-8";

    private File catalogFile;
    private String bookId;

    private NovelDbReader dbReader = null;
    private BufferedRandomAccessFile source;

    private Book book;

    @Override
    public String getName() {
        return "ucnovel";
    }

    private String getBookId(Map<String, Object> kw) throws JemException {
        JemException ex = new JemException(MessageBundle.getText("ucnovel.parse.noBookId", KEY_BOOK_ID));
        if (kw == null || kw.isEmpty()) {
            throw ex;
        }

        Object o = kw.get(KEY_BOOK_ID);
        if (!(o instanceof String)) {
            throw ex;
        }

        return (String) o;
    }

    @Override
    public Book parse(File file, Map<String, Object> kw) throws IOException, JemException {
        return parse(file, getBookId(kw));
    }

    public Book parse(File file, String bookId) throws IOException, JemException {
        if (file.isDirectory()) {
            file = new File(file, CATALOG_FILE_NAME);
        }
        catalogFile = file;
        this.bookId = bookId;

        loadDbReader();
        if (dbReader == null) {
            throw new ParserException(MessageBundle.getText("ucnovel.parse.noDbReader", DB_READER_CONFIG));
        }
        dbReader.init(catalogFile.getPath(), this.bookId);

        return fetchBook();
    }

    private Book fetchBook() throws IOException, JemException {
        book = new Book();

        NovelDetails details = readDetails();
        File file = new File(catalogFile.getParent(), bookId + "/" + bookId + ".ucnovel");
        source = new BufferedRandomAccessFile(file, "r");
        SourceCleaner cleaner = new SourceCleaner(source, new Runnable() {
            @Override
            public void run() {
                dbReader.cleanup();
            }
        });
        try {
            dbReader.watchChapter(this, details.getTable());
        } catch (JemException e) {
            cleaner.clean(null);
            throw e;
        }

        book.registerCleanup(cleaner);
        return book;
    }

    private NovelDetails readDetails() throws JemException {
        NovelDetails details = dbReader.fetchDetails();
        book.setTitle(details.getName());
        book.setAuthor(details.getAuthor());
        book.setDate(details.getExpireTime());
        book.setAttribute("update_time", details.getUpdateTime());
        return details;
    }

    @Override
    public void watch(ChapterItem item) throws JemException {
        Chapter chapter = new Chapter(item.getTitle());
        chapter.setAttribute("update_time", item.getUpdateTime());

        FileObject fb;
        try {
            fb = FileFactory.fromBlock("chapter-" + item.getId() + ".txt",
                    source, item.getStartIndex(), item.getEndIndex() - item.getStartIndex() + 1, "text/plain");
        } catch (IOException e) {
            throw new JemException(e);
        }
        chapter.setContent(TextFactory.fromFile(fb, TEXT_ENCODING));

        book.append(chapter);
    }

    private void loadDbReader() throws JemException {
        Enumeration<URL> urls = IOUtils.getResources(DB_READER_CONFIG,
                UCNovelParser.class.getClassLoader());
        if (urls == null) {
            return;
        }

        if (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            try {
                String name = IOUtils.toString(url.openStream(), null).trim();
                Class<?> clazz = Class.forName(name);
                if (NovelDbReader.class.isAssignableFrom(clazz)) {
                    dbReader = (NovelDbReader) clazz.newInstance();
                } else {
                    throw new JemException("Invalid NovelDbReader: " + name);
                }
            } catch (IOException | IllegalAccessException
                    | InstantiationException | ClassNotFoundException e) {
                throw new JemException("Failed to load NovelDbReader", e);
            }
        }
    }
}
