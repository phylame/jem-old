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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pw.phylame.imabw.ui.Viewer;
import pw.phylame.imabw.ui.com.EditorTab;
import pw.phylame.imabw.ui.com.MainPane;
import pw.phylame.imabw.ui.com.PartNode;
import pw.phylame.imabw.ui.com.UIFactory;
import pw.phylame.ixin.IToolkit;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Jem;
import pw.phylame.jem.core.Part;
import pw.phylame.tools.file.FileNameUtils;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.io.File;
import java.util.*;

/**
 * The manager.
 */
public class Manager implements Constants {
    private static Log LOG = LogFactory.getLog(Manager.class);

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

    /** Update frame title */
    private void updateTitle() {
        // book_title - [book_author] - [imported] - [path]? - app_info
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
        if (source != null) {
            sb.append(source.getPath()).append(" - ");
        }
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
        int ret = JOptionPane.showOptionDialog(viewer, app.getText("Dialog.Save.LabelSaveTip"), title,
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                IToolkit.createImageIcon(":/res/img/dialog/save.png"),
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

        mainPane.setContentsLocked(false);
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

        mainPane.setContentsLocked(false);
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

        viewer.setStatusText(app.getText("Task.SavedBook", book.getTitle(), source.getPath()));
    }

    private void saveAsFile() {
        File path = worker.saveBook(book, null, format, app.getText("Dialog.SaveBook.Title"));
        if (path != null) {
            viewer.setStatusText(app.getText("Task.SavedBook", book.getTitle(), path.getPath()));
        }
    }

    private void fileDetails(File file) {
        List<Object[]> info = new ArrayList<>();
        info.add(new Object[]{app.getText("Dialog.FileDetails.Name"), source.getName()});
        info.add(new Object[]{app.getText("Dialog.FileDetails.Path"), source.getParent()});
        info.add(new Object[]{app.getText("Dialog.FileDetails.Format"), FileNameUtils.extensionName(source.getPath())});
        info.add(new Object[]{app.getText("Dialog.FileDetails.Size"), source.length()});
        info.add(new Object[]{app.getText("Dialog.FileDetails.Date"), new Date(source.lastModified())});
        worker.showMessage(viewer, app.getText("Dialog.FileDetails.Title", source.getPath()),
                UIFactory.infoLabel(info.toArray(new Object[0][])));
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
            case FILE_DETAILS:
                fileDetails(source);
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
        parent.insertNode(sub, index);
        mainPane.refreshNode(parent);
        mainPane.focusToNode(treePath.getParentPath(), sub);
        notifyModified(app.getText("Task.InsertedChapter", sub.getPart().getTitle(), node.getPart().getTitle()));
    }

    private void saveAsPart() {
        TreePath treePath = mainPane.getSelectedPath();
        if (treePath == null) {
            return;
        }
        PartNode node = PartNode.getPartNode(treePath);
        Part part = node.getPart();
        File path = worker.saveBook(Jem.toBook(part), null, Jem.PMAB_FORMAT, app.getText("Dialog.SaveBook.Title"));
        if (path != null) {
            viewer.setStatusText(app.getText("Task.SavedBook", part.getTitle(), path.getPath()));
        }
    }

    private void renameChapter() {
        TreePath treePath = mainPane.getSelectedPath();
        if (treePath == null) {
            return;
        }
        PartNode node = PartNode.getPartNode(treePath);
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
            mainPane.focusToPath(treePath);
            notifyModified(app.getText("Task.RenamedChapter", oldTitle, text));
        }
    }

    private void moveChapters() {
        TreePath[] paths = mainPane.getSelectedPaths();
        if (paths == null || paths[0].getPathCount() == 1) {    // null or root
            return;
        }
        // select destination
    }

    private void deleteChapters() {
        TreePath[] paths = mainPane.getSelectedPaths();
        if (paths == null || paths[0].getPathCount() == 1) {    // null or root
            return;
        }
        if (! worker.showConfirm(viewer, app.getText("Dialog.DeleteChapter.Title"),
                app.getText("Dialog.DeleteChapter.Tip", paths.length))) {
            return;
        }
        for (TreePath path: paths) {
            PartNode node = PartNode.getPartNode(path);
            PartNode parent = (PartNode) node.getParent();
            TreePath parentPath = path.getParentPath();

            mainPane.closeTab(node.getPart());
            parent.removeNode(node);

            mainPane.refreshNode(parent);
            mainPane.expandTreePath(parentPath);
        }
        mainPane.focusToRoot();
        notifyModified(app.getText("Task.DeletedChapter", paths.length));
    }

    private void mergeChapters() {
        TreePath[] paths = mainPane.getSelectedPaths();
        if (paths == null || paths[0].getPathCount() == 1) {    // null or root
            return;
        }
        // select destination
    }

    private void searchChapters(Part part) {
        String key = worker.inputText(viewer, app.getText("Dialog.SearchChapter.Title"),
                app.getText("Dialog.SearchChapter.Tip"), null);
        if (key == null) {
            return;
        }
        if (key.length() == 0) {
            worker.showError(viewer, app.getText("Dialog.SearchChapter.Title"), app.getText("Dialog.SearchChapter.NoInput"));
            return;
        }
        List<Part> results = worker.findParts(part, key);
        System.out.println(results.size());
    }

    public void onTreeAction(Object actionID) {
        switch ((String) actionID) {
            case NEW_CHAPTER:
                newChapter();
                break;
            case INSERT_CHAPTER:
                insertChapter();
                break;
            case SAVE_CHAPTER:
                saveAsPart();
                break;
            case MOVE_CHAPTER:
                moveChapters();
                break;
            case RENAME_CHAPTER:
                renameChapter();
                break;
            case DELETE_CHAPTER:
                deleteChapters();
                break;
            case MERGE_CHAPTER:
                mergeChapters();
                break;
            case TREE_PROPERTIES:
                TreePath path = mainPane.getSelectedPath();
                if (path != null) {
                    viewProperties(PartNode.getPartNode(path).getPart());
                }
                break;
            case SEARCH_CHAPTER:
                path = mainPane.getSelectedPath();
                if (path != null) {
                    searchChapters(PartNode.getPartNode(path).getPart());
                }
                break;
            case REFRESH_CONTENTS:
                mainPane.refreshNode(mainPane.getSelectedNode());
                break;
            case LOCK_CONTENTS:
                mainPane.setContentsLocked(!mainPane.isContentsLocked());
                break;
            default:
                System.out.println("tree action: "+actionID);
                break;
        }
    }

    public void onTabAction(Object actionID) {
        switch ((String) actionID) {
            case CLOSE_TAB:
                mainPane.closeActiveTab();
                break;
            case CLOSE_OTHER_TABS:
                mainPane.closeOtherTabs();
                break;
            case CLOSE_UNMODIFIED_TABS:
                mainPane.closeUnmodifiedTabs();
                break;
            case CLOSE_ALL_TABS:
                mainPane.closeAllTabs();
                break;
            case SELECT_NEXT_TAB:
                mainPane.nextTab();
                break;
            case SELECT_PREVIOUS_TAB:
                mainPane.prevTab();
                break;
            default:
                System.out.println("tab action: "+actionID);
                break;
        }
    }
}