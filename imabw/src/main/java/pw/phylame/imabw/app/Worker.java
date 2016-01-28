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

package pw.phylame.imabw.app;

import pw.phylame.imabw.app.model.MakerData;
import pw.phylame.imabw.app.model.OpenResult;
import pw.phylame.imabw.app.model.ParseResult;
import pw.phylame.imabw.app.model.ParserData;
import pw.phylame.imabw.app.util.BookUtils;
import pw.phylame.jem.core.Jem;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.core.BookHelper;

import static pw.phylame.jem.formats.util.text.TextUtils.*;

import pw.phylame.jem.util.*;
import pw.phylame.imabw.app.config.JemConfig;
import pw.phylame.imabw.app.ui.dialog.DialogFactory;

import java.io.*;
import java.util.*;
import java.awt.Component;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utilities functions for Imabw.
 */
public class Worker {
    private static Worker instance;

    public static Worker sharedInstance() {
        if (instance == null) {
            instance = new Worker();
        }
        return instance;
    }

    private Worker() {
    }

    private final Log LOG = LogFactory.getLog(Worker.class);
    private final Imabw app = Imabw.sharedInstance();

    public final Map<String, String> formatDescriptions = new HashMap<>();
    // reused book extensions
    private Object[] parserExtensions = null, makerExtensions = null;

    // [desc] (*.xxx *.yyy *.zzz)
    private String getFormatDescription(String[] formats) {
        String key = formats[0];
        String v = formatDescriptions.get(key);
        if (v == null) {
            try {
                v = app.getText("common.format." + key.toLowerCase());
            } catch (MissingResourceException e) {
                app.error("no such format key: " + key);
            }
            if (v == null || v.isEmpty()) {
                v = key.toUpperCase() + app.getText("common.format.suffix");
            }
            formatDescriptions.put(key, v);
        }
        return v + " (*." + String.join(" *.", formats) + ")";
    }

    public FileNameExtensionFilter makeExtensionFilter(Object format) {
        if (format == null) {
            throw new NullPointerException("format");
        }
        String[] extensions;
        if (format instanceof String[]) {
            extensions = (String[]) format;
        } else {
            extensions = new String[]{format.toString()};
        }

        return new FileNameExtensionFilter(getFormatDescription(extensions), extensions);
    }

    private <T> boolean contains(T[] ary, T o) {
        for (T t : ary) {
            if (t.equals(o)) {
                return true;
            }
        }
        return false;
    }

    public Object[] makeExtensionFilters(Object[] formats, Object initFormat) {
        FileFilter[] filters = new FileFilter[formats.length];
        FileFilter initFilter = null;
        boolean isEqual = false;
        int i = 0;
        for (Object format : formats) {
            String[] extensions;
            if (format instanceof String) {
                extensions = new String[]{(String) format};
                if (initFormat != null) {
                    isEqual = initFormat.equals(format);
                }
            } else if (format instanceof String[]) {
                extensions = (String[]) format;
                if (initFormat != null) {
                    if (initFormat instanceof String) {
                        isEqual = contains(extensions, initFormat);
                    } else if (initFormat instanceof String[]) {
                        isEqual = Arrays.equals(extensions, (String[]) initFormat);
                    }
                }
            } else {
                continue;
            }
            filters[i++] = makeExtensionFilter(extensions);
            if (isEqual) {
                initFilter = filters[i - 1];
            }
        }
        return new Object[]{filters, initFilter};
    }

    public OpenResult selectFile(Component parent, String title, File initFile,
                                 Object[] formats, Object initFormat,
                                 boolean acceptAll, boolean openMode, boolean multiple) {
        FileFilter[] filters = null;
        FileFilter initFilter = null;
        if (formats != null) {
            Object[] objects = makeExtensionFilters(formats, initFormat);
            filters = (FileFilter[]) objects[0];
            initFilter = (FileFilter) objects[1];
        }
        if (openMode) {
            return DialogFactory.openFile(parent, title, multiple, initFile, null,
                    acceptAll, filters, initFilter);
        } else {
            return DialogFactory.saveFile(parent, title, initFile, null, true,
                    acceptAll, filters, initFilter);
        }
    }

    public OpenResult selectOpenFile(Component parent, String title, File initFile,
                                     Object[] formats, Object initFormat,
                                     boolean acceptAll, boolean multiple) {
        return selectFile(parent, title, initFile, formats, initFormat,
                acceptAll, true, multiple);
    }

    public OpenResult selectSaveFile(Component parent, String title,
                                     File initFile, Object[] formats,
                                     Object initFormat, boolean acceptAll) {
        return selectFile(parent, title, initFile, formats, initFormat,
                acceptAll, false, false);
    }

    public OpenResult selectOpenImage(Component parent, String title) {
        String[] jpg = {"jpg", "jpeg"};
        Object[] formats = {jpg, "png", "gif", "bmp"};
        return selectOpenFile(parent, title, null, formats, jpg, true, false);
    }

    public OpenResult selectSaveImage(Component parent, String title) {
        String[] jpg = {"jpg", "jpeg"};
        Object[] formats = {jpg, "png", "gif", "bmp"};
        return selectSaveFile(parent, title, null, formats, jpg, false);
    }

    private Object[] getParserExtensions() {
        if (parserExtensions == null) {
            HashSet<Object> results = new HashSet<>();
            for (String name : BookHelper.supportedParsers()) {
                String[] extensions = BookHelper.extensionsOfName(name);
                if (extensions.length == 1) {
                    results.add(extensions[0]);
                } else {
                    results.add(extensions);
                }
            }
            parserExtensions = results.toArray();
        }
        return parserExtensions;
    }

    private Object[] getMakerExtensions() {
        if (makerExtensions == null) {
            HashSet<Object> results = new HashSet<>();
            for (String name : BookHelper.supportedMakers()) {
                String[] extensions = BookHelper.extensionsOfName(name);
                if (extensions.length == 1) {
                    results.add(extensions[0]);
                } else {
                    results.add(extensions);
                }
            }
            makerExtensions = results.toArray();
        }
        return makerExtensions;
    }

    private OpenResult selectBookFile(Component parent, String title,
                                      File initFile, String initFormat,
                                      boolean openMode, boolean multiple) {
        if (initFormat == null || initFormat.isEmpty()) {
            if (initFile != null) {
                initFormat = IOUtils.getExtension(initFile.getPath());
                if (initFormat.isEmpty()) {
                    initFormat = Jem.PMAB;
                }
            } else {
                initFormat = Jem.PMAB;
            }
        }

        if (openMode) {
            return selectOpenFile(parent, title, initFile, getParserExtensions(), initFormat, true, multiple
            );
        } else {
            return selectSaveFile(parent, title, initFile,
                    getMakerExtensions(), initFormat, false);
        }
    }

    public OpenResult selectOpenBook(Component parent, String title,
                                     File initFile, String initFormat, boolean multiple) {
        return selectBookFile(parent, title, initFile, initFormat, true, multiple);
    }

    /**
     * Selects file for writing book.
     *
     * @param parent     parent of dialog
     * @param title      title of dialog
     * @param initFormat wanted format, if <tt>null</tt>, user need choose
     *                   one supported format
     * @return the <tt>OpenResult</tt> contained selected file and format
     */
    public OpenResult selectSaveBook(Component parent, String title,
                                     File initFile, String initFormat) {
        return selectBookFile(parent, title, initFile, initFormat, false, false);
    }

    public String formatFileSize(long size) {
        String suffix = app.getText("common.file.sizeSuffix");
        if (size < 0x400) {
            return size + " " + suffix;
        } else if (size < 0x100000) {
            return String.format("%.2f K" + suffix, size / 1024.0);
        } else if (size < 0x40000000) {
            return String.format("%.2f M" + suffix, size / 1024.0 / 1024.0);
        } else {
            return String.format("%.2f G" + suffix, size / 1024.0 / 1024.0 / 1024.0);
        }
    }

    public String readableTypeName(String type) {
        return app.getOptionalText("common.item.type." + type, capitalized(type));
    }

    public String readableItemType(Object value) {
        return readableTypeName((value == null) ? "str" : Jem.typeOfVariant(value).toLowerCase());
    }

    public String readableItemValue(Object value) {
        if (value == null) {
            throw new NullPointerException();
        }
        String type = Jem.typeOfVariant(value);
        switch (type) {
            case "str":
                return value.toString();
            case "text":
                return fetchText((TextObject) value, "");
            case "datetime":
                return formatDate((Date) value, "yy-M-d");
            case "locale":
                return ((Locale) value).getDisplayName();
            case "bool":
                return app.getText("common.item.value." + value);
            default:
                return value.toString();
        }
    }

    /**
     * Creates new book.
     *
     * @param parent dialog parent
     * @param name   name of book
     * @param title  title of new book dialog
     * @return a new book
     */
    public Book newBook(Component parent, String title, String name) {
        if (name == null || name.isEmpty()) {
            name = DialogFactory.inputText(parent, title,
                    app.getText("d.newBook.inputTip"),
                    app.getText("d.newBook.defaultTitle"), false, false);
            if (name == null) {
                return null;
            }
        }

        Book book = new Book(name);
        BookUtils.addAttributes(book);

        book.append(new Chapter(app.getText("dialog.newChapter.defaultTitle")));
        return book;
    }

    /**
     * Creates a new chapter node and appends it to <tt>parentNode</tt> if presents.
     *
     * @param parent parent component of input dialog
     * @param title  dialog title of input dialog
     * @return the new node
     */
    public Chapter newChapter(Component parent, String title) {
        String name = DialogFactory.inputText(parent, title,
                app.getText("dialog.newChapter.inputTip"),
                app.getText("dialog.newChapter.defaultTitle"), false, false);
        if (name == null) {
            return null;
        }
        return new Chapter(name);
    }

    public void storeToFile(FileObject fb, File file) throws IOException {
        try (InputStream in = fb.openStream(); FileOutputStream out = new FileOutputStream(file)) {
            IOUtils.copy(in, out, -1);
            fb.reset();
        }
    }

    public void storeToFile(TextObject tb, File file, String encoding) throws IOException {
        IOUtils.write(file, fetchText(tb, ""), encoding);
    }

    public File duplicateToCache(File source) throws IOException {
        File cache = null;
        try {
            cache = File.createTempFile("imabw_", ".tmp");
            IOUtils.copyFile(source, cache);
        } catch (IOException ex) {
            deleteCache(cache);
            throw ex;
        }
        return cache;
    }

    public void deleteCache(File cache) {
        if (cache != null && !cache.delete()) {
            LOG.error("failed to delete cache: " + cache.getAbsolutePath());
        }
    }

    private class CacheDeleter implements Chapter.Cleanable {
        private File cache;

        public CacheDeleter(File cache) {
            this.cache = cache;
        }

        @Override
        public void clean(Chapter chapter) {
            deleteCache(cache);
        }
    }

    public ParseResult readBook(ParserData pd) throws Exception {
        File input, cache = null;
        if (pd.useCache) {
            cache = duplicateToCache(pd.file);
            input = cache;
        } else {
            input = pd.file;
        }

        Book book;
        try {
            book = Jem.readBook(input, pd.format, pd.arguments);
        } catch (Exception ex) {
            // file is cached
            deleteCache(cache);
            throw ex;
        }
        if (cache != null) {
            book.registerCleanup(new CacheDeleter(cache));
        }
        return new ParseResult(book, cache, pd);
    }

    public MakerData makeMakerData(Component parent, String title, Book book,
                                   File output, String format, Map<String, Object> arguments) {
        if (output == null || format == null) {
            OpenResult od = selectSaveBook(parent, title, output, format);
            if (od == null) {
                return null;
            }
            output = od.getFile();
            format = od.getFormat();
        }
        if (arguments == null) {
            arguments = getMakeArguments(parent, format);
            if (arguments == null) {
                return null;
            }
        }
        return new MakerData(book, output, format, arguments);
    }

    public ParserData makeParserData(Component parent, String title, File file,
                                     String format, Map<String, Object> arguments) {
        if (file == null) {
            OpenResult od = selectOpenBook(parent, title, null, format, false);
            if (od == null) {   // no selection
                return null;
            }
            file = od.getFile();
            format = od.getFormat();
        } else {
            format = null;
        }
        if (arguments == null) {
            arguments = getParseArguments(parent, format);
            if (arguments == null) {   // cancel config arguments
                return null;
            }
        }
        return new ParserData(file, format, arguments, false);
    }

    public void writeBook(MakerData md) throws Exception {
        Jem.writeBook(md.book, md.file, md.format, md.arguments);
    }

    public void showOpenError(Component parent, String title, ParserData data,
                              Throwable err) {
        String str = app.getText("d.openBook.failed", data.file, transErrorMessage(err));
        DialogFactory.viewException(parent, title, str, err);
    }

    public void showSaveError(Component parent, String title, MakerData data,
                              Throwable e) {
        String str = app.getText("d.saveBook.failed", data.file, transErrorMessage(e));
        DialogFactory.viewException(parent, title, str, e);
    }

    public String transErrorMessage(Throwable err) {
        String str;
        if (err instanceof UnsupportedFormatException) {
            str = app.getText("dialog.error.unsupportedFormat",
                    ((UnsupportedFormatException) err).getFormat());
        } else if (err instanceof FileNotFoundException) {
            str = app.getText("dialog.error.fileNotExists");
        } else {
            str = err.getLocalizedMessage();
        }
        return str;
    }

    public Map<String, Object> getDefaultParseArguments(String format) {
        return new HashMap<>();
    }

    public Map<String, Object> getDefaultMakerArguments(String format) {
        return new HashMap<>();
    }

    public Map<String, Object> getParseArguments(Component parent, String format) {
        return new HashMap<>();
    }

    public Map<String, Object> getMakeArguments(Component parent, String format) {
        return new HashMap<>();
    }
}
