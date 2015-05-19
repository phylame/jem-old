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
import pw.phylame.imabw.ui.Viewer;
import pw.phylame.imabw.ui.com.EditorTab;
import pw.phylame.imabw.ui.com.PartNode;
import pw.phylame.imabw.ui.com.UIFactory;
import pw.phylame.imabw.ui.dialog.PartPropertiesDialog;
import pw.phylame.imabw.ui.dialog.PartSelectionDialog;
import pw.phylame.ixin.ITextEdit;
import pw.phylame.ixin.IToolkit;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Jem;
import pw.phylame.jem.core.Part;
import pw.phylame.tools.DateUtils;
import pw.phylame.tools.StringUtils;
import pw.phylame.tools.file.FileNameUtils;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Date;
import java.util.ArrayList;

/**
 * The manager.
 */
public class Manager implements Constants {
    private static Log LOG = LogFactory.getLog(Manager.class);

    /** The application */
    private Imabw app = Imabw.getInstance();

    private Viewer viewer;

    private Worker worker;

    private Task task = null;

    public Manager(Viewer viewer, Worker worker) {
        this.viewer = viewer;
        this.worker = worker;
    }

    /** Begin manager works */
    public void begin() {
        viewer.setVisible(true);
        viewer.setStatusText(app.getText("Frame.Statusbar.Ready"));

        String[] argv = app.getArguments();
        if (argv.length < 1 || !openFile(new File(argv[0]))) {
            newFile(app.getText("Common.NewBookTitle"));
        }
    }

    /** Stop manager works */
    public void stop() {
        if (!maybeSave(app.getText("Dialog.Exit.Title"))) {
            return;
        }
        if (task != null) {
            task.cleanup();
        }
        app.exit(0);
    }

    /** Update frame title */
    private void updateTitle() {
        // book_title - [book_author] - [imported]? - [path]? - title version
        StringBuilder sb = new StringBuilder(task.getBook().getTitle());
        if (task.isModified()) {
            sb.append("*");
        }
        sb.append(" - ");
        String author = task.getBook().getAuthor();
        if ("".equals(author)) {
            author = app.getText("Common.NonAuthor");
        }
        sb.append("[").append(author).append("] - ");
        if (!task.getFormat().equals(Jem.PMAB_FORMAT)) {     // from other format
            sb.append("[").append(app.getText("Frame.Title.Imported")).append("] - ");
        }
        if (task.getSource() != null) {
            sb.append(task.getSource().getPath()).append(" - ");
        }
        sb.append(app.getText("App.Name")).append(" ").append(VERSION);
        viewer.setTitle(sb.toString());
    }

    /**
     * Notifies book has been modified.
     * @param message the message
     */
    public void notifyModified(String message) {
        task.setModified(true);
        updateTitle();
        if (message != null) {
            viewer.setStatusText(message);
        }
    }

    /**
     * Checks book is modified and asks saving
     * @return <tt>true</tt> if saved changes, otherwise <tt>false</tt>.
     */
    private boolean maybeSave(String title) {
        if (task == null || ! task.isModified()) {
            return true;
        }
        Object[] options = {app.getText("Dialog.ButtonSave"), app.getText("Dialog.ButtonDiscard"),
                app.getText("Dialog.ButtonCancel")};
        int ret = JOptionPane.showOptionDialog(viewer, app.getText("Dialog.Save.LabelSaveTip"),
                title,
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

    private void setTask(Task task) {
        if (this.task != null) {
            this.task.cleanup();
            viewer.closeAllTabs();
        }

        this.task = task;

        updateTitle();

        if (this.task.getSource() != null) {       // enable file details menu item
            viewer.getMenuAction(FILE_DETAILS).setEnabled(true);
        } else {
            viewer.getMenuAction(FILE_DETAILS).setEnabled(false);
        }

        viewer.setBook(this.task.getBook());

        viewer.setContentsLocked(false);
        viewer.focusToTreeWindow();
    }

    private void newFile(String bookTitle) {
        if (! maybeSave(app.getText("Dialog.NewBook.Title"))) {
            return;
        }
        Task task = worker.newBook(viewer, app.getText("Dialog.NewBook.Title"), bookTitle);
        if (task == null) {
            return;
        }

        setTask(task);
        viewer.setStatusText(app.getText("Task.CreatedBook", this.task.getBook().getTitle()));
    }

    private boolean openFile(File file) {
        if (! maybeSave(app.getText("Dialog.OpenBook.Title"))) {
            return false;
        }
        String title = app.getText("Dialog.OpenBook.Title");
        // 1. select book file if not exist
        if (file == null) {
            file = worker.selectOpenBook(viewer, title);
        }
        if (file == null) {
            return false;    // no selection
        }
        String format = FileNameUtils.extensionName(file.getPath());

        // 2. get parse arguments
        Map<String, Object> kw = worker.getParseArguments(viewer, format);
        if (kw == null) {
            return false;
        }

        // show progress dialog
//        ProgressDialog progressDialog = new ProgressDialog(viewer, title,
//                app.getText("Dialog.OpenBook.ProgressText", file.getPath()));
//        progressDialog.start();

        Task task = worker.openBook(viewer, title, file, format, kw);
        if (task == null) {
//            progressDialog.stop();
            return false;
        }

        setTask(task);
        viewer.setStatusText(app.getText("Task.OpenedBook", this.task.getSource().getPath()));

//        progressDialog.stop();
        return true;
    }

    private void saveFile() {
        viewer.cacheAllTabs();

        if (! worker.saveBook(viewer, app.getText("Dialog.SaveBook.Title"), task)) {
            return;
        }

        updateTitle();
        if (this.task.getSource() != null) {       // enable file details menu item
            viewer.getMenuAction(FILE_DETAILS).setEnabled(true);
        } else {
            viewer.getMenuAction(FILE_DETAILS).setEnabled(false);
        }

        viewer.setStatusText(app.getText("Task.SavedBook", task.getBook().getTitle(),
                task.getSource().getPath()));
    }

    private void exportFile() {
        viewer.cacheAllTabs();

        File path = worker.exportBook(viewer, app.getText("Dialog.SaveBook.Title"),
                task.getBook(), task);
        if (path != null) {
            worker.showMessage(viewer, app.getText("Dialog.SaveBook.Title"),
                    app.getText("Task.SavedBook", task.getBook().getTitle(), path.getPath()));
        }
    }

    private void fileDetails(File file) {
        ArrayList<Object[]> info = new ArrayList<>();
        info.add(new Object[]{app.getText("Dialog.FileDetails.Name"), file.getName()});
        info.add(new Object[]{app.getText("Dialog.FileDetails.Path"), file.getAbsoluteFile().getParent()});
        info.add(new Object[]{app.getText("Dialog.FileDetails.Format"),
                FileNameUtils.extensionName(file.getPath()).toUpperCase()});
        info.add(new Object[]{app.getText("Dialog.FileDetails.Size"), worker.formatSize(file.length())});
        info.add(new Object[]{app.getText("Dialog.FileDetails.Date"),
                DateUtils.formatDate(new Date(file.lastModified()), "yyyy-M-d H:m:s")});
        worker.showMessage(viewer, app.getText("Dialog.FileDetails.Title"),
                UIFactory.infoLabel(info.toArray(new Object[0][])));
    }

    private void viewProperties(Part part) {
        if (PartPropertiesDialog.viewProperties(viewer, part)) {    // modified
            notifyModified(null);
        }
    }

    // ********************
    // ** Search functions
    // ********************
    private String findText = null;

    /** Find {@code str} from {@code fromIndex} by order {@code reserved} in active editor. */
    private void findText(String str, int fromIndex, boolean reserved) {
        EditorTab tab = viewer.getActiveTab();
        if (tab == null) {
            return;
        }
        if (str == null || "".equals(str)) {
            str = worker.inputText(viewer, app.getText("Dialog.Find.Title"),
                    app.getText("Dialog.Find.Tip"), findText);
        }
        if (str == null || "".equals(str)) {
            return;
        }
        ITextEdit editor = tab.getTextEdit();
        if (fromIndex == -1) {
            /* search from current position */
            fromIndex = editor.getCaretPosition();
        }
        String text = editor.getText();
        int index;
        if (reserved) {
            index = text.lastIndexOf(str, fromIndex);
        } else {
            index = text.indexOf(str, fromIndex);
        }
        findText = str;
        if (index < 0) {
            if (reserved) {     // find previous
                viewer.getMenuAction(FIND_PREVIOUS).setEnabled(false);
            } else {            // find next
                viewer.getMenuAction(FIND_NEXT).setEnabled(false);
            }
            worker.showWarning(viewer, app.getText("Dialog.Find.Title"),
                    app.getText("Dialog.Find.NotFound", str));
        } else {
            viewer.getMenuAction(FIND_NEXT).setEnabled(true);
            viewer.getMenuAction(FIND_PREVIOUS).setEnabled(true);
            editor.setCaretPosition(index);
        }
    }

    /** Repeat last find operation whit order {@code reserved} */
    private void repeatFind(boolean reserved) {
        EditorTab tab = viewer.getActiveTab();
        if (tab == null) {
            return;
        }
        ITextEdit editor = tab.getTextEdit();
        /* not have find operator */
        if (findText == null) {
            return;
        }
        int index;
        if (reserved) {
            index = editor.getCaretPosition()-findText.length();
        } else {
            index = editor.getCaretPosition()+findText.length();
        }
        findText(findText, index, reserved);
    }

    private void replaceText() {
        EditorTab tab = viewer.getActiveTab();
        if (tab == null) {
            return;
        }
        ITextEdit editor = tab.getTextEdit();
        if (editor == null) {
            return;
        }
//        String str = worker.inputText(viewer, app.getText("Dialog.Replace.Title"), app.getText("Dialog.Replace.Tip"), findText);
//        if (str == null || "".equals(str)) {
//            return;
//        }
        System.out.println("Replace string");
    }

    private void gotoLine() {
        EditorTab tab = viewer.getActiveTab();
        if (tab == null) {
            return;
        }
        ITextEdit editor = tab.getTextEdit();
        String str = worker.inputText(viewer, app.getText("Dialog.Goto.Title"),
                app.getText("Dialog.Goto.Tip"), Integer.toString(editor.getCurrentRow() + 1));
        if (str == null || "".equals(str)) {
            return;
        }
        try {
            editor.gotoLine(Integer.parseInt(str) - 1);
            viewer.focusToEditorWindow();
        } catch (NumberFormatException e) {
            worker.showError(viewer, app.getText("Dialog.Goto.Title"), app.getText("Dialog.Goto.InvalidNumber", str));
        } catch (javax.swing.text.BadLocationException e) {
            worker.showError(viewer, app.getText("Dialog.Goto.Title"), app.getText("Dialog.Goto.NoSuchLine", str));
        }
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
                exportFile();
                break;
            case FILE_DETAILS:
                fileDetails(task.getSource());
                break;
            case EXIT_APP:
                stop();
                break;
            case EDIT_PREFERENCE:
                pw.phylame.imabw.ui.dialog.SettingsDialog.editSettings(viewer);
                break;
            case SHOW_TOOLBAR:
                viewer.showOrHideToolBar();
                app.getConfig().setShowToolbar(viewer.isToolBarVisible());
                break;
            case SHOW_STATUSBAR:
                viewer.showOrHideStatusBar();
                app.getConfig().settShowStatusbar(viewer.isStatusBarVisible());
                break;
            case SHOW_SIDEBAR:
                viewer.showOrHideSideBar();
                app.getConfig().setShowSidebar(viewer.isSideBarVisible());
                break;
            case FIND_TEXT:
                findText(null, -1, false);
                break;
            case FIND_NEXT:
                repeatFind(false);
                break;
            case FIND_PREVIOUS:
                repeatFind(true);
                break;
            case FIND_AND_REPLACE:
                replaceText();
                break;
            case GO_TO_POSITION:
                gotoLine();
                break;
            case SHOW_ABOUT:
                pw.phylame.imabw.ui.dialog.AboutDialog.showAbout(viewer);
                break;
            case DO_ACTION:
                String cmd = worker.inputText(viewer, app.getText("Dialog.DoAction.Title"),
                        app.getText("Dialog.DoAction.Tip"), null);
                if (StringUtils.isEmpty(cmd)) {
                    return;
                }
                onCommand(cmd);
                break;
            case BOOK_ATTRIBUTES:
                viewProperties(task.getBook());
                break;
            default:
                System.out.println("menu action: "+cmdID);
                break;
        }
    }

    // **************************
    // ** Tree action function
    // **************************
    private void viewChapter() {
        TreePath[] paths = viewer.getSelectedPaths();
        if (paths == null) {
            return;
        }
        for (TreePath path: paths) {
            PartNode node = PartNode.getPartNode(path);
            if (! node.isLeaf()) {  // chapter section
                continue;
            }
            Part part = node.getPart();
            EditorTab tab = viewer.findTab(part);
            if (tab == null) {
                tab = viewer.newTab(part);
            }
            viewer.switchToTab(tab);
        }
    }

    private void newChapter() {
        if (viewer.isContentsLocked()) {
            return;
        }
        TreePath path = viewer.getSelectedPath();
        if (path == null) {
            return;
        }
        PartNode node = PartNode.getPartNode(path);
        Part part = node.getPart();
        if (! part.isSection() && node.getParent() != null) {   // not section and not root
            if (! worker.showConfirm(app.getViewer(), app.getText("Dialog.NewChapter.Title"),
                    app.getText("Dialog.NewChapter.NonSection", part.getTitle()))) {
                return;
            }
        }
        PartNode sub = worker.newChapter(viewer, node, app.getText("Dialog.NewChapter.Title"));
        if (sub == null) {
            return;
        }
        viewer.appendedNode(node);
        // TODO focus to new node
        viewer.expandPath(path.getParentPath());
        viewer.focusToNode(path, sub);
        notifyModified(app.getText("Task.CreatedChapter", sub.getPart().getTitle(), part.getTitle()));
    }

    private void insertChapter() {
        if (viewer.isContentsLocked()) {
            return;
        }
        TreePath path = viewer.getSelectedPath();
        if (path == null || path.getPathCount() == 1) {     // no selection or root
            return;
        }
        PartNode sub = worker.newChapter(viewer, null, app.getText("Dialog.InsertChapter.Title"));
        if (sub == null) {
            return;
        }
        PartNode node = PartNode.getPartNode(path);
        PartNode parent = (PartNode) node.getParent();
        int index = parent.getIndex(node);
        parent.insertNode(sub, index);
        viewer.insertedNode(parent, index);
        // TODO focus to new node
        index = viewer.getRowForPath(path);
        viewer.focusToRow(index-1);
        notifyModified(app.getText("Task.InsertedChapter", sub.getPart().getTitle(),
                node.getPart().getTitle()));
    }

    private void saveAsPart() {
        TreePath path = viewer.getSelectedPath();
        if (path == null) {
            return;
        }
        viewer.cacheAllTabs();

        PartNode node = PartNode.getPartNode(path);
        Part part = node.getPart();

        Book book = Jem.toBook(part);
        if (! book.isSection()) {   // add new chapter with text
            book.newChapter(app.getText("Common.TextChapterTitle"), part.getSource());
        }
        File file = worker.exportBook(viewer, app.getText("Dialog.SaveBook.Title"), book, task);
        if (file != null) {
            worker.showMessage(viewer, app.getText("Dialog.SaveBook.Title"),
                    app.getText("Task.SavedBook", part.getTitle(), file.getPath()));
        }
    }

    private void renameChapter() {
        if (viewer.isContentsLocked()) {
            return;
        }
        TreePath path = viewer.getSelectedPath();
        if (path == null) {
            return;
        }
        PartNode node = PartNode.getPartNode(path);
        Part part = node.getPart();
        String oldTitle = part.getTitle();
        String newTitle = worker.inputLoop(viewer, app.getText("Dialog.RenameChapter.Title", part.getTitle()),
                app.getText("Dialog.RenameChapter.Tip"),
                app.getText("Dialog.RenameChapter.NoInput"), oldTitle);
        if (newTitle == null) {
            return;
        }

        part.setTitle(newTitle);
        viewer.updatedNode(node);
        notifyModified(app.getText("Task.RenamedChapter", oldTitle, newTitle));
    }

    private void moveChapters() {
        if (viewer.isContentsLocked()) {
            return;
        }
        TreePath[] paths = viewer.getSelectedPaths();
        if (paths == null || paths[0].getPathCount() == 1) {    // null or root
            return;
        }
        // select destination
        TreePath destPath = PartSelectionDialog.selectPart(viewer,
                app.getText("Dialog.MoveChapter.Title"), PartSelectionDialog.CHAPTER_OR_SECTION,
                viewer.getRootNode(), null);
        if (destPath == null) {
            return;
        }
        PartNode destNode = PartNode.getPartNode(destPath);
        if (destNode.isLeaf()) {    // base chapter
            if (! worker.showConfirm(viewer, app.getText("Dialog.MoveChapter.Title"),
                    app.getText("Dialog.MoveChapter.EmptyChapter", destNode.getPart().getTitle()))) {
                return;
            }
        }
        int count = 0;
        ArrayList<PartNode> ignoredNodes = new ArrayList<>();
        for (TreePath path: paths) {
            PartNode node = PartNode.getPartNode(path);
            // ignore: same one, already in, is ancestor
            if (node == destNode || destNode.isNodeChild(node) || destNode.isNodeAncestor(node)) {
                ignoredNodes.add(node);
                continue;
            }
            PartNode parentNode = (PartNode) node.getParent();
            int index = parentNode.getIndex(node);
            parentNode.removeNode(node);
            viewer.removedNode(parentNode, index, node);

            destNode.appendNode(node);
            viewer.appendedNode(destNode);
            ++count;
        }
        viewer.closeTab(destNode.getPart());  // close dest part tab if opened
        viewer.focusToPath(destPath);
        if (ignoredNodes.size() != 0) {     // has ignored item
            String sep = "\n  ";
            String str = sep+StringUtils.join(ignoredNodes, sep);
            worker.showMessage(viewer, app.getText("Dialog.MoveChapter.Title"),
                    app.getText("Dialog.MoveChapter.IgnoreTip", str));
        }
        notifyModified(app.getText("Task.MovedChapter", count, destNode.getPart().getTitle()));
    }

    private void deleteChapters() {
        if (viewer.isContentsLocked()) {
            return;
        }
        TreePath[] paths = viewer.getSelectedPaths();
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

            int index = parent.getIndex(node);
            viewer.closeTab(node.getPart());
            parent.removeNode(node);

            viewer.removedNode(parent, index, node);

        }
//        viewer.focusToRoot();
        notifyModified(app.getText("Task.DeletedChapter", paths.length));
    }

    private void mergeChapters() {
        if (viewer.isContentsLocked()) {
            return;
        }
        TreePath[] paths = viewer.getSelectedPaths();
        if (paths == null || paths.length < 2 || paths[0].getPathCount() == 1) {    // null, one or root
            return;
        }
        // select destination
        TreePath destPath = PartSelectionDialog.selectPart(viewer,
                app.getText("Dialog.MergeChapter.Title"), PartSelectionDialog.CHAPTER_ONLY,
                viewer.getRootNode(), null);
        if (destPath == null) {
            return;
        }
        for (TreePath path: paths) {
            PartNode node = PartNode.getPartNode(path);
            if (! node.isLeaf()) {
                worker.showWarning(viewer, app.getText("Dialog.MergeChapter.Title"),
                        app.getText("Dialog.MergeChapter.IsSection", node.getPart().getTitle()));
                return;
            }
        }
        PartNode destNode = PartNode.getPartNode(destPath);
        Part destPart = destNode.getPart();
        JCheckBox cb = new JCheckBox(app.getText("Dialog.MergerChapter.WithTitle"));
        Object[] msg = {app.getText("Dialog.MergeChapter.EmptyChapter", destPart.getTitle()), cb};
        if (! worker.showConfirm(viewer, app.getText("Dialog.MergeChapter.Title"), msg)) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (TreePath path: paths) {
            PartNode node = PartNode.getPartNode(path);
            PartNode parent = (PartNode) node.getParent();
            if (node == destNode) {     // same node
                continue;
            }
            Part part = node.getPart();
            if (cb.isSelected()) {
                sb.append(part.getTitle()).append("\n");
            }
            int index = parent.getIndex(node);
            viewer.closeTab(part);    // cache part content
            try {
                sb.append(part.getText()).append("\n");
            } catch (IOException e) {
                LOG.debug("cannot get text content of " + part.getTitle(), e);
            }
            parent.removeNode(node);
            viewer.removedNode(parent, index, node);
            ++count;
        }
        EditorTab tab = viewer.findTab(destPart);
        if (tab != null) {      // is opened
            tab.getTextEdit().setText(sb.toString());
            viewer.switchToTab(tab);
        } else {
            destPart.getSource().setRaw(sb.toString());
            viewer.newTab(destPart);
        }
        viewer.focusToPath(destPath);
        notifyModified(app.getText("Task.MergedChapter", count, destPart.getTitle()));
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
        ArrayList<Part> results = worker.findParts(part, key);
        System.out.println(results.size());
    }

    public void onTreeAction(Object actionID) {
        switch ((String) actionID) {
            case VIEW_CHAPTER:
                viewChapter();
                break;
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
                TreePath path = viewer.getSelectedPath();
                if (path != null) {
                    viewProperties(PartNode.getPartNode(path).getPart());
                }
                break;
            case SEARCH_CHAPTER:
                path = viewer.getSelectedPath();
                if (path != null) {
                    searchChapters(PartNode.getPartNode(path).getPart());
                }
                break;
            case REFRESH_CONTENTS:
                viewer.refreshNode(viewer.getSelectedNode());
                break;
            case LOCK_CONTENTS:
                viewer.setContentsLocked(!viewer.isContentsLocked());
                break;
            default:
                System.out.println("tree action: "+actionID);
                break;
        }
    }

    public void onTabAction(Object actionID) {
        switch ((String) actionID) {
            case CLOSE_ACTIVE_TAB:
                viewer.closeActiveTab();
                break;
            case CLOSE_OTHER_TABS:
                viewer.closeOtherTabs();
                break;
            case CLOSE_UNMODIFIED_TABS:
                viewer.closeUnmodifiedTabs();
                break;
            case CLOSE_ALL_TABS:
                viewer.closeAllTabs();
                break;
            case SELECT_NEXT_TAB:
                viewer.nextTab();
                break;
            case SELECT_PREVIOUS_TAB:
                viewer.previousTab();
                break;
            default:
                System.out.println("tab action: "+actionID);
                break;
        }
    }
}