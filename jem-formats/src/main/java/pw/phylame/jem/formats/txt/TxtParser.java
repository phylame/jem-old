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

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.util.IOUtils;
import pw.phylame.jem.util.TextFactory;
import pw.phylame.jem.util.FileFactory;
import pw.phylame.jem.formats.common.CommonParser;
import pw.phylame.jem.formats.util.CacheCleaner;
import pw.phylame.jem.formats.util.ParserException;
import pw.phylame.jem.formats.util.ExceptionFactory;
import pw.phylame.jem.formats.util.text.TextUtils;
import pw.phylame.jem.formats.util.BufferedRandomAccessFile;

/**
 * <tt>Parser</tt> implement for TXT book.
 */
public class TxtParser extends CommonParser<Reader, TxtParseConfig> {
    private static final String CACHE_ENCODING = "UTF-16";

    public TxtParser() {
        super("txt", TxtParseConfig.CONFIG_SELF, TxtParseConfig.class);
    }

    @Override
    protected Reader openFile(File file, TxtParseConfig config) throws IOException, ParserException {
        FileInputStream stream = new FileInputStream(file);
        try {
            return new BufferedReader(new InputStreamReader(stream, config.encoding));
        } catch (UnsupportedEncodingException e) {
            stream.close();
            throw e;
        }
    }

    @Override
    public Book parse(Reader input, TxtParseConfig config) throws IOException, ParserException {
        if (config == null) {
            config = new TxtParseConfig();
        }
        String title = (source != null) ? IOUtils.getBaseName(source.getPath()) : "";
        Book book = parse(input, title, config);
        book.setExtension(TxtInfo.FILE_INFO, new TxtInfo(config.encoding));
        return book;
    }

    public Book parse(Reader reader, String title, TxtParseConfig config) throws IOException, ParserException {
        if (config == null) {
            config = new TxtParseConfig();
        }
        Pattern pattern;
        try {
            pattern = Pattern.compile(config.pattern, config.patternFlags);
        } catch (PatternSyntaxException e) {
            throw ExceptionFactory.parserException(e, "txt.parse.invalidPattern", config.pattern);
        }

        // cached file content
        StringBuilder sb = new StringBuilder();
        Object[] objects = cacheContent(reader, sb);
        reader.close();

        RandomAccessFile source = (RandomAccessFile) objects[0];
        File cache = (File) objects[1];

        Book book = new Book(title, "");
        String raw = sb.toString();
        try {
            Matcher matcher = pattern.matcher(raw);

            int prevOffset, firstOffset;
            FileFactory.BlockFile fb;
            if (matcher.find()) {
                firstOffset = prevOffset = matcher.start();
                title = matcher.group();
                if (config.trimChapterTitle) {
                    prevOffset += title.length();
                }
                fb = FileFactory.forBlock("0.txt", source, prevOffset << 1, 0, TXT.MIME_PLAIN_TEXT);
                book.append(new Chapter(TextUtils.trimmed(title), TextFactory.forFile(fb, CACHE_ENCODING)));
            } else {
                source.close();
                if (!cache.delete()) {
                    throw new IOException("Failed to delete TXT cache: " + cache);
                }
                return book;
            }
            while (matcher.find()) {
                int offset = matcher.start();
                fb.size = (offset - prevOffset) << 1;

                title = matcher.group();
                if (config.trimChapterTitle) {
                    offset += title.length();
                }

                fb = FileFactory.forBlock(book.size() + ".txt", source, offset << 1, 0, TXT.MIME_PLAIN_TEXT);
                prevOffset = offset;
                book.append(new Chapter(TextUtils.trimmed(title), TextFactory.forFile(fb, CACHE_ENCODING)));
            }
            fb.size = (raw.length() - prevOffset) << 1;

            if (firstOffset > 0) {    // no formatted head store as intro
                fb = FileFactory.forBlock("head.txt", source, 0, firstOffset << 1, TXT.MIME_PLAIN_TEXT);
                book.setIntro(TextFactory.forFile(fb, CACHE_ENCODING));
            }
        } catch (IOException e) {
            source.close();
            if (!cache.delete()) {
                throw new IOException("Failed to delete TXT cache: " + cache);
            }
            throw e;
        }
        book.registerCleanup(new CacheCleaner(source, cache));

        System.gc();

        return book;
    }

    private Object[] cacheContent(Reader reader, StringBuilder sb) throws IOException {
        File cache = File.createTempFile("jem_txt_", ".tmp");
        Closeable closeable = null;
        try {
            OutputStream stream = new FileOutputStream(cache);
            closeable = stream;
            Writer writer = new OutputStreamWriter(stream, CACHE_ENCODING);
            closeable = stream;
            writer = new BufferedWriter(writer);
            closeable = writer;

            char[] buf = new char[4096];
            int n;
            while ((n = reader.read(buf)) != -1) {
                writer.write(buf, 0, n);
                sb.append(buf, 0, n);
            }
            writer.close();
            closeable = null;
            return new Object[]{new BufferedRandomAccessFile(cache, "r"), cache};
        } catch (IOException e) {
            IOUtils.closeQuietly(closeable);
            if (!cache.delete()) {
                throw new IOException("Failed to delete TXT cache: " + cache);
            }
            throw e;
        }
    }
}
