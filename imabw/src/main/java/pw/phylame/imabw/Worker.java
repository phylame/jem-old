/*
 * Copyright 2015 Peng Wan <phylame@163.com>
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

import pw.phylame.imabw.ui.com.PartNode;
import pw.phylame.ixin.IToolkit;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Jem;
import pw.phylame.jem.core.Part;
import pw.phylame.jem.util.JemException;
import pw.phylame.tools.StringUtils;
import pw.phylame.tools.file.FileFactory;
import pw.phylame.tools.file.FileNameUtils;

import java.awt.Component;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Utilities functions and constants for Imabw.
 */
public class Worker {
    Application app = Application.getApplication();

    private JFileChooser fileChooser = new JFileChooser();

    private Map<String, String> formatNames = new HashMap<>();

    public Worker() {
        String[] formats = {"pmab", "umd", "jar", "txt", "epub", "jpg", "jpeg", "png", "gif", "bmp"};
        for (String fmt: formats) {
            String name = app.getText("Common.Format."+fmt.toUpperCase());
            formatNames.put(fmt, String.format("%s (*.%s)", name, fmt));
        }

        UIManager.put("OptionPane.okButtonText", app.getText("Dialog.ButtonOk"));
        UIManager.put("OptionPane.cancelButtonText", app.getText("Dialog.ButtonCancel"));
        UIManager.put("OptionPane.yesButtonText", app.getText("Dialog.ButtonYes"));
        UIManager.put("OptionPane.noButtonText", app.getText("Dialog.ButtonNo"));
    }

    public String inputText(Component parent, String title, Object message, String initValue) {
        return (String) JOptionPane.showInputDialog(parent, message, title, JOptionPane.PLAIN_MESSAGE, null, null, initValue);
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
                                       boolean acceptAll, String initDir) {
        fileChooser.setDialogTitle(title);
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

    public List<FileFilter> makeFileFormatFilters(List<String> formats, boolean acceptAll) {
        List<FileFilter> filters = new java.util.ArrayList<>();
        if (acceptAll) {
            filters.add(new FileNameExtensionFilter(app.getText("Common.Format.All", StringUtils.join(formats, " *.")),
                    formats.toArray(new String[0])));
        }
        for (String format: formats) {
            filters.add(new FileNameExtensionFilter(formatNames.get(format), format));
        }
        return filters;
    }

    public File selectOpenFile(Component parent, String title, List<FileFilter> filters,
                                      FileFilter initFilter, boolean acceptAll, String initDir) {
        initFileChooser(title, filters, initFilter, acceptAll, initDir);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (fileChooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        return fileChooser.getSelectedFile();
    }

    public File selectSaveFile(Component parent, String title, List<FileFilter> filters,
                                      FileFilter initFilter, boolean acceptAll, String initDir) {
        initFileChooser(title, filters, initFilter, acceptAll, initDir);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (fileChooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File file = fileChooser.getSelectedFile();
        /* Add file extension name if not given */
        FileNameExtensionFilter filter = (FileNameExtensionFilter) fileChooser.getFileFilter();
        if (filter.getExtensions().length == 1) {
            if ("".equals(FileNameUtils.extensionName(file.getPath()))) {
                file = new File(file.getPath() + "." + filter.getExtensions()[0]);
            }
        }
        return file;
    }

    public File selectDirectory(Component parent, String title, String initDir) {
        initFileChooser(title, null, null, false, initDir);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fileChooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        return fileChooser.getSelectedFile();
    }

    public File selectOpenBook(Component parent, String title) {
        List<String> formats = Arrays.asList("pmab", "umd", "jar", "txt", "epub");
        return selectOpenFile(parent, title, makeFileFormatFilters(formats, true), null, true, null);
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

    public Book newBook(String title) {
        while (title == null || title.length() == 0) {
            title = inputText(app.getViewer(), app.getText("Dialog.NewBook.Title"), app.getText("Dialog.NewBook.Tip"),
                    app.getText("Common.NewBookTitle"));
            if (title == null) {
                return null;    // cancel input
            }
            if (title.length() == 0) {
                showError(app.getViewer(), app.getText("Dialog.NewBook.Title"), app.getText("Dialog.NewBook.NoInput"));
            }
        }
        Book book = new Book(title, "");
        addAttributes(book);
        book.newChapter(app.getText("Common.NewChapterTitle"));
        return book;
    }

    /**
     * Selects book file and open it.
     * @param title title of open file dialog
     * @return the book
     */
    public Book openBook(File file, String title) {
        // 1. select book file
        if (file == null) {
            file = selectOpenBook(app.getViewer(), title);
        }
        if (file == null) {
            return null;
        }

        String format = FileNameUtils.extensionName(file.getPath());
        Map<String, Object> kw = null;

        // 2. read book
        Book book = null;
        try {
            book = Jem.readBook(file, format, null);
            // 3. addAttributes
            addAttributes(book);
        } catch (IOException |JemException e) {
            showError(app.getViewer(), title, app.getText("Dialog.OpenBook.Error",
                    file.getPath(), e.getMessage()));
        }

        return book;
    }

    public PartNode newChapter(PartNode parent, String title) {
        String text = null;
        while (text == null || text.length() == 0) {
            text = inputText(app.getViewer(), title, app.getText("Dialog.NewChapter.Tip"),
                    app.getText("Common.NewChapterTitle"));
            if (text == null) {
                return null;        // cancel input
            }
            if (text.length() == 0) {
                showError(app.getViewer(), title, app.getText("Dialog.NewChapter.NoInput"));
            }
        }
        PartNode node = new PartNode(new Part(text));
        if (parent != null) {
            parent.appendNode(node);
        }

        return node;
    }

    /**
     * Saves book to file. If {@code path} is <code>null</code>, select new file path.
     * @param book the book
     * @param path destination path
     * @param format output format
     * @param title title of save file dialog
     * @return the saved path
     */
    public File saveBook(Book book, File path, String format, String title) {
        // 1. select file path is path is null
        // 3. select output kw
        // 4. save book
        Jem.walkPart(book, new Jem.Walker() {
            @Override
            public boolean watch(Part part) {
                System.out.println(part.getTitle());
                return true;
            }
        });
        System.out.printf("save %s to %s with %s\n", book, path, format);
        return null;
    }

    private void addAttributes(Book book) {
        if (book.getCover() == null) {
            URL url = Worker.class.getResource("/cover.png");
            if (url != null) {
                book.setCover(FileFactory.getFile(url, null));
            }
        }
        if ("".equals(book.stringAttribute("vendor", ""))) {
            book.setAttribute("vendor", String.format("%s v%s", app.getText("App.Name"), Constants.VERSION));
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
