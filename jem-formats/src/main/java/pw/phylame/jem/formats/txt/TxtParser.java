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

package pw.phylame.jem.formats.txt;

import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.util.FileFactory;
import pw.phylame.jem.util.FileObject;
import pw.phylame.jem.util.TextFactory;
import pw.phylame.jem.formats.common.CommonParser;
import pw.phylame.jem.formats.util.CacheCleaner;
import pw.phylame.jem.formats.util.ParserException;
import pw.phylame.jem.formats.util.text.TextUtils;
import pw.phylame.jem.formats.util.BufferedRandomAccessFile;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.io.FilenameUtils;

/**
 * <tt>Parser</tt> implement for TXT book.
 */
public class TxtParser extends CommonParser<Reader, TxtParseConfig> {
    private static Log LOG = LogFactory.getLog(TxtParser.class);

    private static final String CACHED_TEXT_ENCODING = "UTF-16";

    public TxtParser() {
        super("txt", TxtParseConfig.class, TxtParseConfig.CONFIG_SELF);
    }

    @Override
    protected Reader openInput(File file, TxtParseConfig config)
            throws IOException {
        InputStream stream = new FileInputStream(file);
        String encoding = (config != null) ? config.encoding : TXT.defaultEncoding;
        try {
            return new BufferedReader(new InputStreamReader(stream, encoding));
        } catch (UnsupportedEncodingException e) {
            stream.close();
            throw e;
        }
    }

    @Override
    public Book parse(Reader input, TxtParseConfig config) throws IOException,
            ParserException {
        if (config == null) {
            config = new TxtParseConfig();
        }
        Book book = parse(input, FilenameUtils.getBaseName(source.getPath()), config);
        book.setExtension(TxtInfo.FILE_INFO, new TxtInfo(config.encoding));
        return book;
    }

    public Book parse(Reader reader, String title, TxtParseConfig config)
            throws IOException, ParserException {
        if (config == null) {
            config = new TxtParseConfig();
        }
        Pattern pattern;
        try {
            pattern = Pattern.compile(config.pattern, config.patternFlags);
        } catch (PatternSyntaxException e) {
            throw parserException(e, "txt.parse.invalidPattern", config.pattern);
        }

        // cached file content
        StringBuilder sb = new StringBuilder();
        Object[] objects = cacheContent(reader, sb);
        reader.close();

        BufferedRandomAccessFile source = (BufferedRandomAccessFile) objects[0];
        File cache = (File) objects[1];

        Book book = new Book(title, "");
        String raw = sb.toString();
        try {
            List<Integer> offsets = new LinkedList<Integer>();
            Matcher matcher = pattern.matcher(raw);
            while (matcher.find()) {
                offsets.add(matcher.start());
                book.append(new Chapter(matcher.group()));
            }
            offsets.add(raw.length());

            Iterator<Integer> offsetIterator = offsets.iterator();
            Iterator<Chapter> chapterIterator = book.iterator();

            int start = offsetIterator.next();
            if (start > 0) {    // no formatted head store as intro
                FileObject fb = FileFactory.fromBlock(
                        "text_head.txt", source, 0, start << 1, "text/plain");
                book.setIntro(TextFactory.fromFile(fb, CACHED_TEXT_ENCODING));
            }

            while (chapterIterator.hasNext()) {
                Chapter chapter = chapterIterator.next();
                title = chapter.getTitle();
                if (config.trimChapterTitle) {
                    title = TextUtils.trim(title);
                }
                chapter.setTitle(title);

                int end = offsetIterator.next();
                start += title.length();
                int length = end - start;

                FileObject fb = FileFactory.fromBlock(
                        start + ".txt", source, start << 1, length << 1, "text/plain");
                chapter.setContent(TextFactory.fromFile(fb, CACHED_TEXT_ENCODING));

                start = end;
            }
        } catch (IOException e) {
            source.close();
            if (!cache.delete()) {
                LOG.debug("cannot delete TXT cache: " + cache.getAbsolutePath());
            }
            throw e;
        }
        book.registerCleanup(new CacheCleaner(source, cache));

        System.gc();

        return book;
    }

    private Object[] cacheContent(Reader reader, StringBuilder sb)
            throws IOException {
        File cache = File.createTempFile("txt_", ".tmp");
        Closeable dev = null;
        try {
            OutputStream stream = new FileOutputStream(cache);
            dev = stream;
            Writer writer = new OutputStreamWriter(stream, CACHED_TEXT_ENCODING);
            dev = stream;
            writer = new BufferedWriter(writer);
            dev = writer;

            char[] buf = new char[4096];
            int n;
            while ((n = reader.read(buf)) != -1) {
                writer.write(buf, 0, n);
                sb.append(buf, 0, n);
            }
            writer.close();

            dev = null;
            return new Object[]{new BufferedRandomAccessFile(cache, "r"), cache};
        } catch (IOException e) {
            if (dev != null) {
                dev.close();
                dev = null;
            }
            if (!cache.delete()) {
                LOG.debug("cannot delete TXT cache: " + cache.getAbsolutePath());
            }
            throw e;
        } finally {
            if (dev != null) {
                dev.close();
            }
        }
    }
}
