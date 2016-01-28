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

import java.io.File;
import java.util.List;

import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.BookHelper;
import pw.phylame.gaf.ixin.ICommandMethod;
import pw.phylame.gaf.ixin.ICommandDispatcher;
import pw.phylame.imabw.app.model.*;
import pw.phylame.imabw.app.ui.Viewer;

import static pw.phylame.imabw.app.ui.dialog.DialogFactory.*;

/**
 * Provides methods for menu actions.
 */
public class Manager extends ICommandDispatcher implements Constants {
    private Imabw app;
    private Worker worker;
    private Viewer viewer;

    private BookTask task;

    Manager(Imabw app) {
        this.app = app;
        worker = Worker.sharedInstance();
        setDelegate(this);
    }

    void start() {
        this.viewer = app.getForm();
        viewer.setStatusText(app.getText("viewer.status.ready"));

        newFile(app.getText("d.newBook.defaultTitle"));
        viewer.setVisible(true);

//        editSettings(viewer);
        Imabw.CLIContext context = app.context;
        if (!context.inputs.isEmpty()) {
            // only open the last input file
            File input = new File(context.inputs.getLast());
            openBook(app.getText("d.openBook.title"),
                    new ParserData(input, context.format, context.kw, true));
        }
    }

    /**
     * Activates specified task to current task.
     *
     * @param task the task to be activated
     */
    public void activateTask(BookTask task) {
        File oldSource = (this.task != null) ? this.task.getSource() : null;
        this.task = task;
        viewer.setBook(task.getBook());
        FileHistory.insert(oldSource, false); // add old file to history
        File newSource = task.getSource();
        setCurrentDirectory(newSource);
        FileHistory.remove(newSource, true); // remove new file from history
        viewer.setActionEnable(FILE_DETAILS, newSource != null);
    }

    /**
     * Gets current active task
     *
     * @return the task
     */
    public BookTask getActiveTask() {
        return task;
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
        String title = app.getText("d.newBook.title");
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
        app.localizedMessage("d.newBook.finished", book);
    }

    /**
     * Reads e-book file with specified config and activates the book.
     * <p>
     * While reading the file, a waiting dialog will be shown and be closed
     * f finished reading.
     *
     * @param pd Jem parser data
     */
    public void openBook(String title, ParserData pd) {
        if (!pd.file.exists()) {
            localizedError(viewer, title, "d.openBook.fileNotExist", pd.file);
            return;
        }

        if (!BookHelper.hasParser(pd.format)) {
            localizedError(viewer, title, "d.openBook.unsupportedFormat", pd.format);
            return;
        }

        String tip = app.getText("d.openBook.tipText", pd.file);
        String waiting = app.getText("d.openBook.waitingText");
        openWaiting(viewer, title, tip, waiting, new OpenBookWork(title, pd), false);
    }

    /**
     * Selects e-book from local storage and activates the book.
     */
    public void openFile() {
        openFile(null);
    }

    public void openFile(File file) {
        String title = app.getText("d.openBook.title");
        if (!maybeSaving(title)) {
            return;
        }
        ParserData pd = worker.makeParserData(viewer, title, file, task.getFormat(), null);
        if (pd == null) {
            return;
        }
        pd.useCache = true;
        openBook(title, pd);
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
        String tip = app.getText("dialog.askSaving.tip");
        int rev = askSaving(viewer, title, tip);
        switch (rev) {
            case OPTION_DISCARD:
                break;
            case OPTION_OK:
                return saveFile();
            default:
                return false;
        }
        return true;
    }

    public void saveBook(String title, MakerData md, boolean isSaveAs) {
        if (!BookHelper.hasMaker(md.format)) {
            localizedError(viewer, title, "d.saveBook.unsupportedFormat", md.format);
            return;
        }

        String tip = app.getText("d.saveBook.tipText", md.file);
        String waiting = app.getText("d.saveBook.waitingText");
        openWaiting(viewer, title, tip, waiting, new SaveBookWork(title, md, isSaveAs), false);
    }

    /**
     * Saves book modifications to file.
     * <p>
     * If no source file specified, user need select new file to store book.
     *
     * @return <tt>true</tt> if book was saved, otherwise <tt>false</tt>.
     */
    public boolean saveFile() {
        String title = app.getText("d.saveBook.title", task.getBook());
        MakerData md = worker.makeMakerData(viewer, title, task.getBook(),
                task.getSource(), task.getFormat(), task.getMakerArguments());
        if (md == null) {
            return false;
        }
        saveBook(title, md, false);
        return true;
    }

    public void saveAsFile() {
        String title = app.getText("d.saveAsBook.title", task.getBook());
        MakerData md = worker.makeMakerData(viewer, title, task.getBook(),
                task.getSource(), null, null);
        if (md == null) {
            return;
        }
        saveBook(title, md, true);
    }

    @ICommandMethod(CLEAR_HISTORY)
    public void clearHistory() {
        FileHistory.clear(true);
    }

    public void exitApp() {
        if (!maybeSaving(app.getText("d.exitApp.title"))) {
            return;
        }
        if (task != null) {
            task.cleanup();
            FileHistory.insert(task.getSource(), false);
        }
        app.exit(0);
    }

    @ICommandMethod(FILE_DETAILS)
    public void fileDetails() {
        showDetails(viewer, task.getSource(), task.getBook());
    }

    @ICommandMethod(APP_SETTINGS)
    public void appSettings() {
        editSettings(viewer);
    }

    @ICommandMethod(HELP_CONTENTS)
    public void helpContents() {
        browseURI(DOCUMENT);
    }

    @ICommandMethod(ABOUT_APP)
    public void aboutApp() {
        showAbout(viewer);
    }

    boolean executeCommonCommand(String command) {
        boolean executed = true;
        switch (command) {
            case NEW_FILE:
                newFile(null);
                break;
            case OPEN_FILE:
                openFile();
                break;
            case SAVE_FILE:
                saveFile();
                break;
            case EXIT_APP:
                exitApp();
                break;
            case SAVE_AS_FILE:
                saveAsFile();
                break;
            default:
                executed = false;
        }
        return executed;
    }

    @Override
    public void commandPerformed(String command) {
        if (!executeCommonCommand(command)) {
            super.commandPerformed(command);
        }
    }

    private class OpenBookWork extends ReadBook {
        public OpenBookWork(String title, ParserData pd) {
            super(title, new ParserData[]{pd});
        }

        @Override
        protected List<ParseResult> doInBackground() throws Exception {
            List<ParseResult> result = super.doInBackground();
            task.cleanup();
            return result;
        }

        @Override
        protected void onSuccess(List<ParseResult> result) {
            ParseResult pr = result.get(0);
            activateTask(BookTask.fromOpenBook(pr));
            app.localizedMessage("d.openBook.finished", pr.pd.file);
            hideWaitingDialog();
        }
    }

    private class SaveBookWork extends WriteBook {
        private boolean saveAs;

        SaveBookWork(String title, MakerData md, boolean saveAs) {
            super(title, md);
            this.saveAs = saveAs;
        }

        @Override
        protected File doInBackground() throws Exception {
            File file = super.doInBackground();
            if (!saveAs) {
                task.bookSaved(md);
            }
            return file;
        }

        @Override
        protected void onSuccess(File result) {
            if (saveAs) {
                hideWaitingDialog();
                localizedInformation(viewer, title, "d.saveAsBook.finished", result);
            } else {
                viewer.setActionEnable(FILE_DETAILS, true);
                viewer.updateTitle();
                app.localizedMessage("d.saveBook.finished", result);
                hideWaitingDialog();
            }
        }
    }
}
