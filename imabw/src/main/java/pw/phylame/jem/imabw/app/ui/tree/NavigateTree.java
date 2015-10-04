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

package pw.phylame.jem.imabw.app.ui.tree;

import pw.phylame.jem.core.Book;
import pw.phylame.gaf.ixin.IToolkit;
import pw.phylame.gaf.ixin.IMenuLabel;
import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.core.Jem;
import pw.phylame.jem.imabw.app.Imabw;
import pw.phylame.jem.imabw.app.Worker;
import pw.phylame.jem.imabw.app.data.*;
import pw.phylame.jem.imabw.app.ui.Editable;
import pw.phylame.jem.imabw.app.ui.Viewer;
import pw.phylame.jem.imabw.app.ui.dialog.DialogFactory;
import pw.phylame.jem.imabw.app.ui.dialog.WaitingWork;
import pw.phylame.jem.imabw.app.ui.editor.EditorTab;
import pw.phylame.jem.imabw.app.ui.editor.TabbedEditor;
import pw.phylame.util.StringUtils;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.tree.TreePath;

/**
 * Common navigate treeView component.
 */
public class NavigateTree extends JPanel implements Editable {
    private static Imabw app = Imabw.getInstance();

    private static final String CHAPTER_SEPARATOR = "->";

    private static Object[] NEW_MENU = {
            new IMenuLabel("Frame.Contents.NewMenu"),
            Imabw.NEW_CHAPTER, Imabw.IMPORT_CHAPTER
    };

    private static Object[] CONTEXT_MENU_MODEL = {
            NEW_MENU,
            Imabw.INSERT_CHAPTER,
            null,
            Imabw.EXPORT_CHAPTER,
            null,
            Imabw.RENAME_CHAPTER,
            null,
            Imabw.EDIT_DELETE,
            null,
            Imabw.JOIN_CHAPTER,
            null,
            Imabw.CHAPTER_PROPERTIES
    };

    public NavigateTree(Viewer viewer) {
        this.viewer = viewer;
        undoManager = new TreeUndoManager(this);
        treeModel = new BookTreeModel(this, null);
        createComponents();
        initializeTree();
    }

    private void createComponents() {
        optionsPane = new OptionsPane(this);
        treeView = new JTree(treeModel);

        setLayout(new BorderLayout());
        add(optionsPane, BorderLayout.PAGE_START);
        add(new JScrollPane(treeView), BorderLayout.CENTER);
    }

    private void initializeTree() {
        treeView.setEditable(true);
        treeView.setDragEnabled(true);
        treeView.setDropMode(DropMode.USE_SELECTION);
        treeView.setCellRenderer(new BookCellRender());

        contextMenu = new JPopupMenu();
        IToolkit.addMenuItems(contextMenu, CONTEXT_MENU_MODEL, viewer.getMenuActions(), viewer);

        // select chapter when right click, double-click to edit chapter text
        treeView.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    TreePath path = treeView.getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        editText(path);
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        if (! treeView.isFocusOwner()) {
                            treeView.requestFocus();
                        }
                    }
                    if (!e.isControlDown()) {
                        TreePath path = treeView.getPathForLocation(e.getX(), e.getY());
                        // select the path if not in selection
                        if (path != null && !treeView.isPathSelected(path)) {
                            treeView.setSelectionPath(path);
                        }
                    }
                    if (SwingUtilities.isRightMouseButton(e)) {
                        // show context menu
                        contextMenu.show(treeView, e.getX(), e.getY());
                    }
                }
            }
        });

        treeView.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                viewer.notifyActivated(NavigateTree.this);
                updateActions();
            }
        });

        treeView.addTreeSelectionListener(e -> updateActions());

        mapKeyboardAction(new ShowMenuAction());
        mapKeyboardAction(new EditChapterAction());
        mapKeyboardAction(new SelectChildrenAction());
    }

    public void mapKeyboardAction(Action action) {
        mapKeyboardAction((String) action.getValue(Action.ACTION_COMMAND_KEY),
                (KeyStroke) action.getValue(Action.ACCELERATOR_KEY), action);
    }

    public void mapKeyboardAction(String command, KeyStroke keyStroke, Action action) {
        treeView.getInputMap(WHEN_FOCUSED).put(keyStroke, command);
        treeView.getActionMap().put(command, action);
    }

    /**
     * Updates state of all default treeView actions
     *
     * @param enable <tt>true</tt> to enable those actions, otherwise <tt>false</tt>
     */
    public void updateTreeActions(boolean enable) {
        for (String actionId : Imabw.TREE_ACTIONS) {
            viewer.setActionEnable(actionId, enable);
        }
    }

    private void updateUndoRedoActions() {
        Action action = viewer.getMenuAction(Imabw.EDIT_UNDO);
        action.setEnabled(undoManager.canUndo());
        action.putValue(Action.NAME, undoManager.getUndoTitle());

        action = viewer.getMenuAction(Imabw.EDIT_REDO);
        action.setEnabled(undoManager.canRedo());
        action.putValue(Action.NAME, undoManager.getRedoTitle());
    }

    /**
     * Updates state of all default chapter actions.
     */
    public void updateActions() {
        if (!viewer.isActive(this)) {
            return;
        }
        // disable all common edit actions
        viewer.updateEditActions(false);

        viewer.getTabbedEditor().updateTextActions(false);

        viewer.setActionEnable(Imabw.FIND_CONTENT, false);
        viewer.setActionEnable(Imabw.GOTO_POSITION, false);

        // disable all treeView actions
        updateTreeActions(false);

        TreePath[] paths = treeView.getSelectionPaths();

        // no selection
        if (paths == null || paths.length == 0) {
            return;
        }

        viewer.setActionEnable(Imabw.EDIT_SELECT_ALL, true);
        viewer.setActionEnable(Imabw.EXPORT_CHAPTER, true);
        viewer.setActionEnable(Imabw.BOOK_EXTRA, true);

        if (paths.length == 1) {                        // single selection
            viewer.setActionEnable(Imabw.FIND_CONTENT, true);
            viewer.setActionEnable(Imabw.GOTO_POSITION, true);
            if (isReadonly()) {            // read-only
                viewer.setActionEnable(Imabw.CHAPTER_PROPERTIES, true);
            } else {
                viewer.updateEditActions(true);
                updateUndoRedoActions();
                viewer.setActionEnable(Imabw.EDIT_PASTE, canPaste());
                updateTreeActions(true);
                viewer.setActionEnable(Imabw.JOIN_CHAPTER, false);
            }
        } else {                                        // multi selection
            if (!isReadonly()) {          // read-write
                viewer.updateEditActions(true);
                updateUndoRedoActions();
                viewer.setActionEnable(Imabw.EDIT_PASTE, canPaste());
                viewer.setActionEnable(Imabw.JOIN_CHAPTER, true);
            }
        }

        // root is in selection
        if (treeView.isPathSelected(treeView.getPathForRow(0))) {
            // root cannot insert, save, move, delete, merge
            viewer.setActionEnable(Imabw.EDIT_CUT, false);
            viewer.setActionEnable(Imabw.EDIT_DELETE, false);
            viewer.setActionEnable(Imabw.INSERT_CHAPTER, false);
            viewer.setActionEnable(Imabw.EXPORT_CHAPTER, false);
            viewer.setActionEnable(Imabw.JOIN_CHAPTER, false);
        }
        viewer.setActionEnable(Imabw.EDIT_COPY, false);
    }

    public JTree getTree() {
        return treeView;
    }

    public Viewer getOwner() {
        return viewer;
    }

    public BookTreeModel getTreeModel() {
        return treeModel;
    }

    public TreeUndoManager getUndoManager() {
        return undoManager;
    }

    public boolean isReadonly() {
        return optionsPane.isReadonly();
    }

    public void setReadonly(boolean readonly) {
        optionsPane.setReadonly(readonly);
    }

    public void setBook(Book book) {
        treeModel.setBook(book);
        focusToRow(0);
        setReadonly(false);
    }

    public void editText(TreePath path) {
        Chapter chapter = (Chapter) path.getLastPathComponent();
        if (!chapter.isSection()) {
            TabbedEditor tabbedEditor = viewer.getTabbedEditor();
            EditorTab tab = tabbedEditor.findTab(chapter);
            if (tab == null) {
                tab = tabbedEditor.newTab(chapter, StringUtils.join(path.getPath(), CHAPTER_SEPARATOR));
            }
            tabbedEditor.activateTab(tab);
        }
    }

    public void insertChaptersInto(Chapter[] chapters, Chapter target, int[] indices) {
        if (chapters.length != indices.length) {
            throw new IllegalArgumentException("length of chapters and indices is different");
        }
        for (int i = 0; i < chapters.length; i++) {
            target.insert(indices[i], chapters[i]);
        }
        app.getActiveTask().chapterChildrenModified(target, true);
        treeModel.chaptersInserted(target, indices, chapters);
    }

    public void insertChapterInto(Chapter chapter, Chapter target, int index) {
        insertChaptersInto(new Chapter[]{chapter}, target, new int[]{index});
    }

    public void removeChaptersFrom(Chapter[] chapters, Chapter parent, int[] indices) {
        if (chapters.length != indices.length) {
            throw new IllegalArgumentException("length of chapters and indices is different");
        }
        for (int i = 0; i < chapters.length; i++) {
            Chapter chapter = parent.remove(indices[i]);
            viewer.getTabbedEditor().closeTab(chapter);
        }
        app.getActiveTask().chapterChildrenModified(parent, true);
        treeModel.chaptersRemoved(parent, indices, chapters);
    }

    public void removeChapterFrom(Chapter chapter, Chapter parent, int index) {
        removeChaptersFrom(new Chapter[]{chapter}, parent, new int[]{index});
    }

    public void updateChapterAttributes(Chapter chapter, Map<String, Object> newAttributes, String what) {
        HashMap<String, Object> oldAttributes = new HashMap<>();
        for (String name : newAttributes.keySet()) {
            oldAttributes.put(name, chapter.getAttribute(name));
        }
        chapter.updateAttributes(newAttributes);
        undoManager.attributesUpdated(chapter, oldAttributes, what);
        app.getActiveTask().chapterAttributeModified(chapter, true);
        attributesUpdated(chapter);
    }

    public void attributesUpdated(Chapter chapter) {
        treeModel.chapterUpdated(chapter);
        viewer.getTabbedEditor().updateTabTitle(chapter);
        updateUndoRedoActions();
    }

    public void textModified(Chapter chapter) {
        treeModel.chapterUpdated(chapter);
    }

    public void focusToRow(int row) {
        treeView.setSelectionRow(row);
        treeView.scrollRowToVisible(row);
    }

    public void focusToPath(TreePath path) {
        treeView.setSelectionPath(path);
        treeView.scrollPathToVisible(path);
    }

    public void focusToChapter(Chapter chapter) {
        focusToPath(getChapterPath(chapter));
    }

    public TreePath getChapterPath(Chapter chapter) {
        return new TreePath(BookTreeModel.getPathToRoot(chapter));
    }

    public void newChapter() {
        assert !isReadonly();
        TreePath[] paths = treeView.getSelectionPaths();
        assert paths != null && paths.length == 1;
        TreePath path = paths[0];

        String dialogTitle = app.getText("Dialog.NewChapter.Title");
        Chapter chapter = (Chapter) path.getLastPathComponent();
        String name = chapter.getTitle();
        // ask if node is leaf
        if (!chapter.isSection() && !BookTreeModel.isRoot(chapter)) {     // not section and book
            if (!DialogFactory.showConfirm
                    (viewer, dialogTitle, app.getText("Dialog.NewChapter.NoSection", name))) {
                return;
            }
        }

        Chapter sub = Worker.getInstance().newChapter(viewer, dialogTitle);
        if (sub == null) {
            return;
        }
        // close the editor if the chapter is editing
        viewer.getTabbedEditor().closeTab(chapter);
        int index = chapter.size();
        insertChapterInto(sub, chapter, index);

        // undo supported
        TreeUndoManager.UndoItem undoItem = new TreeUndoManager.UndoItem(new Chapter[]{sub}, chapter,
                new int[]{index});
        undoManager.chaptersInserted(new TreeUndoManager.UndoItem[]{undoItem},
                app.getText("Undo.Message.NewChapter"), paths);

        treeView.expandPath(path);
        focusToChapter(sub);

        app.notifyMessage(app.getText("Dialog.NewChapter.Finished", sub.getTitle(), name));
    }

    public void insertChapter() {
        assert !isReadonly();
        TreePath[] paths = treeView.getSelectionPaths();
        assert paths != null && paths.length == 1;

        String dialogTitle = app.getText("Dialog.InsertChapter.Title");
        Chapter chapter = (Chapter) paths[0].getLastPathComponent();

        Chapter newChapter = Worker.getInstance().newChapter(viewer, dialogTitle);
        if (newChapter == null) {
            return;
        }

        Chapter parent = chapter.getParent();
        int row = treeView.getMinSelectionRow(), index = parent.indexOf(chapter);
        insertChapterInto(newChapter, parent, index);

        // undo supported
        TreeUndoManager.UndoItem undoItem = new TreeUndoManager.UndoItem(new Chapter[]{newChapter}, parent,
                new int[]{index});
        undoManager.chaptersInserted(new TreeUndoManager.UndoItem[]{undoItem},
                app.getText("Undo.Message.InsertChapter"), paths);

        focusToRow(row);

        app.notifyMessage(
                app.getText("Dialog.InsertChapter.Finished", newChapter.getTitle(), parent.getTitle()));
    }

    public void importChapter() {
        assert !isReadonly();
        TreePath[] paths = treeView.getSelectionPaths();
        assert paths != null && paths.length == 1;

        String dialogTitle = app.getText("Dialog.ImportChapter.Title");

        OpenResult od = Worker.getInstance().selectOpenBook(viewer, dialogTitle, true, null, null);
        if (od == null) {   // no selection
            return;
        }

        DialogFactory.openWaiting(viewer, dialogTitle, "", app.getText("Dialog.ImportChapter.WaitingTip"),
                new ImportChapterWork(dialogTitle, paths[0], od), false);
    }

    public void exportChapter() {
        TreePath[] paths = treeView.getSelectionPaths();
        assert paths != null && paths.length == 1;

        Book book = Jem.toBook((Chapter) paths[0].getLastPathComponent());

        String title = app.getText("Dialog.ExportChapter.Title", book.getTitle());
        Worker worker = Worker.getInstance();
        OpenResult od = worker.selectSaveBook(viewer, title, null, null);
        if (od == null) {
            return;
        }
        Map<String, Object> kw = worker.getMakeArguments(viewer, od.getFormat());
        if (kw == null) {
            return;
        }

        MakerData md = new MakerData(book, od.getFile(), od.getFormat(), kw);
        DialogFactory.openWaiting(viewer, title,
                app.getText("Dialog.ExportChapter.TipText", md.file),
                app.getText("Dialog.ExportChapter.WaitingText"),
                new ExportChapterWork(title, md), false);
    }

    public void renameChapter() {
        assert !isReadonly();
        TreePath[] paths = treeView.getSelectionPaths();
        assert paths != null && paths.length == 1;

        Chapter chapter = (Chapter) paths[0].getLastPathComponent();
        String dialogTitle = app.getText("Dialog.RenameChapter.Title", chapter.getTitle());
        String oldTitle = chapter.getTitle();
        String newTitle = DialogFactory.inputText(viewer, dialogTitle,
                app.getText("Dialog.RenameChapter.InputTip"), oldTitle, true, false);

        if (newTitle == null) {
            return;
        }
        HashMap<String, Object> newAttr = new HashMap<>();
        newAttr.put(Chapter.TITLE, newTitle);
        updateChapterAttributes(chapter, newAttr, app.getText("Undo.Message.RenameChapter"));
        app.notifyMessage(app.getText("Dialog.RenameChapter.Finished", oldTitle, newTitle));
    }

    public void joinChapter() {

    }

    public void editExtra() {

    }

    public void undo() {
        if (undoManager.canUndo()) {
            undoManager.undo();
//            updateUndoRedoActions();
        }
    }

    public void redo() {
        if (undoManager.canRedo()) {
            undoManager.redo();
//            updateUndoRedoActions();
        }
    }

    public void cut() {
        assert !isReadonly();
        TreeClipboard.getInstance().cut();
    }

    public void copy() {
        throw new UnsupportedOperationException("unsupported copy operation");
    }

    public boolean canPaste() {
        return TreeClipboard.getInstance().canPaste();
    }

    public void paste() {
        assert !isReadonly();
        TreeClipboard.getInstance().paste();
    }

    public void delete() {
        assert !isReadonly();
        TreePath[] paths = treeView.getSelectionPaths();
        assert paths != null && paths.length > 0;

        String dialogTitle = app.getText("Dialog.DeleteChapter.Title");
        String tip;
        if (paths.length == 1) {
            Chapter ch = (Chapter) paths[0].getLastPathComponent();
            if (ch.isSection()) {
                tip = app.getText("Dialog.DeleteChapter.DeleteSectionTip", ch);
            } else {
                tip = app.getText("Dialog.DeleteChapter.DeleteChapterTip", ch);
            }
        } else {
            tip = app.getText("Dialog.DeleteChapter.DeleteSomeTip", paths.length);
        }

        if (!DialogFactory.showConfirm(viewer, dialogTitle, tip)) {
            return;
        }

        int row = treeView.getMinSelectionRow();

        LinkedList<TreeUndoManager.UndoItem> undoItems = new LinkedList<>();
        for (TreePath path : paths) {
            Chapter chapter = (Chapter) path.getLastPathComponent();
            Chapter parent = chapter.getParent();
            int index = parent.indexOf(chapter);
            removeChapterFrom(chapter, parent, index);
            undoItems.addLast(new TreeUndoManager.UndoItem(new Chapter[]{chapter}, parent, new int[]{index}));
        }

        // undo supported
        undoManager.chaptersRemoved(undoItems.toArray(new TreeUndoManager.UndoItem[0]),
                app.getText("Undo.Message.DeleteChapter"), paths);

        if (row == treeView.getRowCount()) {       // focus to the next row
            --row;
        }
        focusToRow(row);

        app.notifyMessage(app.getText("Dialog.DeleteChapter.Result", paths.length));
    }

    public void selectAll() {
        treeView.addSelectionInterval(0, treeView.getRowCount());
    }

    @Override
    public void find() {

    }

    @Override
    public void findNext() {

    }

    @Override
    public void findPrevious() {

    }

    @Override
    public void gotoPosition() {

    }

    public void editAttributes() {
        TreePath[] paths = treeView.getSelectionPaths();
        assert paths != null && paths.length == 1;

        Chapter chapter = (Chapter) paths[0].getLastPathComponent();
        // backup old attributes
        HashMap<String, Object> oldAttributes = new HashMap<>();
        for (String name: chapter.attributeNames()) {
            oldAttributes.put(name, chapter.getAttribute(name));
        }
        if (DialogFactory.editChapterAttributes(viewer, chapter)) {
            // undo supported
            undoManager.attributesUpdated(chapter, oldAttributes, app.getText("Undo.Message.ChapterProperties"));
            attributesUpdated(chapter);
            app.getActiveTask().chapterAttributeModified(chapter, true);
        }
    }

    private class ShowMenuAction extends AbstractAction {
        {
            putValue(ACTION_COMMAND_KEY, "show-context-menu");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            Rectangle rect = treeView.getPathBounds(treeView.getSelectionPath());
            if (rect == null) {
                return;
            }
            contextMenu.show(treeView, (int) rect.getX(), (int) rect.getY());
        }
    }

    private class EditChapterAction extends AbstractAction {
        {
            putValue(ACTION_COMMAND_KEY, "edit-selected-chapter");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            TreePath[] paths = treeView.getSelectionPaths();
            assert paths != null && paths.length > 0;

            for (TreePath path : paths) {
                Chapter chapter = (Chapter) path.getLastPathComponent();
                if (chapter.isSection()) {
                    if (treeView.isExpanded(path)) {
                        treeView.collapsePath(path);
                    } else {
                        treeView.expandPath(path);
                    }
                } else {
                    editText(path);
                }
            }
        }
    }

    private class SelectChildrenAction extends AbstractAction {
        {
            putValue(ACTION_COMMAND_KEY, "select-children-chapters");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A,
                    KeyEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            TreePath[] paths = treeView.getSelectionPaths();
            assert paths != null && paths.length > 0;

            for (TreePath path : paths) {
                Chapter chapter = (Chapter) path.getLastPathComponent();
                Chapter parent = chapter.getParent();
                if (parent == null) {   // root
                    continue;
                }
                for (Chapter sub: parent) {
                    treeView.addSelectionPath(getChapterPath(sub));
                }
            }
        }
    }

    private class ExportChapterWork extends WaitingWork<File, Void> {
        private String title;
        private MakerData md;

        public ExportChapterWork(String title, MakerData md) {
            this.title = title;
            this.md = md;
        }

        @Override
        protected File doInBackground() throws Exception {
            viewer.getTabbedEditor().cacheAllTabs();
            Worker.getInstance().writeBook(md);
            return md.file;
        }

        @Override
        protected void done() {
            hideWaitingDialog();
            try {
                String str = app.getText("Dialog.ExportChapter.Finished", get().getPath());
                DialogFactory.showMessage(viewer, title, str);
            } catch (InterruptedException | ExecutionException e) {
                Worker.getInstance().showSaveError(viewer, title, md, e.getCause());
            }
        }
    }

    private class BookTuple {
        public Book book;
        public ParserData pd;

        public BookTuple(Book book, ParserData pd) {
            this.book = book;
            this.pd = pd;
        }
    }

    private class ImportChapterWork extends WaitingWork<LinkedList<BookTuple>, BookTuple> {
        private TreePath path;
        private Chapter parent;
        private OpenResult od;
        private ParserData errorPd = null;
        private String dialogTitle;

        public ImportChapterWork(String title, TreePath path, OpenResult od) {
            this.dialogTitle = title;
            this.path = path;
            this.od = od;

            parent = (Chapter) path.getLastPathComponent();

            addPropertyChangeListener(evt -> {
                if (evt.getPropertyName().equals("current-file")) {
                    File file = (File) evt.getNewValue();
                    updateTipText(app.getText("Dialog.ImportChapter.TipText", file.getPath()));
                }
            });
        }

        @Override
        protected LinkedList<BookTuple> doInBackground() throws Exception {
            LinkedList<BookTuple> books = new LinkedList<>();
            for (File file : od.getFiles()) {
                ParserData pd = new ParserData(file);
                try {
                    firePropertyChange("current-file", null, file);
                    BookTuple tuple = new BookTuple(Worker.getInstance().readBook(pd), pd);
                    books.add(tuple);
                    publish(tuple);
                } catch (Exception ex) {
                    errorPd = pd;
                    throw ex;
                }
            }
            return books;
        }

        @Override
        protected void process(List<BookTuple> chunks) {
            treeView.clearSelection();
            for (BookTuple tuple : chunks) {
                int index = parent.size();
                parent.insert(index, tuple.book);
                treeModel.chaptersInserted(parent, new int[]{index}, new Chapter[]{tuple.book});
                treeView.addSelectionPath(getChapterPath(tuple.book));
            }
            app.getActiveTask().chapterChildrenModified(parent, true);
        }

        @Override
        protected void done() {
            hideWaitingDialog();
            try {
                LinkedList<BookTuple> books = get();
                // undo supported
                Chapter[] chapters = new Chapter[books.size()];
                int[] indices = new int[chapters.length];
                int i = 0;
                for (BookTuple tuple : books) {
                    chapters[i] = tuple.book;
                    indices[i] = parent.size() - chapters.length + i;
                    ++i;
                }
                TreeUndoManager.UndoItem undoItem = new TreeUndoManager.UndoItem(chapters, parent, indices);
                undoManager.chaptersInserted(new TreeUndoManager.UndoItem[]{undoItem},
                        app.getText("Undo.Message.ImportChapter"), new TreePath[]{path});

                updateActions();
                app.notifyMessage(
                        app.getText("Dialog.ImportChapter.Finished", books.size(), parent.getTitle()));
            } catch (InterruptedException | ExecutionException e) {
                Worker.getInstance().showOpenError(viewer, dialogTitle, errorPd, e.getCause());
            }
        }
    }

    private Viewer viewer;
    private JTree treeView;
    private OptionsPane optionsPane;
    private JPopupMenu contextMenu;
    private BookTreeModel treeModel;
    private TreeUndoManager undoManager;
}
