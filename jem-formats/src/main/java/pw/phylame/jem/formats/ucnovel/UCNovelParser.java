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

import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.formats.common.CommonParser;
import pw.phylame.jem.formats.util.BufferedRandomAccessFile;
import pw.phylame.jem.formats.util.ExceptionFactory;
import pw.phylame.jem.formats.util.ParserException;
import pw.phylame.jem.formats.util.text.TextUtils;
import pw.phylame.jem.util.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;

public class UCNovelParser extends CommonParser<NovelDbReader, NovelConfig> implements ChapterWatcher {
    public static final String DEFAULT_CONFIG_FILE = "META-INF/pw-jem/ucnovel-reader";
    public static final String CATALOG_FILE_NAME = "com.UCMobile_catalog";
    public static final String TEXT_ENCODING = "UTF-8";

    private NovelConfig mycfg;
    private Book book;
    private HashMap<String, RandomAccessFile> sourceCaches = new HashMap<>();

    public UCNovelParser() {
        super("ucnovel", NovelConfig.CONFIG_SELF, NovelConfig.class);
    }

    @Override
    protected NovelDbReader openFile(File file, NovelConfig config) throws IOException, ParserException {
        if (file.isDirectory()) {
            file = new File(file, CATALOG_FILE_NAME);
        }
        NovelDbReader reader = loadDbReader(config.readerConfig);
        reader.init(file.getPath());
        return reader;
    }

    public static NovelDbReader loadDbReader() throws IOException {
        return loadDbReader(DEFAULT_CONFIG_FILE);
    }

    public static NovelDbReader loadDbReader(String readerConfig) throws IOException {
        Enumeration<URL> urls = IOUtils.getResources(readerConfig, UCNovelParser.class.getClassLoader());
        if (urls == null) {
            throw ExceptionFactory.ioException("ucnovel.parse.notFoundDbReader", readerConfig);
        }

        if (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            try {
                String name = IOUtils.toString(url.openStream(), null).trim();
                Class<?> clazz = Class.forName(name);
                if (NovelDbReader.class.isAssignableFrom(clazz)) {
                    return (NovelDbReader) clazz.newInstance();
                } else {
                    throw ExceptionFactory.ioException("ucnovel.parse.invalidDbReader",
                            NovelDbReader.class.getName());
                }
            } catch (IOException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
                throw ExceptionFactory.ioException(e, "ucnovel.parse.cannotMakeDbReader",
                        NovelDbReader.class.getName());
            }
        }
        throw ExceptionFactory.ioException("ucnovel.parse.notFoundDbReader", readerConfig);
    }

    @Override
    public Book parse(NovelDbReader reader, NovelConfig config) throws IOException, ParserException {
        if (config == null || TextUtils.isEmpty(config.novelId)) {
            throw ExceptionFactory.parserException("ucnovel.parse.noBookId", NovelConfig.NOVEL_ID);
        }
        return fetchBook(reader, config);
    }

    private Book fetchBook(NovelDbReader reader, NovelConfig config) throws ParserException {
        mycfg = config;
        book = new Book();
        sourceCaches.clear();
        reader.fetchChapters(this, fetchInfo(reader, config.novelId));
        book.registerCleanup(new Chapter.Cleanable() {
            @Override
            public void clean(Chapter chapter) {
                for (RandomAccessFile raf : sourceCaches.values()) {
                    try {
                        raf.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                sourceCaches.clear();
            }
        });
        return book;
    }

    // return the table name for the fetched novel
    private String fetchInfo(NovelDbReader reader, String novelId) throws ParserException {
        NovelInfo info = reader.fetchInfo(novelId);
        if (info == null) {
            throw ExceptionFactory.parserException("ucnovel.parse.noSuchNovel", novelId);
        }
        book.setTitle(info.name);
        book.setAuthor(info.author);
        book.setDate(info.expireTime);
        book.setAttribute("update_time", info.updateTime);
        return info.table;
    }

    @Override
    public void watch(ChapterItem item) throws ParserException {
        Chapter chapter = new Chapter(item.title);
        chapter.setAttribute("update_time", item.updateTime);
        if (!item.offlinePath.isEmpty()) {
            try {
                chapter.setContent(TextFactory.forFile(openSource(item), TEXT_ENCODING));
            } catch (IOException e) {
                throw ExceptionFactory.parserException(e, "ucnovel.parse.badChapterItem", item.id);
            }
        }
        book.append(chapter);
    }

    private FileObject openSource(ChapterItem item) throws IOException {
        RandomAccessFile source = sourceCaches.get(item.offlinePath);
        if (source == null) {
            String path;
            if (mycfg.novelFolder != null) {
                path = mycfg.novelFolder + File.separatorChar + mycfg.novelId
                        + File.separatorChar + IOUtils.getFullName(item.offlinePath);
            } else {
                path = item.offlinePath;
            }
            source = new BufferedRandomAccessFile(path, "r");
            sourceCaches.put(item.offlinePath, source);
        }
        return FileFactory.forBlock("chapter-" + item.id + ".txt", source, item.startIndex,
                item.endIndex - item.startIndex, "text/plain");
    }
}
