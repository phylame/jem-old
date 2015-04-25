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
import pw.phylame.imabw.ui.com.PartNode;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Jem;
import pw.phylame.jem.core.Part;

import javax.swing.*;
import javax.swing.tree.TreePath;
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
        viewer.setStatusText(app.getText("Frame.Statusbar.Ready"));

        String[] argv = app.getArguments();
        if (argv.length < 1 || ! openFile(new File(argv[0]))) {
            newFile(app.getText("Common.NewBookTitle"));
        }
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
            name = String.format("%s.%s", book.getTitle(), Jem.PMAB_FORMAT);
        }
        return name;
    }

    /** Update frame title */
    private void updateTitle() {
        // book_title - [book_author] - [imported] - path - app_info
        StringBuilder sb = new StringBuilder(book.getTitle());
        if (modified) {
            sb.append("*");
        }
        sb.append(" - ");
        String author = book.getAuthor();
        if ("".equals(author)) {
            author = app.getText("Common.NonAuthor");
        }
        sb.append("[").append(author).append("] - ");
        if (! format.equals(Jem.PMAB_FORMAT)) {     // from other format
            sb.append("[").append(app.getText("Frame.Title.Imported")).append("] - ");
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

    private void newFile(String title) {
        if (! maybeSave(app.getText("Dialog.New.Title"))) {
            return;
        }
        Book _book = worker.newBook(title);
        if (_book == null) {
            return;
        }
        if (book != null) {     // clean old book
            book.cleanup();
        }
        mainPane.closeAllTabs();
        book = _book;

        source = null;
        viewer.getMenuAction(FILE_DETAILS).setEnabled(false);   // disable file details menu

        format = Jem.PMAB_FORMAT;
        modified = false;

        mainPane.setBook(book);
        updateTitle();

        mainPane.focusToTreeWindow();
        viewer.setStatusText(app.getText("Task.NewedBook", book.getTitle()));
    }

    private boolean openFile(File file) {
        if (! maybeSave(app.getText("Dialog.Open.Title"))) {
            return false;
        }
        Book _book = worker.openBook(file, app.getText("Dialog.OpenBook.Title"));
        if (_book == null) {
            return false;
        }
        if (book != null) {     // clean old book
            book.cleanup();
        }
        mainPane.closeAllTabs();
        book = _book;

        Object o = book.getAttribute("source_file", null);
        assert o instanceof File;
        source = (File) o;
        viewer.getMenuAction(FILE_DETAILS).setEnabled(true);

        o = book.getAttribute("source_format", null);
        assert o instanceof String;
        format = (String) o;

        modified = false;

        mainPane.setBook(book);
        updateTitle();

        mainPane.focusToTreeWindow();
        viewer.setStatusText(app.getText("Task.OpenedBook", source.getPath()));
        return true;
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
        pw.phylame.imabw.ui.dialog.PropertiesDialog.viewProperties(viewer, part);
    }

    public void onCommand(Object cmdID) {
        switch ((String) cmdID) {
            case NEW_FILE:
                newFile(null);
                break;
            case OPEN_FILE:
                openFile(null);
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
                System.out.println("menu action: "+cmdID);
                break;
        }
    }

    // **************************
    // ** Tree action function
    // **************************
    public void viewPart(Part part) {
        EditorTab tab = mainPane.findEditorTab(part);
        if (tab == null) {
            tab = mainPane.newTab(part);
        }
        mainPane.switchToTab(tab);
    }

    private void newChapter() {
        TreePath treePath = mainPane.getSelectedPath();
        if (treePath == null) {
            return;
        }
        PartNode node = PartNode.getPartNode(treePath);
        Part part = node.getPart();
        if (! part.isSection()) {
            if (! worker.showConfirm(app.getViewer(), app.getText("Dialog.NewChapter.Title"),
                    app.getText("Dialog.NewChapter.NonSection", part.getTitle()))) {
                return;
            }
        }
        PartNode sub = worker.newChapter(node, app.getText("Dialog.NewChapter.Title"));
        if (sub == null) {
            return;
        }
        mainPane.refreshNode(node);
        mainPane.expandTreePath(treePath);
        mainPane.focusToNode(treePath, sub);
        notifyModified(app.getText("Task.NewedChapter", sub.getPart().getTitle(), part.getTitle()));
    }

    private void insertChapter() {
        TreePath treePath = mainPane.getSelectedPath();
        if (treePath == null || treePath.getPathCount() == 1) {     // no selection or root
            return;
        }
        PartNode node = PartNode.getPartNode(treePath);
        PartNode parent = (PartNode) node.getParent();
        PartNode sub = worker.newChapter(null, app.getText("Dialog.InsertChapter.Title"));
        if (sub == null) {
            return;
        }
        int index = parent.getIndex(node);
        parent.getPart().insert(index, sub.getPart());
        parent.insert(sub, index);
        mainPane.refreshNode(parent);
        mainPane.focusToNode(treePath.getParentPath(), sub);
        notifyModified(app.getText("Task.InsertedChapter", sub.getPart().getTitle(), node.getPart().getTitle()));
    }

    private void saveAsPart() {
        PartNode node = mainPane.getSelectedNode();
        if (node == null) {
            return;
        }
        Part part = node.getPart();
    }

    private void renameChapter() {
        PartNode node = mainPane.getSelectedNode();
        if (node == null) {
            return;
        }
        Part part = node.getPart();
        String oldTitle = part.getTitle();
        String text = worker.inputText(app.getViewer(), app.getText("Dialog.RenameChapter.Title", part.getTitle()),
                app.getText("Dialog.RenameChapter.Tip"), oldTitle);
        if (text == null) {
            // nothing
        } else if (text.length() == 0) {
            worker.showError(app.getViewer(), app.getText("Dialog.RenameChapter.Title", part.getTitle()),
                    app.getText("Dialog.RenameChapter.NoInput"));
        } else {
            part.setTitle(text);
            mainPane.refreshNode(node);
            notifyModified(app.getText("Task.RenamedChapter", oldTitle, text));
        }
    }

    public void onTreeAction(Object actionID) {
        switch ((String) actionID) {
            case TREE_NEW:
                newChapter();
                break;
            case TREE_INSERT:
                insertChapter();
                break;
            case TREE_IMPORT:
                break;
            case TREE_SAVE_AS:
                saveAsPart();
                break;
            case TREE_MOVE:
                break;
            case TREE_RENAME:
                renameChapter();
                break;
            case TREE_DELETE:
                break;
            case TREE_MERGE:
                break;
            case TREE_PROPERTIES:
                PartNode node = mainPane.getSelectedNode();
                if (node != null) {
                    viewProperties(node.getPart());
                }
                break;
            default:
                System.out.println("tree action: "+actionID);
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
            default:
                System.out.println("tab action: "+actionID);
                break;
        }
    }
}