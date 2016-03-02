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

package pw.phylame.imabw.app.util;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pw.phylame.imabw.app.Imabw;
import pw.phylame.imabw.app.config.JemConfig;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.formats.util.FileInfo;
import pw.phylame.jem.util.FileFactory;
import pw.phylame.jem.util.TextObject;

import static pw.phylame.imabw.app.ui.dialog.DialogFactory.*;
import static pw.phylame.jem.formats.util.text.TextUtils.*;

/**
 * Utilities for book and chapter.
 */
public final class BookUtils {
    private static final Imabw app = Imabw.sharedInstance();
    private static final Log Log = LogFactory.getLog(BookUtils.class);

    private BookUtils() {
    }

    public static String chooseGenre(Component parent, String title, String defaultGenre) {
        String tipText = app.getText("dialog.chooseGenre.tip");
        String[] genres = JemConfig.sharedInstance().getGenres().split(";");
        return chooseItem(parent, title, tipText, genres,
                defaultGenre.isEmpty() ? null : defaultGenre, true);
    }

    public static String chooseState(Component parent, String title, String defaultState) {
        String tipText = app.getText("dialog.chooseState.tip");
        String[] states = JemConfig.sharedInstance().getStates().split(";");
        return chooseItem(parent, title, tipText, states,
                defaultState.isEmpty() ? null : defaultState, true);
    }

    public static Map<String, Object> dumpAttributes(Chapter chapter) {
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, Object> entry : chapter.attributeEntries()) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    public static Map<String, Object> dumpExtensions(Book book) {
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, Object> entry : book.extensionEntries()) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getVariant(Map<String, Object> map, String key, T defaultValue, Class<T> type) {
        Object v = map.get(key);
        return v != null && type.isInstance(v) ? (T) v : defaultValue;
    }

    public static void addAttributes(Book book) {
        String[] names = {Book.AUTHOR, Book.COVER, Book.DATE, Book.LANGUAGE,
                Book.VENDOR, Book.RIGHTS, Book.STATE};
        for (String name : names) {
            if (book.hasAttribute(name)) {
                continue;
            }
            Object value = defaultOfAttribute(name);
            if (value != null) {
                book.setAttribute(name, value);
            }
        }
    }

    public static Object defaultOfAttribute(String name) {
        switch (name) {
            case Book.COVER: {
                String path = JemConfig.sharedInstance().getAttribute(Chapter.COVER);
                if (path != null && !(path = path.trim()).isEmpty()) {
                    try {
                        return FileFactory.forFile(new File(path), null);
                    } catch (IOException e) {
                        Log.error("cannot load default book cover: " + path, e);
                    }
                }
                return null;
            }
            case Book.DATE:
                return new Date();
            case Book.LANGUAGE:
                return Locale.getDefault();
            case Book.WORDS:
                return 0;
            default:
                return JemConfig.sharedInstance().getAttribute(name);
        }
    }

    public static String nameOfAttribute(String name) {
        name = name.toLowerCase();
        return app.getOptionalText("common.attributes.name." + name, capitalized(name));
    }

    public static String[] getFileInfo(Book book) {
        Object o = book.getExtension(FileInfo.FILE_INFO, null);
        if (o instanceof FileInfo) {
            FileInfo fileInfo = (FileInfo) o;
            Set<Map.Entry<String, Object>> entries = fileInfo.entries();
            String[] strings = new String[entries.size() * 2];
            int ix = 0;
            for (Map.Entry<String, Object> entry : entries) {
                strings[ix++] = fileInfo.localizedKey(entry.getKey());
                strings[ix++] = fileInfo.localizedValue(entry.getValue());
            }
            return strings;
        } else {
            return null;
        }
    }

    public static String contentOfChapter(Chapter chapter, String defaultValue) {
        TextObject content = chapter.getContent();
        return content != null ? fetchText(content, defaultValue) : defaultValue;
    }
}
