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

package pw.phylame.jem.formats.util;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

import pw.phylame.jem.core.Jem;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.core.BookHelper;
import pw.phylame.jem.formats.txt.TxtParseConfig;
import pw.phylame.jem.util.IOUtils;
import pw.phylame.jem.util.JemException;

/**
 * Utilities for debug parser and maker.
 */
public final class DebugUtils {
    private DebugUtils() {
    }

    public static Book parseFile(String path, Map<String, Object> kw) {
        String format = BookHelper.nameOfExtension(IOUtils.getExtension(path));
        if (format == null) {
            System.err.println("unsupported format: " + path);
            return null;
        }
        return parseFile(path, format, kw);
    }

    public static Book parseFile(String path, String format, Map<String, Object> kw) {
        Book book = null;
        try {
            book = Jem.readBook(new File(path), format, kw);
        } catch (IOException | JemException e) {
            e.printStackTrace();
        }
        return book;
    }

    public static void makeFile(Book book, String path, Map<String, Object> kw) {
        String format = BookHelper.nameOfExtension(IOUtils.getExtension(path));
        if (format == null) {
            System.err.println("unsupported format: " + path);
            return;
        }
        makeFile(book, path, format, kw);
    }

    public static void makeFile(Book book, String path, String format, Map<String, Object> kw) {
        try {
            Jem.writeBook(book, new File(path), format, kw);
        } catch (IOException | JemException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Object> makeConfig(Object... objects) {
        int size = objects.length;
        if (size % 2 != 0) {
            throw new IllegalArgumentException("size of objects must % 2 = 0");
        }
        HashMap<String, Object> map = new HashMap<>();
        for (int ix = 0; ix < size; ix += 2) {
            map.put(objects[ix].toString(), objects[ix + 1]);
        }
        return map;
    }

    public static void printAttributes(Chapter ch) {
        System.out.println(ch.attributeCount() + " attributes of " + ch.getTitle());
        for (Map.Entry<String, Object> entry : ch.attributeEntries()) {
            System.out.println(" " + entry.getKey() + "=" + Jem.formatVariant(entry.getValue()));
        }
    }

    public static void printExtension(Book book) {
        System.out.println(book.extensionCount() + " extensions of " + book.getTitle());
        for (Map.Entry<String, Object> entry : book.extensionEntries()) {
            System.out.println(" " + entry.getKey() + "=" + Jem.formatVariant(entry.getValue()));
        }
    }

    public static void printTOC(Chapter ch) {
        printTOC(ch, "", " ");
    }

    public static void printTOC(Chapter ch, String prefix) {
        printTOC(ch, prefix, " ");
    }

    public static void printTOC(Chapter ch, String prefix, String separator) {
        System.out.println(prefix + ch.getTitle());
        int order = 1;
        for (Chapter sub : ch) {
            printTOC(sub, prefix + order + separator, separator);
            ++order;
        }
    }

    public static void main(String[] args) {
        Map<String, Object> kw = makeConfig(
                TxtParseConfig.TEXT_ENCODING, "GBK"
                , TxtParseConfig.CHAPTER_PATTERN, "^第[一二三四五六七八九十百零]+[(卷)(章\\s)].*\\r\\n"
        );
        String path = "D:\\tmp\\辽东钉子户.txt";
        Book book = parseFile(path, kw);
        printTOC(book);
        try {
            Jem.writeBook(book, new File("D:/tmp/a.pmab"), "pmab", null);
        } catch (IOException | JemException e) {
            e.printStackTrace();
        }
    }
}
