/*
 * Copyright 2015 Peng Wan <phylame@163.com>
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

package pw.phylame.jem.imabw.app;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.BookHelper;
import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.core.Jem;
import pw.phylame.jem.imabw.app.config.JemConfig;
import pw.phylame.jem.imabw.app.data.*;
import pw.phylame.jem.imabw.app.ui.dialog.DialogFactory;
import pw.phylame.jem.util.FileFactory;
import pw.phylame.jem.util.FileObject;
import pw.phylame.jem.util.UnsupportedFormatException;
import pw.phylame.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.awt.Component;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Utilities functions for Imabw.
 */
public class Worker {
    private static Worker       instance = null;

    public static Worker getInstance() {
        if (instance == null) {
            instance = new Worker();
        }
        return instance;
    }

    private Worker() {
    }

    private Log                 LOG = LogFactory.getLog(Worker.class);
    private Imabw               app = Imabw.getInstance();

    public Map<String, String>  formatDescriptions = new HashMap<>();
    private Object[]            bookExtensions = null;

    // [desc] (*.xxx *.yyy *.zzz)
    private String getFormatDescription(String[] formats) {
        String v;
        try {
            v = app.getText("Common.Format." + formats[0].toUpperCase());
        } catch (MissingResourceException e) {
            v = formatDescriptions.get(formats[0]);
        }
        if (v == null || v.isEmpty()) {
            v = formats[0].toUpperCase() + app.getText("Common.Format.Suffix");
        }
        return v + " (*." + StringUtils.join(formats, " *.") + ")";
    }

    public FileNameExtensionFilter makeExtensionFilter(Object format) {
        if (format == null) {
            throw new NullPointerException("format");
        }
        String[] ext;
        if (format instanceof String[]) {
            ext = (String[]) format;
        } else {
            ext = new String[]{format.toString()};
        }

        return new FileNameExtensionFilter(getFormatDescription(ext), ext);
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
        int          i       = 0;
        for (Object format : formats) {
            String[] ext;
            if (format instanceof String) {
                ext = new String[]{(String) format};
                if (initFormat != null) {
                    isEqual = initFormat.equals(format);
                }
            } else if (format instanceof String[]) {
                ext = (String[]) format;
                if (initFormat != null) {
                    if (initFormat instanceof String) {
                        isEqual = contains(ext, initFormat);
                    } else if (initFormat instanceof String[]) {
                        isEqual = Arrays.equals(ext, (String[]) initFormat);
                    }
                }
            } else {
                continue;
            }
            filters[i++] = makeExtensionFilter(ext);
            if (isEqual) {
                initFilter = filters[i-1];
            }
        }
        return new Object[]{filters, initFilter};
    }

    public OpenResult selectOpenFile(Component parent, String title, boolean multiple,
                                   File initFile, Object[] formats, Object initFormat,
                                     boolean acceptAll) {
        FileFilter[] filters;
        FileFilter initFilter;
        if (formats != null) {
            Object[] rev = makeExtensionFilters(formats, initFormat);
            filters = (FileFilter[])rev[0];
            initFilter = (FileFilter)rev[1];
        } else {
            filters = null;
            initFilter = null;
        }
        return DialogFactory.openFile(parent, title, multiple, initFile, null,
                acceptAll, filters, initFilter);
    }

    public OpenResult selectSaveFile(Component parent, String title,
                                   File initFile, Object[] formats, Object initFormat,
                                     boolean acceptAll) {
        FileFilter[] filters;
        FileFilter initFilter;
        if (formats != null) {
            Object[] rev = makeExtensionFilters(formats, initFormat);
            filters = (FileFilter[])rev[0];
            initFilter = (FileFilter)rev[1];
        } else {
            filters = null;
            initFilter = null;
        }
        return DialogFactory.saveFile(parent, title, initFile, null, true,
                acceptAll, filters, initFilter);
    }

    public OpenResult selectOpenImage(Component parent, String title) {
        String[] jpg = {"jpg", "jpeg"};
        Object[] formats = {jpg, "png", "gif", "bmp"};
        return selectOpenFile(parent, title, false, null, formats, jpg, true);
    }

    public OpenResult selectSaveImage(Component parent, String title) {
        String[] jpg = {"jpg", "jpeg"};
        Object[] formats = {jpg, "png", "gif", "bmp"};
        return selectSaveFile(parent, title, null, formats, jpg, false);
    }

    private Object[] getSupportedBookExtensions() {
        if (bookExtensions == null) {
            HashSet<Object> extensions = new HashSet<>();
            for (String name : BookHelper.supportedParsers()) {
                String[] ext = BookHelper.getExtensions(name);
                if (ext.length == 1) {
                    extensions.add(ext[0]);
                } else {
                    extensions.add(ext);
                }
            }
            bookExtensions = extensions.toArray(new Object[0]);
        }
        return bookExtensions;
    }

    private OpenResult selectBookFile(Component parent, String title,
                                      boolean multiple, boolean openBook,
                                      File initFile, String initFormat) {
        if (initFormat == null || initFormat.isEmpty()) {
            if (initFile != null) {
                initFormat = FilenameUtils.getExtension(initFile.getPath());
                if (initFormat.isEmpty()) {
                    initFormat = Jem.PMAB_FORMAT;
                }
            } else {
                initFormat = Jem.PMAB_FORMAT;
            }
        }

        if (openBook) {
            return selectOpenFile(parent, title, multiple, initFile,
                    getSupportedBookExtensions(), initFormat, true);
        } else {
            return selectSaveFile(parent, title, initFile,
                    BookHelper.supportedMakers(), initFormat, false);
        }
    }

    public OpenResult selectOpenBook(Component parent, String title, boolean multiple,
                                     File initFile, String initFormat) {
        return selectBookFile(parent, title, multiple, true, initFile, initFormat);
    }

    /**
     * Selects file for writing book.
     * @param parent parent of dialog
     * @param title title of dialog
     * @param initFormat wanted format, if <tt>null</tt>, user need choose
     *               one supported format
     * @return the <tt>OpenResult</tt> contained selected file and format
     */
    public OpenResult selectSaveBook(Component parent, String title,
                                     File initFile, String initFormat) {
        return selectBookFile(parent, title, false, false, initFile, initFormat);
    }

    public String formatFileSize(long size) {
        if (size < 0x400) {
            return size + " b";
        } else if (size < 0x100000) {
            return String.format("%.2f Kb", size / 1024.0);
        } else if (size < 0x40000000) {
            return String.format("%.2f Mb", size / 1024.0 / 1024.0);
        } else {
            return String.format("%.2f Gb", size / 1024.0 / 1024.0 / 1024.0);
        }
    }

    private void addAttributes(Book book) {
        JemConfig jemConfig = JemConfig.getInstance();
        if (book.getAttribute(Book.COVER) == null) {
            FileObject fb = null;
            String path = jemConfig.getAttributeValue(Chapter.COVER);
            if (path != null && !path.trim().isEmpty()) {
                try {
                    fb = FileFactory.fromFile(new File(path), null);
                } catch (IOException e) {
                    LOG.debug("cannot load default book cover: "+path, e);
                }
            } else {
                URL url = Worker.class.getResource("/cover.png");   // in jem-formats
                if (url != null) {
                    fb = FileFactory.fromURL(url, null);
                }
            }

            if (fb != null) {
                book.setAttribute(Book.COVER, fb);
            }
        }

        if (book.getAttribute(Book.DATE) == null) {
            book.setAttribute(Book.DATE, new Date());
        }

        if (book.getAttribute(Book.LANGUAGE) == null) {
            book.setAttribute(Book.LANGUAGE, Locale.getDefault().toLanguageTag());
        }

        if (book.getAttribute(Book.VENDOR) == null) {
            book.setAttribute(Book.VENDOR, jemConfig.getAttributeValue(Chapter.VENDOR));
        }

        if (book.getAttribute(Book.RIGHTS) == null) {
            book.setAttribute(Book.RIGHTS, jemConfig.getAttributeValue(Chapter.RIGHTS));
        }
    }

    /**
     * Creates new book.
     * @param parent dialog parent
     * @param name name of book
     * @param title title of new book dialog
     * @return a new book
     */
    public Book newBook(Component parent, String title, String name) {
        if (name == null || name.isEmpty()) {
            name = DialogFactory.inputText(parent, title,
                    app.getText("Dialog.NewBook.InputTip"),
                    app.getText("Dialog.NewBook.BookTitle"), false, false);
            if (name == null) {
                return null;
            }
        }

        Book book = new Book(name, "");
        addAttributes(book);

        book.append(new Chapter(app.getText("Dialog.NewBook.ChapterTitle")));

        return book;
    }

    /**
     * Creates a new chapter node and appends it to <tt>parentNode</tt> if presents.
     * @param parent parent component of input dialog
     * @param title dialog title of input dialog
     * @return the new node
     */
    public Chapter newChapter(Component parent, String title) {
        String name = DialogFactory.inputText(parent, title,
                app.getText("Dialog.NewChapter.InputTip"),
                app.getText("Dialog.NewChapter.ChapterTitle"), false, false);
        if (name == null) {
            return null;
        }
        return new Chapter(name);
    }

    public void deleteCache(File cache) {
        if (cache != null) {
            if (! cache.delete()) {
                LOG.debug("failed to delete cache: " + cache.getAbsolutePath());
            }
        }
    }

    public File duplicateToCache(File source) throws IOException {
        File cache = null;
        try {
            cache = File.createTempFile("imabw_", ".itf");
            FileUtils.copyFile(source, cache);
        } catch (IOException ex) {
            deleteCache(cache);
            throw ex;
        }
        return cache;
    }

    public Object[] readBook(ParserData pd, boolean useCache) throws Exception {
        File input, cache = null;
        if (useCache) {
            cache = duplicateToCache(pd.file);
            input = cache;
        } else {
            input = pd.file;
        }

        Book book;
        try {
            book = Jem.readBook(input, pd.format, pd.kw);
            addAttributes(book);
        } catch (Exception ex) {
            // file is cached
            deleteCache(cache);
            throw ex;
        }

        return new Object[]{book, cache};
    }

    public Book readBook(ParserData pd) throws Exception {
        return Jem.readBook(pd.file, pd.format, pd.kw);
    }

    public void writeBook(MakerData md) throws Exception {
        Jem.writeBook(md.book, md.file, md.format, md.kw);
    }

    public void showOpenError(Component parent, String title, ParserData data,
                               Throwable t) {
        String str = app.getText("Dialog.ReadBook.Failed", data.file, transErrorMessage(t));
        DialogFactory.viewException(parent, title, str, t);
    }

    public void showSaveError(Component parent, String title, MakerData data,
                               Throwable e) {
        String str = app.getText("Dialog.WriteBook.Failed", data.file, transErrorMessage(e));
        DialogFactory.viewException(parent, title, str, e);
    }

    public String transErrorMessage(Throwable t) {
        String str;
        if (t instanceof UnsupportedFormatException) {
            str = app.getText("Dialog.Exception.UnsupportedFormat",
                    ((UnsupportedFormatException) t).getFormat());
        } else {
            str = t.getMessage();
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
