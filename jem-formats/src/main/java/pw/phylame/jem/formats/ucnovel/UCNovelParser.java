/*
 * Copyright 2015 Peng Wan <phylame@163.com>
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
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.Enumeration;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.util.FileObject;
import pw.phylame.jem.util.FileFactory;
import pw.phylame.jem.util.TextFactory;
import pw.phylame.jem.util.JemException;
import pw.phylame.jem.util.JemUtilities;
import pw.phylame.jem.formats.util.AbstractParser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Jem Parser for UC Browser Novels.
 */
public class UCNovelParser extends AbstractParser implements ChapterWatcher {
    private static Log LOG = LogFactory.getLog(UCNovelParser.class);

    public static final String KEY_BOOK_ID = "uc_book_id";

    static final String DB_READER_CONFIG  = "META-INF/pw-jem/ucnovel-reader";
    static final String CATALOG_FILE_NAME = "com.UCMobile_catalog";
    static final String TEXT_ENCODING     = "UTF-8";

    private File   mDbFile;
    private String mBookId;

    private NovelDbReader    mDbReader  = null;
    private RandomAccessFile mNovelFile = null;

    private Book mBook;

    @Override
    public String getName() {
        return "ucnovel";
    }

    private String getBookId(Map<String, Object> kw) throws JemException {
        JemException ex = makeParserException("No '" + KEY_BOOK_ID + "' specified in parser arguments");
        if (kw == null || kw.size() == 0) {
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
        mDbFile = file;
        mBookId = bookId;

        loadDbReader();
        if (mDbReader == null) {
            throw makeParserException("No NovelDbReader found");
        }
        mDbReader.init(mDbFile.getPath(), mBookId);

        return fetchBook();
    }

    private Book fetchBook() throws IOException, JemException {
        mBook = new Book();

        try {
            NovelDetails details = readDetails();
            File file = new File(mDbFile.getParent(), mBookId+"/"+mBookId+".ucnovel");
            mNovelFile = new RandomAccessFile(file, "r");
            mDbReader.watchChapter(this, details.getTable());
        } catch (IOException ex) {
            mDbReader.cleanup();
            throw new JemException(ex);
        }

        mBook.registerCleanup(new Chapter.Cleanable() {
            @Override
            public void clean(Chapter chapter) {
                mDbReader.cleanup();
                if (mNovelFile != null) {
                    try {
                        mNovelFile.close();
                    } catch (IOException e) {
                        LOG.debug(e);
                    }
                }
            }
        });

        return mBook;
    }

    private NovelDetails readDetails() throws JemException {
        NovelDetails details = mDbReader.fetchDetails();
        mBook.setAttribute(Book.TITLE, details.getName());
        mBook.setAttribute(Book.AUTHOR, details.getAuthor());
        mBook.setAttribute(Book.DATE, details.getExpired());
        mBook.setAttribute("update_time", details.getUpdated());
        return details;
    }

    @Override
    public void watch(ChapterItem item) throws JemException {
        Chapter chapter = new Chapter(item.getTitle());
        chapter.setAttribute("update_time", item.getUpdated());

        FileObject fb;
        try {
            fb = FileFactory.fromBlock("chapter-" + item.getId() + ".txt",
                    mNovelFile, item.getStartIndex(), item.getEndIndex() - item.getStartIndex() + 1, null);
        } catch (IOException e) {
            throw new JemException(e);
        }
        chapter.setSource(TextFactory.fromFile(fb, TEXT_ENCODING));

        mBook.append(chapter);
    }

    private void loadDbReader() throws JemException {
        Enumeration<URL> urls = JemUtilities.getResources(DB_READER_CONFIG);
        if (urls == null) {
            return;
        }

        if (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            try {
                String name = IOUtils.toString(url).trim();
                Class<?> clazz = Class.forName(name);
                if (NovelDbReader.class.isAssignableFrom(clazz)) {
                    mDbReader = (NovelDbReader) clazz.newInstance();
                } else {
                    throw new JemException("Invalid NovelDbReader: "+name);
                }
            } catch (IOException e) {
                LOG.debug("no NovelDbReader found", e);
            } catch (ClassNotFoundException e) {
                LOG.debug("no NovelDbReader found", e);
            } catch (InstantiationException e) {
                LOG.debug("no NovelDbReader found", e);
            } catch (IllegalAccessException e) {
                LOG.debug("no NovelDbReader found", e);
            }
        }
    }
}
