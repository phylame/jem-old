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

import pw.phylame.ixin.IToolkit;
import pw.phylame.imabw.ui.com.PartNode;

import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.BookHelper;
import pw.phylame.jem.core.Jem;
import pw.phylame.jem.core.Part;
import pw.phylame.jem.util.JemException;
import pw.phylame.tools.StringUtils;
import pw.phylame.tools.file.FileFactory;
import pw.phylame.tools.file.FileNameUtils;

import java.awt.Component;
import javax.swing.UIManager;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.net.URL;
import java.io.File;
import java.io.IOException;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Utilities functions for Imabw.
 */
public class Worker {
    private static Log LOG = LogFactory.getLog(Worker.class);

    private static Worker worker;

    Application app = Application.getApplication();

    private JFileChooser fileChooser = new JFileChooser();

    private HashMap<String, String> formatNames = new HashMap<>();

    public Worker() {
        worker = this;

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

    public static Worker getInstance() {
        return worker;
    }

    public String inputText(Component parent, String title, Object message, String initValue) {
        return (String) JOptionPane.showInputDialog(parent, message, title, JOptionPane.PLAIN_MESSAGE, null,
                null, initValue);
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
        localeChooser.setLocale(Locale.forLanguageTag(initLang));
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
        fileChooser.removeChoosableFileFilter(fileChooser.getAcceptAllFileFilter());
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

    public List<FileFilter> makeFileExtensionFilters(String[] formats, boolean acceptAll) {
        List<FileFilter> filters = new java.util.ArrayList<>();
        if (acceptAll) {
            filters.add(new FileNameExtensionFilter(
                    app.getText("Common.Format.All", StringUtils.join(formats, " *.")), formats));
        }
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

    public File selectOpenBook(Component parent, String title) {
        String[] formats = BookHelper.supportedParsers().toArray(new String[0]);
        return selectOpenFile(parent, title, makeFileExtensionFilters(formats, true), null, true, null);
    }

    public File selectSaveBook(Component parent, String title, String format) {
        String[] formats;
        if (format != null) {
            formats = new String[]{format};
        } else {
            formats = BookHelper.supportedParsers().toArray(new String[0]);
        }
        return selectSaveFile(parent, title, makeFileExtensionFilters(formats, false), null, false, null);
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
     * @param title initialized book title
     * @return the new book
     */
    public Book newBook(Component parent, String title) {
        if (StringUtils.isEmpty(title)) {
            title = inputLoop(parent, app.getText("Dialog.NewBook.Title"), app.getText("Dialog.NewBook.Tip"),
                    app.getText("Dialog.NewBook.NoInput"), app.getText("Common.NewBookTitle"));
            if (title == null) {
                return null;
            }
        }
        Book book = new Book(title, "");
        addAttributes(book);
        book.newChapter(app.getText("Common.NewChapterTitle"));
        return book;
    }

    /**
     * Loads book from book file.
     * If <code>file</code> is <code>null</code>, asks user to select book file.
     * @param file the book file
     * @param title title of open file dialog
     * @return the book
     */
    public Book openBook(Component parent, File file, String title) {
        // 1. select book file
        if (file == null) {
            file = selectOpenBook(parent, title);
        }
        if (file == null) {
            return null;    // no selection
        }

        fileChooser.setCurrentDirectory(file.getAbsoluteFile().getParentFile());

        String format = FileNameUtils.extensionName(file.getPath());
        HashMap<String, Object> kw = null;

        // 2. read book
        Book book = null;
        try {
            book = Jem.readBook(file, format, kw);
            // 3. addAttributes
            addAttributes(book);
        } catch (IOException | JemException e) {
            showError(parent, title, app.getText("Dialog.OpenBook.Error", file.getPath(), e.getMessage()));
        }

        return book;
    }

    public File getSourceFile(Book book) {
        Object o = book.getAttribute(Jem.SOURCE_FILE);
        if (o instanceof File) {
            return (File) o;
        } else {
            return null;
        }
    }

    public String getSourceFormat(Book book) {
        Object o = book.getAttribute(Jem.SOURCE_FORMAT);
        if (o instanceof String) {
            return (String) o;
        } else {
            return null;
        }
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

    private File prepareSaving(Component parent, String title, File path, String format) {
        if (path != null) {
            return path;
        }
        path = selectSaveBook(parent, title, format);
        if (path == null) {     // cancel
            return null;
        }
        File source = app.getManager().getOpenedFile();
        if (source != null && source.compareTo(path) == 0) {        // path of file is currently opening
            showError(parent, title, app.getText("Dialog.SaveBook.UsingFile", path));
            return null;
        }
        return path;
    }

    private HashMap<String, Object> getMakeArguments(Book book, String format) {
        HashMap<String, Object> kw = new HashMap<>();

        return kw;
    }

    /**
     * Saves book to file.
     * @param parent parent component of input dialog
     * @param title title of save file dialog
     * @param book the book
     * @param path destination path
     * @param format output format
     * @return the saved path
     */
    public File saveBook(Component parent, String title, Book book, File path, String format) {
        if (path == null) {
            path = prepareSaving(parent, title, null, format);
            if (path == null) {     // cancel
                return null;
            }
        }
        HashMap<String, Object> kw = getMakeArguments(book, format);

        System.out.printf("save book %s to %s with %s\n", book, path, format);

        return path;
    }

    public File saveBook(Component parent, String title, Book book) {
        File path = prepareSaving(parent, title, null, null);
        if (path == null) {
            return null;
        }
        String format = getSelectedFormat();
        assert format != null;

        HashMap<String, Object> kw = getMakeArguments(book, format);

        try {
            Jem.writeBook(book, path, format, kw);
        } catch (IOException | JemException e) {
            showError(app.getViewer(), title, app.getText("Dialog.SaveBook.Error", path.getPath(), e.getMessage()));
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
        if ("".equals(book.stringAttribute("vendor", ""))) {
            book.setAttribute("vendor", String.format("%s v%s %s", app.getText("App.Name"), Constants.VERSION,
                    Constants.RELEASE));
        }
        if ("".equals(book.getRights())) {
            book.setRights(String.format("(C) %d PW arts", Calendar.getInstance().get(Calendar.YEAR)));
        }
    }

    public List<Part> findParts(final Part part, String key) {
        final List<Part> parts = new ArrayList<>();
        final Pattern pattern = Pattern.compile(key);
        Jem.walkPart(part, new Jem.Walker() {
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
