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

package pw.phylame.imabw;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pw.phylame.imabw.ui.com.*;
import pw.phylame.ixin.IToolkit;

import pw.phylame.jem.core.*;
import pw.phylame.jem.util.JemException;
import pw.phylame.tools.StringUtils;
import pw.phylame.tools.file.FileFactory;
import pw.phylame.tools.file.FileNameUtils;

import java.awt.Component;
import java.awt.Window;
import javax.swing.UIManager;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.net.URL;
import java.io.File;
import java.io.IOException;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Utilities functions for Imabw.
 */
public class Worker {
    private static Log LOG = LogFactory.getLog(Worker.class);

    Imabw app = Imabw.getInstance();

    private JFileChooser fileChooser = new JFileChooser();

    private HashMap<String, String> formatNames = new HashMap<>();

    private Set<String> encodings = null;

    public Worker() {
        // supported formats
        String[] formats = {"pmab", "umd", "jar", "txt", "epub", "jpg", "jpeg", "png", "gif", "bmp"};
        for (String fmt : formats) {
            String name = app.getText("Common.Format." + fmt.toUpperCase());
            formatNames.put(fmt, String.format("%s (*.%s)", name, fmt));
        }

        UIManager.put("OptionPane.okButtonText", app.getText("Dialog.ButtonOk"));
        UIManager.put("OptionPane.cancelButtonText", app.getText("Dialog.ButtonCancel"));
        UIManager.put("OptionPane.yesButtonText", app.getText("Dialog.ButtonYes"));
        UIManager.put("OptionPane.noButtonText", app.getText("Dialog.ButtonNo"));
    }

    public Set<String> getEncodings() {
        if (encodings == null) {
            encodings = new TreeSet<>(Arrays.asList("ASCII", "ISO-8859-1", "UTF-16LE",
                    "UTF-16BE", "UTF-8", "GB2312", "GBK", "GB18030", "Big5"));
        }
        return encodings;
    }

    public String inputText(Component parent, String title, Object message, String initValue) {
        return (String) JOptionPane.showInputDialog(parent, message, title, JOptionPane.PLAIN_MESSAGE, null,
                null, initValue);
    }

    public String longInput(Component parent, String title, String message, String initValue) {
        if (parent instanceof java.awt.Dialog) {
            return pw.phylame.imabw.ui.dialog.LongInputDialog.inputText((java.awt.Dialog) parent,
                    title, message, initValue);
        } else {
            return pw.phylame.imabw.ui.dialog.LongInputDialog.inputText((java.awt.Frame)parent,
                    title, message, initValue);
        }
    }

    public boolean showConfirm(Component parent, String title, Object message) {
        return JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                IToolkit.createImageIcon(":/res/img/dialog/question.png")) == JOptionPane.YES_OPTION;
    }

    public void showMessage(Component parent, String title, Object message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE,
                IToolkit.createImageIcon(":/res/img/dialog/information.png"));
    }

    public void showWarning(Component parent, String title, Object message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE,
                IToolkit.createImageIcon(":/res/img/dialog/warning.png"));
    }

    public void showError(Component parent, String title, Object message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE,
                IToolkit.createImageIcon(":/res/img/dialog/prohibit.png"));
    }

    public String inputLoop(Component parent, String title, String inputTip, String noInput, String initValue) {
        String text = null;
        while (StringUtils.isEmpty(text)) {
            text = inputText(parent, title, inputTip, initValue);
            if (text == null) {
                break;          // cancel input
            }
            if (text.length() == 0) {
                showError(parent, title, noInput);
            }
        }

        return text;
    }

    public Date selectDate(Component parent, String title, String tipText, Date initDate) {
        com.toedter.calendar.JCalendar calendar = new com.toedter.calendar.JCalendar(initDate);
        Object[] message = new Object[]{tipText, calendar};
        int r = JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        return r == JOptionPane.OK_OPTION ? calendar.getDate() : null;
    }

    public String selectLanguage(Component parent, String title, String tipText, String initLang) {
        com.toedter.components.JLocaleChooser localeChooser = new com.toedter.components.JLocaleChooser();
        if (initLang != null) {
            localeChooser.setLocale(Locale.forLanguageTag(initLang));
        }
        Object[] message = new Object[]{tipText, localeChooser};
        int r = JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        return r == JOptionPane.OK_OPTION ? localeChooser.getLocale().toLanguageTag() : null;
    }


    public void initFileChooser(String title, List<FileFilter> filters, FileFilter initFilter,
                                       boolean acceptAll, int mode, String initDir) {
        fileChooser.setDialogTitle(title);
        fileChooser.setFileSelectionMode(mode);
        fileChooser.setAcceptAllFileFilterUsed(acceptAll);
        /* remove all file filters */
        fileChooser.resetChoosableFileFilters();
        if (! acceptAll) {
            fileChooser.removeChoosableFileFilter(fileChooser.getAcceptAllFileFilter());
        }
        if (filters != null) {
            for (FileFilter filter : filters) {
                fileChooser.addChoosableFileFilter(filter);
            }
        }
        if (initFilter != null) {
            fileChooser.setFileFilter(initFilter);
        }
        if (initDir != null) {
            fileChooser.setCurrentDirectory(new File(initDir));
        }
    }

    public List<FileFilter> makeFileExtensionFilters(String[] formats) {
        List<FileFilter> filters = new ArrayList<>();
        for (String format: formats) {
            filters.add(new FileNameExtensionFilter(formatNames.get(format), format));
        }
        return filters;
    }

    public File selectOpenFile(Component parent, String title, List<FileFilter> filters,
                                      FileFilter initFilter, boolean acceptAll, String initDir) {
        initFileChooser(title, filters, initFilter, acceptAll, JFileChooser.FILES_ONLY, initDir);
        if (fileChooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        return fileChooser.getSelectedFile();
    }

    public File selectSaveFile(Component parent, String title, List<FileFilter> filters,
                               FileFilter initFilter, boolean acceptAll, String initDir) {
        initFileChooser(title, filters, initFilter, acceptAll, JFileChooser.FILES_ONLY, initDir);
        File file = null;
        while (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();

            /* Add file extension name if not given */
            String format = getSelectedFormat();
            if (format != null) {
                if ("".equals(FileNameUtils.extensionName(file.getPath()))) {
                    file = new File(file.getPath() + "." + format);
                }
            }
            if (file != null && file.exists()) {    // ask overwrite
                if (! showConfirm(parent, title, app.getText("Dialog.SaveFile.Overwrite", file.getPath()))) {
                    file = null;
                    continue;
                }
            }
            break;
        }
        return file;
    }

    public String[] getSelectedFormats() {
        FileFilter fileFilter = fileChooser.getFileFilter();
        if (fileFilter instanceof FileNameExtensionFilter) {
            return ((FileNameExtensionFilter) fileFilter).getExtensions();
        }
        return null;
    }

    public String getSelectedFormat() {
        String[] formats = getSelectedFormats();
        if (formats != null && formats.length > 0) {
            return formats[0];
        }
        return null;
    }

    public File selectDirectory(Component parent, String title, String initDir) {
        initFileChooser(title, null, null, false, JFileChooser.DIRECTORIES_ONLY, initDir);
        if (fileChooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        return fileChooser.getSelectedFile();
    }

    public File selectOpenImage(Component parent, String title) {
        String[] formats = {"jpg", "jpeg", "png", "gif", "bmp"};
        return selectOpenFile(parent, title, makeFileExtensionFilters(formats), null, true, null);
    }

    public File selectSaveImage(Component parent, String title) {
        String[] formats = {"jpg", "jpeg", "png", "gif", "bmp"};
        return selectSaveFile(parent, title, makeFileExtensionFilters(formats), null, false, null);
    }

    public File selectOpenBook(Component parent, String title) {
        ArrayList<String> formats = new ArrayList<>(BookHelper.supportedParsers());
        formats.remove("online");
        return selectOpenFile(parent, title, makeFileExtensionFilters(formats.toArray(
                new String[0])), null, true, null);
    }

    public File selectSaveBook(Component parent, String title, String format) {
        String[] formats;
        if (format != null) {
            formats = new String[]{format};
        } else {
            ArrayList<String> fmt = new ArrayList<>(BookHelper.supportedMakers());
            formats = fmt.toArray(new String[0]);
        }
        return selectSaveFile(parent, title, makeFileExtensionFilters(formats), null, false, null);
    }

    /**
     * Selects a supported file or other file.
     * @param parent parent component
     * @param title dialog title
     * @return the file or <tt>null</tt> if cancelled
     */
    public File selectSupportedFile(Component parent, String title) {
        String[] formats = formatNames.keySet().toArray(new String[0]);
        return selectOpenFile(parent, title, makeFileExtensionFilters(formats), null, true, null);
    }

    // get max same content in left of a and b
    public <T> List<T> leftSame(List<T> a, List<T> b) {
        List<T> results = new ArrayList<>();
        Iterator<T> itA = a.iterator(), itB = b.iterator();
        while (itA.hasNext() && itB.hasNext()) {
            T iA = itA.next(), iB = itB.next();

            if (! iA.equals(iB)) {      // different
                break;
            }
            results.add(iA);
        }
        return results;
    }

    public String formatSize(long size) {
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

    /**
     * Creates new book with specified title.
     * If <code>title</code> is <code>null</code>, asks user input a new title.
     * @param parent parent component of input dialog
     * @param title title of input dialog
     * @param bookTitle initialized book title
     * @return the new book
     */
    public Task newBook(Component parent, String title, String bookTitle) {
        if (StringUtils.isEmpty(bookTitle)) {
            bookTitle = inputLoop(parent, title, app.getText("Dialog.NewBook.Tip"),
                    app.getText("Dialog.NewBook.NoInput"),
                    app.getText("Common.NewBookTitle"));
            if (bookTitle == null) {
                return null;
            }
        }
        Book book = new Book(bookTitle, "");
        addAttributes(book);
        book.newChapter(app.getText("Common.NewChapterTitle"));

        return Task.newBook(book);
    }

    /**
     * Loads book from book file.
     * If <code>file</code> is <code>null</code>, asks user to select book file.
     * @param file the book file
     * @param title title of open file dialog
     * @return the book
     */
    public Task openBook(Component parent, String title, File file, String format, Map<String, Object> kw) {
        Task task = Task.newBook(null);
        task.setSource(file);
        task.setFormat(format);

        if (Jem.PMAB_FORMAT.equals(format)) {       // pmab using cache
            File cache = null;
            try {
                cache = File.createTempFile("imabw_", ".itf");
                org.apache.commons.io.FileUtils.copyFile(file, cache);
            } catch (IOException e) {
                if (cache != null) {
                    if (! cache.delete()) {
                        LOG.debug("cannot delete PMAB cache "+cache.getAbsolutePath());
                    }
                }
                showError(parent, title, app.getText("Dialog.OpenBook.Error", file.getPath(), e.getMessage()));
                return null;
            }
            task.setCache(cache, true);
            file = cache;
        }

        // 2. read book
        Book book = readBook(parent, title, file, format, kw, task.getSource());
        if (book == null) {
            return null;
        }
        task.setBook(book);
        fileChooser.setCurrentDirectory(task.getSource().getAbsoluteFile().getParentFile());
        return task;
    }

    public Book readBook(Component parent, String title, File file, String format, Map<String, Object> kw, File source) {
        Book book = null;
        try {
            book = Jem.readBook(file, format, kw);
            addAttributes(book);
        } catch (IOException | JemException e) {
            showError(parent, title, app.getText("Dialog.OpenBook.Error", source.getPath(), e.getMessage()));
        }

        return book;
    }

    /**
     * Creates a new chapter node and appends it to <tt>parentNode</tt> if presents.
     * @param parent parent component of input dialog
     * @param parentNode parent node, if not <tt>null</tt>, appends new node to it.
     * @param title dialog title of input dialog
     * @return the new node
     */
    public PartNode newChapter(Component parent, PartNode parentNode, String title) {
        String text = inputLoop(parent, title, app.getText("Dialog.NewChapter.Tip"),
                app.getText("Dialog.NewChapter.NoInput"), app.getText("Common.NewChapterTitle"));
        if (text == null) {
            return null;
        }
        PartNode node = new PartNode(new Part(text));
        if (parentNode != null) {
            parentNode.appendNode(node);
        }

        return node;
    }

    private File prepareSaving(Component parent, String title, String format, Task task) {
        File path = selectSaveBook(parent, title, format);
        if (path == null) {     // cancel
            return null;
        }
        // path of file is currently opening, source or cache
        if ((task.getSource() != null && task.getSource().compareTo(path) == 0) ||
                (task.getCache() != null && task.getCache().compareTo(path) == 0)) {
            showError(parent, title, app.getText("Dialog.SaveBook.UsingFile", path));
            return null;
        }
        return path;
    }


    public Map<String, Object> getParseArguments(Window parent, String format) {
        return PacFactory.getArguments(parent, app.getText("Dialog.PacProvider.Title",
                format.toUpperCase()), format);
    }

    public Map<String, Object> getMakeArguments(Window parent, String format) {
        return MacFactory.getArguments(parent,
                app.getText("Dialog.MacProvider.Title", format.toUpperCase()), format);
    }

    private boolean writeBook(Window parent, String title, Book book, File path, String format) {
        Map<String, Object> kw = getMakeArguments(parent, format);
        if (kw == null) {
            return false;
        }

        try {
            Jem.writeBook(book, path, format, kw);
        } catch (IOException | JemException e) {
            showError(parent, title, app.getText("Dialog.SaveBook.Error", path.getPath(),
                    e.getMessage()));
            return false;
        }

        return true;
    }

    public boolean saveBook(Window parent, String title, Task task) {
        File path;
        // new book or imported book
        if (task.getSource() == null || ! task.getFormat().equals(Jem.PMAB_FORMAT)) {
            path = prepareSaving(parent, title, Jem.PMAB_FORMAT, task);
            if (path == null) {
                return false;
            }
            task.setCache(task.getSource(), false);    // origin source is cache
        } else {    // save PMAB
            path = task.getSource();
        }

        if (! writeBook(parent, title, task.getBook(), path, Jem.PMAB_FORMAT)) {
            return false;
        }

        task.setSource(path);
        task.setFormat(Jem.PMAB_FORMAT);
        task.setModified(false);
        return true;
    }

    public File exportBook(Window parent, String title, Book book, Task task) {
        File path = prepareSaving(parent, title, null, task);
        if (path == null) {
            return null;
        }
        String format = getSelectedFormat();
        assert format != null;

        if (! writeBook(parent, title, book, path, format)) {
            return null;
        }

        return path;
    }

    private void addAttributes(Book book) {
        if (book.getCover() == null) {
            URL url = Worker.class.getResource("/cover.png");   // in jem-formats
            if (url != null) {
                book.setCover(FileFactory.fromURL(url, null));
            }
        }

        if (book.getDate() == null) {
            book.setDate(new Date());
        }

        if ("".equals(book.getLanguage())) {
            book.setLanguage(Locale.getDefault().toLanguageTag());
        }

        if ("".equals(book.getVendor())) {
            book.setVendor(String.format("%s v%s", app.getText("App.Name"), Constants.VERSION));
        }

        if ("".equals(book.getRights())) {
            book.setRights(String.format("(C) %d PW arts", Calendar.getInstance().get(Calendar.YEAR)));
        }
    }

    public ArrayList<Part> findParts(final Part part, String key) {
        final ArrayList<Part> parts = new ArrayList<>();
        final Pattern pattern = Pattern.compile(key);
        Jem.walkPart(part, new Walker() {
            @Override
            public boolean watch(Part p) {
                if (pattern.matcher(p.getTitle()).find()) {
                    parts.add(p);
                }
                return true;
            }
        });
        return parts;
    }
}
