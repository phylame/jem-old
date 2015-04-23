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

import static pw.phylame.imabw.Constants.*;
import pw.phylame.imabw.ui.Viewer;
import pw.phylame.imabw.ui.com.EditorTab;
import pw.phylame.imabw.ui.com.MainPane;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Jem;
import pw.phylame.jem.core.Part;

import javax.swing.*;
import java.io.File;

/**
 * The manager.
 */
public class Manager {
    /** The application */
    private Application app = Application.getApplication();

    /** The viewer */
    private Viewer viewer;

    /** The worker */
    private Worker worker;

    private MainPane mainPane;

    private Book book;
    private File source;
    private String format;
    private boolean modified;

    public Manager(Viewer viewer, Worker worker) {
        this.viewer = viewer;
        this.worker = worker;
    }

    /** Begin manager works */
    public void begin() {
        showMainPane();
        viewer.setVisible(true);

        newFile();
        viewer.setStatusText(app.getText("Frame.StatusBar.Ready"));
    }

    /** Stop manager works */
    public void stop() {
        if (! maybeSave(app.getText("Dialog.Exit.Title"))) {
            return;
        }
        book.cleanup();
        app.exit(0);
    }

    private void showMainPane() {
        if (viewer.getPaneRender() instanceof MainPane) {
            return;
        }
        mainPane = new MainPane();
        viewer.setPaneRender(mainPane);
    }

    /** Get current name of book file */
    private String getSourceName() {
        String name;
        /* saved or open PMAB file */
        if (source != null) {
            name = source.getPath();
        } else {
            name = String.format("%s.%s", app.getText("Common.NewBookTitle"), Jem.PMAB_FORMAT);
        }
        return name;
    }

    /** Update frame title */
    private void updateTitle() {
        StringBuilder sb = new StringBuilder(book.getTitle());
        if (modified) {
            sb.append("*");
        }
        sb.append(" - ");
        String author = book.getAuthor();
        if (! "".equals(author)) {
            sb.append("[").append(author).append("] - ");
        }
        sb.append(getSourceName()).append(" - ");
        sb.append(app.getText("App.Name")).append(" ").append(Constants.VERSION);
        viewer.setTitle(sb.toString());
    }

    /**
     * Notifies book has been modified.
     * @param message the message
     */
    public void notifyModified(String message) {
        modified = true;
        updateTitle();
        if (message != null) {
            viewer.setStatusText(message);
        }
    }

    /** Checks book is modified and asks saving */
    private boolean maybeSave(String title) {
        if (! modified) {
            return true;
        }
        Object[] options = {app.getText("Dialog.ButtonSave"), app.getText("Dialog.ButtonDiscard"),
                app.getText("Dialog.ButtonCancel")};
        int ret = JOptionPane.showOptionDialog(viewer, app.getText("Dialog.Save.LabelSaveTip"),
                title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                options, options[0]);
        switch (ret) {
            case JOptionPane.YES_OPTION:
                saveFile();
                break;
            case JOptionPane.NO_OPTION:
                break;
            case JOptionPane.CANCEL_OPTION:
            case -1:
                return false;
        }
        return true;
    }

    private void newFile() {
        if (! maybeSave(app.getText("Dialog.New.Title"))) {
            return;
        }
        if (book != null) {
            book.cleanup();
        }
        book = worker.newBook();
        source = null;
        format = Jem.PMAB_FORMAT;
        modified = false;

        mainPane.setBook(book);
        updateTitle();

        mainPane.focusToTreeWindow();
        viewer.setStatusText(app.getText("Task.NewedBook", book.getTitle()));
    }

    private void openFile() {
        if (! maybeSave(app.getText("Dialog.Open.Title"))) {
            return;
        }
        Book _book = worker.openBook(app.getText("Dialog.OpenBook.Title"));
        if (_book == null) {
            return;
        }
        if (book != null) {
            book.cleanup();
        }
        book = _book;

        Object o = book.getAttribute("source_file", null);
        assert o instanceof File;
        source = (File) o;

        o = book.getAttribute("source_format", null);
        assert o instanceof String;
        format = (String) o;

        modified = false;

        mainPane.setBook(book);
        updateTitle();

        mainPane.focusToTreeWindow();
        viewer.setStatusText(app.getText("Task.OpenedBook", source.getPath()));
    }

    private void saveFile() {
        File path = worker.saveBook(book, source, format, app.getText("Dialog.SaveBook.Title"));
        if (path == null) {
            return;
        }
        source = path;
        modified = false;

        updateTitle();

        viewer.setStatusText(app.getText("Task.SavedBook", source.getPath()));
    }

    private void saveAsFile() {
        File path = worker.saveBook(book, null, format, app.getText("Dialog.SaveBook.Title"));
        if (path != null) {
            viewer.setStatusText(app.getText("Task.SavedAsBook", path.getPath()));
        }
    }

    private void viewProperties(Part part) {

    }

    public void viewPart(Part part) {
        // if part is opened, switch to the tab
        // else open new tab and switch to it
        EditorTab tab = mainPane.findEditorTab(part);
        if (tab == null) {
            tab = mainPane.newTab(part);
        }
        mainPane.switchToTab(tab);
    }

    public void onCommand(Object cmdID) {
        switch ((String) cmdID) {
            case NEW_FILE:
                newFile();
                break;
            case OPEN_FILE:
                openFile();
                break;
            case SAVE_FILE:
                saveFile();
                break;
            case SAVE_AS_FILE:
                saveAsFile();
                break;
            case EXIT_APP:
                stop();
                break;
            case EDIT_PREFERENCE:
                pw.phylame.imabw.ui.dialog.SettingsDialog.editSettings(viewer, app.getSettings());
                break;
            case SHOW_TOOLBAR:
                viewer.showOrHideToolBar();
                break;
            case SHOW_STATUSBAR:
                viewer.showOrHideStatusBar();
                break;
            case SHOW_SIDEBAR:
                viewer.showOrHideSideBar();
                break;
            case SHOW_ABOUT:
                pw.phylame.imabw.ui.dialog.AboutDialog.showAbout(viewer);
                break;
            case EDIT_META:
                viewProperties(book);
                break;
            default:
                System.out.println(cmdID);
                break;
        }
    }

    public void onTreeAction(Object actionID) {
        switch ((String) actionID) {
            default:
                System.out.println(actionID);
                break;
        }
    }

    public void onTabAction(Object actionID) {
        switch ((String) actionID) {
            case TAB_CLOSE:
                mainPane.closeActiveTab();
                break;
            case TAB_CLOSE_OTHERS:
                mainPane.closeOtherTabs();
                break;
            case TAB_CLOSE_UNMODIFIED:
                mainPane.closeUnmodifiedTabs();
                break;
            case TAB_CLOSE_ALL:
                mainPane.closeAllTabs();
                break;
            case TAB_SELECT_NEXT:
                mainPane.nextTab();
                break;
            case TAB_SELECT_PREVIOUS:
                mainPane.prevTab();
                break;
        }
    }
}
