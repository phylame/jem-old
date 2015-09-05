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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.core.Parser;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.util.*;
import pw.phylame.jem.formats.util.ParserException;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * <tt>Parser</tt> implement for TXT book.
 */
public class TxtParser implements Parser {
    private static Log LOG = LogFactory.getLog(TxtParser.class);

    private static final String CACHED_TEXT_ENCODING = "UTF-16";

    public static final String KEY_CHAPTER_PATTERN = "chapter_pattern";
    public static final String KEY_TEXT_ENCODING = "txt_encoding";

    public static String DEFAULT_CHAPTER_PATTERN = null;

    private static void loadConfig() {
        Properties prop = new Properties();
        InputStream in = TxtParser.class.getResourceAsStream("txt.properties");
        if (in != null) {
            try {
                prop.load(in);
                DEFAULT_CHAPTER_PATTERN = prop.getProperty("chapter_pattern");
            } catch (IOException e) {
                LOG.debug("failed to load TXT parser configurations", e);
            }
        }
    }

    static {
        loadConfig();
    }

    private RandomAccessFile source = null;

    @Override
    public String getName() {
        return "txt";
    }

    @Override
    public Book parse(File file, Map<String, Object> kw)
            throws IOException, JemException {
        String encoding = System.getProperty("file.encoding"),
                chapterPattern = DEFAULT_CHAPTER_PATTERN;
        if (kw != null && kw.size() > 0) {
            Object o = kw.get(KEY_TEXT_ENCODING);
            if (o instanceof String) {
                encoding = (String) o;
            }
            o = kw.get(KEY_CHAPTER_PATTERN);
            if (o instanceof String) {
                chapterPattern = (String) o;
            }
        }

        Reader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), encoding));
        return parse(reader, FilenameUtils.getBaseName(file.getPath()), chapterPattern);
    }

    public Book parse(Reader reader, String title, String chapterPattern)
            throws IOException, JemException {
        Pattern pattern;
        try {
            pattern = Pattern.compile(chapterPattern, Pattern.MULTILINE);
        } catch (PatternSyntaxException e) {
            throw new ParserException(
                    "Invalid chapter pattern: "+chapterPattern, e, getName());
        }

        Book book = new Book(title, "");
        final File cache = File.createTempFile("TXT", ".tmp");
        book.registerCleanup(new Chapter.Cleanable() {
            @Override
            public void clean(Chapter part) {
                if (source != null) {
                    try {
                        source.close();
                        if (! cache.delete()) {
                            LOG.debug("cannot delete TXT cached file: "+cache.getPath());
                        }
                    } catch (IOException e) {
                        LOG.debug("cannot close TXT cached file: "+cache.getPath());
                    }
                }
            }
        });

        // cached file content
        StringBuilder sb = new StringBuilder();
        try {
            char[] buf = new char[1024];
            Writer writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(cache), CACHED_TEXT_ENCODING));
            int n;
            while ((n=reader.read(buf)) != -1) {
                writer.write(buf, 0, n);
                sb.append(buf, 0, n);
            }
            writer.close();
            source = new RandomAccessFile(cache, "r");
        } catch (IOException ex) {
            book.cleanup();
            throw ex;
        }

        String raw = sb.toString();

        List<Integer> offsets = new LinkedList<Integer>();
        Matcher matcher = pattern.matcher(raw);
        while (matcher.find()) {
            offsets.add(matcher.start());
            book.append(new Chapter(matcher.group()));
        }
        offsets.add(raw.length());

        Iterator<Integer> offsetIt = offsets.iterator();
        Iterator<Chapter> partIt = book.iterator();

        int start = offsetIt.next();
        if (start > 0) {    // no formatted head
            FileObject fb = FileFactory.fromBlock("text_head.txt",
                    source, 0, start*2, null);
            book.setIntro(TextFactory.fromFile(fb, CACHED_TEXT_ENCODING));
        }

        while (partIt.hasNext()) {
            Chapter part = partIt.next();
            title = part.getTitle();
            int end = offsetIt.next();
            start += title.length();
            int length = end - start;

            part.setTitle(title.trim());
            FileObject fb = FileFactory.fromBlock(start + ".txt",
                    source, start * 2, length * 2, null);
            part.setSource(TextFactory.fromFile(fb, CACHED_TEXT_ENCODING));

            start = end;
        }

        raw = null;
        System.gc();

        return book;
    }
}
