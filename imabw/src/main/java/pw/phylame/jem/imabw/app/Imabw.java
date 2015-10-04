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

import java.awt.*;
import java.io.File;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

import pw.phylame.jem.core.Book;
import pw.phylame.gaf.ixin.IResource;
import pw.phylame.gaf.ixin.IApplication;
import pw.phylame.jem.imabw.app.config.AppSettings;
import pw.phylame.jem.imabw.app.data.*;
import pw.phylame.jem.imabw.app.ui.Viewer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.io.FilenameUtils;
import pw.phylame.jem.imabw.app.ui.dialog.DialogFactory;
import pw.phylame.jem.imabw.app.ui.dialog.WaitingWork;

import javax.swing.*;

public class Imabw extends IApplication implements Constants {
    private static Log LOG = LogFactory.getLog(Imabw.class);

    public static Imabw getInstance() {
        return (Imabw) getApplication();
    }

    Imabw(String[] args) {
        super(NAME, VERSION, args);

        worker = Worker.getInstance();

        // firstly, parse CLI arguments
        parseCLIOptions();
    }

    private Viewer viewer;
    private Worker worker;
    private CLIContext context;
    private BookTask task;

    /**
     * Holds data from CLI arguments.
     */
    private class CLIContext {
        // input book files
        LinkedList<String> inputs = new LinkedList<>();

        // input book format
        String format = null;

        // Jem parser argument
        HashMap<String, Object> kw = new HashMap<>();
    }

    /**
     * Prints CLI usage and exit Imabw.
     *
     * @param status exit status code
     */
    private void printCLIUsage(int status) {
        System.out.println("usage: imabw: -p <key=value> -f <format> file");
        System.exit(status);
    }

    private void parseCLIOptions() {
        context = new CLIContext();

        String[] argv = getArguments();
        int i = 0, length = argv.length;
        while (i < length) {
            String arg = argv[i++];
            if ("-p".equals(arg)) {             // parser argument
                if (i < length) {               // has value
                    String[] parts = argv[i++].split("=", 2);
                    if (parts.length != 2) {
                        System.err.println("imabw: -p required <key=value> argument");
                        printCLIUsage(-1);
                    } else {
                        context.kw.put(parts[0], parts[1]);
                    }
                } else {
                    System.err.println("imabw: -p required <key=value> argument");
                    printCLIUsage(-1);
                }
            } else if ("-f".equals(arg)) {
                if (i < length) {               // has value
                    context.format = argv[i++];
                } else {
                    System.err.println("imabw: -f required format");
                    printCLIUsage(-1);
                }
            } else if ("-h".equals(arg)) {
                printCLIUsage(0);
            } else {
                --i;
                context.inputs.add(argv[i++]);
            }
        }
    }

    @Override
    protected void onStart() {
        // ensure home directory existed
        ensureHomeExisted();

        // resource home
        IResource.BASE_DIR = "pw-imabw";

        AppSettings config = AppSettings.getInstance();

        debug = config.isDebugEnable();

        // I18N
        Locale.setDefault(config.getAppLocale());
        installTranslator(IResource.loadTranslator(I18N_NAME));

        // resource icon set
        IResource.setIconSet(config.getIconSet());

        // SWING LAF
        setLafTheme(config.getLafTheme());

        setWindowDecorated(config.isWindowDecorated());

        // global UI font
        Font font = config.getGlobalFont();
        if (font != null) {
            setGlobalFont(font);
        }

        // font anti-aliasing
        setAntiAliasing(config.isAntiAliasing());

        if (config.isPluginsEnable()) {
            try {
                loadPlugins();
            } catch (Exception e) {
                LOG.debug("cannot load plugins", e);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (task != null) {
            task.cleanup();
            if (task.getSource() != null) {
                FileHistory.add(task.getSource());
            }
        }
        viewer.destroy();
    }

    @Override
    public void run() {
        viewer = new Viewer();
        viewer.setStatusText(getText("Frame.Status.Ready"));

        newFile(getText("Dialog.NewBook.BookTitle"));

        viewer.setVisible(true);

        if (!context.inputs.isEmpty()) {
            // only open the last input file
            File input = new File(context.inputs.getLast());
            openBook(new ParserData(input, context.format, context.kw));
        }
    }

    private boolean debug = false;

    public void debug(String message, Object... args) {
        if (!debug) {
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d H:m:s-S");
        String text = String.format("[DEBUG] %s %s: %s", getName(), sdf.format(new Date()),
                MessageFormat.format(message, args));
        System.err.println(text);
    }

    public void debug(String message, Exception e, Object... args) {
        debug(message, args);
        if (debug) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the main frame window.
     *
     * @return the viewer
     */
    public Viewer getActiveViewer() {
        return viewer;
    }

    /**
     * Activates specified task to current task.
     *
     * @param task the task to be activated
     */
    public void activateTask(BookTask task) {
        if (this.task != null) {
            File source = this.task.getSource();
            if (source != null) {
                FileHistory.add(source);
            }
        }

        this.task = task;
        viewer.setBook(task.getBook());
        File source = task.getSource();
        if (source != null) {
            viewer.setActionEnable(FILE_DETAILS, true);
            FileHistory.remove(source);
        } else {
            viewer.setActionEnable(FILE_DETAILS, false);
            viewer.updateHistoryMenu();
        }
    }

    /**
     * Gets current active task
     *
     * @return the task
     */
    public BookTask getActiveTask() {
        return task;
    }

    public void notifyMessage(String message) {
        getActiveViewer().setStatusText(message);
    }

    // ******************* //
    // ** Menu actions ** //
    // ******************* //

    /**
     * Creates new book with specified name and activates the book.
     *
     * @param name name of the new book, if <tt>null</tt>, user need input new name.
     */
    public void newFile(String name) {
        String title = getText("Dialog.NewBook.Title");
        if (!maybeSaving(title)) {
            return;
        }
        Book book = worker.newBook(viewer, title, name);
        if (book == null) {
            return;
        }
        if (task != null) {
            task.cleanup();
        }
        activateTask(BookTask.fromNewBook(book));
        notifyMessage(getText("Dialog.NewBook.Finished", book.stringAttribute(Book.TITLE)));
    }

    /**
     * Reads e-book file with specified config and activates the book.
     * <p>
     * While reading the file, a waiting dialog will be shown and be closed
     * f finished reading.
     *
     * @param pd Jem parser data
     */
    public void openBook(ParserData pd) {
        String title = getText("Dialog.OpenBook.Title");

        DialogFactory.openWaiting(viewer, title, getText("Dialog.OpenBook.TipText", pd.file.getPath()),
                getText("Dialog.OpenBook.WaitingText"), new OpenBookWork(title, pd, true), false);
    }

    public void saveBook(String title, MakerData md, boolean isSaveAs) {
        DialogFactory.openWaiting(viewer, title, getText("Dialog.SaveBook.TipText", md.file.getPath()),
                getText("Dialog.SaveBook.WaitingText"), new SaveBookWork(title, md, isSaveAs), false);
    }

    /**
     * Selects e-book from local storage and activates the book.
     */
    public void openFile() {
        String title = getText("Dialog.OpenBook.Title");
        if (!maybeSaving(title)) {
            return;
        }
        OpenResult od = worker.selectOpenBook(viewer, title, false, task.getSource(), task.getFormat());
        if (od == null) {   // no selection
            return;
        }
        String format = od.getFormat();
        if (format == null) {
            format = FilenameUtils.getExtension(od.getFile().getPath());
        }
        Map<String, Object> kw = worker.getParseArguments(viewer, format);
        if (kw == null) {   // cancel config arguments
            return;
        }
        openBook(new ParserData(od.getFile(), format, kw));
    }

    /**
     * Checks book changes and ask user to save changes.
     *
     * @return <tt>true</tt> if book is not modified or all changes are saved,
     * otherwise <tt>false</tt>.
     */
    public boolean maybeSaving(String title) {
        if (task == null || !task.isBookModified()) {
            return true;
        }
        String[] options = {getText("Dialog.AskSaving.ButtonSave"),
                getText("Dialog.AskSaving.ButtonDiscard"), getText("Dialog.AskSaving.ButtonCancel")};
        int rev = DialogFactory.showAsking(viewer, title,
                getText("Dialog.AskSaving.Tip"), options, options[0]);
        switch (rev) {
            case DialogFactory.YES_OPTION:
                return saveFile();
            case DialogFactory.NO_OPTION:
                break;
            case DialogFactory.CANCEL_OPTION:
            case -1:
                return false;
        }
        return true;
    }

    /**
     * Saves book modifications to file.
     * <p>
     * If no source file specified, user need select new file to store book.
     * @return <tt>true</tt> if book was saved, otherwise <tt>false</tt>.
     */
    public boolean saveFile() {
        String title = getText("Dialog.SaveBook.Title", task.getBook().stringAttribute(Book.TITLE));
        File file = task.getSource();
        String format = task.getFormat();
        Map<String, Object> kw = task.getMakerArguments();
        // save new book
        if (file == null || format == null) {
            OpenResult od = worker.selectSaveBook(viewer, title, null, null);
            if (od == null) {
                return false;
            }
            file = od.getFile();
            format = od.getFormat();
        }
        if (kw == null) {
            kw = worker.getMakeArguments(viewer, format);
            if (kw == null) {   // cancel config arguments
                return false;
            }
        }
        MakerData md = new MakerData(task.getBook(), file, format, kw);
        saveBook(title, md, false);
        return true;
    }

    public void saveAsFile() {
        String title = getText("Dialog.SaveAsBook.Title", task.getBook().stringAttribute(Book.TITLE));
        OpenResult od = worker.selectSaveBook(viewer, title, task.getSource(), task.getFormat());
        if (od == null) {
            return;
        }
        Map<String, Object> kw = worker.getMakeArguments(viewer, od.getFormat());
        if (kw == null) {
            return;
        }
        MakerData md = new MakerData(task.getBook(), od.getFile(), od.getFormat(), kw);
        saveBook(title, md, true);
    }

    public void exitApp() {
        if (!maybeSaving(getText("Dialog.ExitApp.Title"))) {
            return;
        }
        exit(0);
    }

    private class OpenBookWork extends WaitingWork<Book, Void> {
        private String title;
        private ParserData pd;
        private boolean useCache;
        private File cache;

        public OpenBookWork(String title, ParserData pd, boolean useCache) {
            this.title = title;
            this.pd = pd;
            this.useCache = useCache;
        }

        @Override
        protected Book doInBackground() throws Exception {
            Object[] rev = worker.readBook(pd, useCache);
            cache = (File) rev[1];
            task.cleanup();
            return (Book) rev[0];
        }

        @Override
        protected void done() {
            try {
                Book book = get();
                activateTask(BookTask.fromOpenBook(book, pd, cache, true));
                notifyMessage(getText("Dialog.OpenBook.Finished", pd.file.getPath()));
                hideWaitingDialog();
            } catch (InterruptedException | ExecutionException e) {
                hideWaitingDialog();
                worker.showOpenError(viewer, title, pd, e.getCause());
            }
        }
    }

    private class SaveBookWork extends WaitingWork<File, Void> {
        private String title;
        private MakerData md;

        private boolean isSaveAs = false;

        public SaveBookWork(String title, MakerData md, boolean isSaveAs) {
            this.title = title;
            this.md = md;
            this.isSaveAs = isSaveAs;
        }

        @Override
        protected File doInBackground() throws Exception {
            viewer.getTabbedEditor().cacheAllTabs();
            worker.writeBook(md);
            if (!isSaveAs) {
                getActiveTask().bookSaved();
            }
            return md.file;
        }

        @Override
        protected void done() {
            try {
                File output = get();
                if (isSaveAs) {
                    hideWaitingDialog();
                    String str = getText("Dialog.SaveAsBook.Finished", output.getPath());
                    DialogFactory.showMessage(viewer, title, str);
                } else {
                    if (task.getSource() == null || task.getFormat() == null) {
                        task.setSource(md.file);
                        task.setFormat(md.format);
                        task.setMakerArguments(md.kw);
                        viewer.setActionEnable(FILE_DETAILS, true);
                        notifyMessage(getText("Dialog.SaveBook.Finished", output.getPath()));
                    }
                    viewer.updateTitle();
                    hideWaitingDialog();
                }
            } catch (InterruptedException | ExecutionException e) {
                hideWaitingDialog();
                worker.showSaveError(viewer, title, md, e.getCause());
            }
        }
    }
}
