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

package pw.phylame.imabw.app.ui.tree;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.Rectangle;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.function.Consumer;

import pw.phylame.gaf.ixin.IQuietAction;
import pw.phylame.imabw.app.model.*;
import pw.phylame.imabw.app.ui.Editable;
import pw.phylame.imabw.app.ui.tree.undo.*;
import pw.phylame.imabw.app.util.BookUtils;
import pw.phylame.jem.core.Jem;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.util.TextFactory;
import pw.phylame.imabw.app.Imabw;
import pw.phylame.imabw.app.Worker;
import pw.phylame.imabw.app.ui.Viewer;
import pw.phylame.imabw.app.ui.editor.EditorTab;
import pw.phylame.imabw.app.ui.editor.TabbedEditor;

import static pw.phylame.imabw.app.ui.dialog.DialogFactory.*;

/**
 * Book contents navigation tree.
 */
public class ContentsTree extends JPanel implements Editable {
    private static final Imabw app = Imabw.sharedInstance();

    private static final String CHAPTER_SEPARATOR = "->";

    private static final Object[] NEW_MENU_MODEL = {
            "contents.menu.new",
            Imabw.NEW_CHAPTER, Imabw.IMPORT_CHAPTER
    };

    private static final Object[] CONTEXT_MENU_MODEL = {
            NEW_MENU_MODEL,
            Imabw.INSERT_CHAPTER,
            null,
            Imabw.EDIT_CUT, Imabw.EDIT_PASTE,
            null,
            Imabw.EXPORT_CHAPTER,
            null,
            Imabw.RENAME_CHAPTER,
            null,
            Imabw.EDIT_DELETE,
            null,
            Imabw.MERGE_CHAPTER,
            null,
            Imabw.CHAPTER_PROPERTIES
    };

    public ContentsTree(Viewer viewer) {
        this.viewer = viewer;
        worker = Worker.sharedInstance();
        clipboard = new TreeClipboard(this);
        undoManager = new TreeUndoManager();
        model = new BookTreeModel();
        createTreeActions();
        createComponents();
        initialize();
    }

    private void createComponents() {
        setLayout(new BorderLayout());

        add(new ControlsPane(this), BorderLayout.PAGE_START);

        tree = new JTree(model);
        add(new JScrollPane(tree), BorderLayout.CENTER);
    }

    private void createTreeActions() {
        viewer.new MenuAction(Imabw.NEW_CHAPTER) {
            @Override
            public void actionPerformed(ActionEvent e) {
                newChapter();
            }
        };
        viewer.new MenuAction(Imabw.INSERT_CHAPTER) {
            @Override
            public void actionPerformed(ActionEvent e) {
                insertChapter();
            }
        };
        viewer.new MenuAction(Imabw.IMPORT_CHAPTER) {
            @Override
            public void actionPerformed(ActionEvent e) {
                importChapter();
            }
        };
        viewer.new MenuAction(Imabw.EXPORT_CHAPTER) {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportChapter();
            }
        };
        viewer.new MenuAction(Imabw.RENAME_CHAPTER) {
            @Override
            public void actionPerformed(ActionEvent e) {
                renameChapter();
            }
        };
        viewer.new MenuAction(Imabw.MERGE_CHAPTER) {
            @Override
            public void actionPerformed(ActionEvent e) {
                joinChapter();
            }
        };
        viewer.new MenuAction(Imabw.LOCK_CONTENTS) {
            {
                putValue(SELECTED_KEY, false);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                readonly = !readonly;
                updateCorrelatedActions();
            }
        };
        viewer.new MenuAction(Imabw.CHAPTER_PROPERTIES) {
            @Override
            public void actionPerformed(ActionEvent e) {
                chapterAttributes(getSingleChapter());
            }
        };
        viewer.new MenuAction(Imabw.BOOK_EXTENSIONS) {
            @Override
            public void actionPerformed(ActionEvent e) {
                bookExtensions();
            }
        };
    }

    private void initialize() {
        tree.setEditable(false);
        tree.setDragEnabled(true);
        tree.setDropMode(DropMode.USE_SELECTION);
        tree.setCellRenderer(render = new BookCellRender());
        render.clipboard = clipboard;

        contextMenu = viewer.createPopupMenu("Hello", CONTEXT_MENU_MODEL);
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        editContent(path);
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getClickCount() != 1) {
                    return;
                }
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (!tree.isFocusOwner()) {
                        tree.requestFocus();
                    }
                }
                if (!e.isControlDown()) {
                    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                    // select the path if not in selection
                    if (path != null && !tree.isPathSelected(path)) {
                        tree.setSelectionPath(path);
                    }
                }
                if (SwingUtilities.isRightMouseButton(e)) {
                    contextMenu.show(tree, e.getX(), e.getY());
                }
            }
        });

        tree.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                viewer.setActiveComponent(ContentsTree.this);
                updateCorrelatedActions();
            }
        });


        tree.addTreeSelectionListener(e -> updateCorrelatedActions());

        mapKeyboardAction(new ShowMenuAction());
        mapKeyboardAction(new EditContentAction());
        mapKeyboardAction(new SelectChildrenAction());
        mapKeyboardAction(new GotoRootAction());
    }

    public void updateCorrelatedActions() {
        // disable all common edit actions

        updateEditActions(false);

        viewer.setActionEnable(Imabw.FIND_CONTENT, false);
        viewer.setActionEnable(Imabw.GOTO_POSITION, false);

        // disable all tree actions
        updateTreeActions(false);
        viewer.setActionEnable(Imabw.LOCK_CONTENTS, true);

        TreePath[] paths = tree.getSelectionPaths();
        // no selection
        if (paths == null || paths.length == 0) {
            return;
        }

        viewer.setActionEnable(Imabw.EDIT_SELECT_ALL, true);
        viewer.setActionEnable(Imabw.EXPORT_CHAPTER, true);
        viewer.setActionEnable(Imabw.BOOK_EXTENSIONS, true);

        if (paths.length == 1) {                        // single selection
            viewer.setActionEnable(Imabw.FIND_CONTENT, true);
            viewer.setActionEnable(Imabw.GOTO_POSITION, true);
            if (isReadonly()) {            // read-only
                viewer.setActionEnable(Imabw.CHAPTER_PROPERTIES, true);
            } else {
                updateEditActions(true);
                updateUndoRedoActions();
                viewer.setActionEnable(Imabw.EDIT_PASTE, canPaste());
                updateTreeActions(true);
                viewer.setActionEnable(Imabw.MERGE_CHAPTER, false);
            }
        } else {                                        // multi selection
            if (!isReadonly()) {          // read-write
                updateEditActions(true);
                updateUndoRedoActions();
                viewer.setActionEnable(Imabw.EDIT_PASTE, canPaste());
                viewer.setActionEnable(Imabw.MERGE_CHAPTER, true);
            }
        }

        // root is in selection
        if (isBookInSelections()) {
            // root cannot insert, save, move, delete, merge
            viewer.setActionEnable(Imabw.EDIT_CUT, false);
            viewer.setActionEnable(Imabw.EDIT_COPY, false);
            viewer.setActionEnable(Imabw.EDIT_DELETE, false);
            viewer.setActionEnable(Imabw.INSERT_CHAPTER, false);
            viewer.setActionEnable(Imabw.EXPORT_CHAPTER, false);
            viewer.setActionEnable(Imabw.MERGE_CHAPTER, false);
        }
    }

    /**
     * Updates state of all default tree actions
     *
     * @param enable <tt>true</tt> to enable those actions, otherwise <tt>false</tt>
     */
    private void updateTreeActions(boolean enable) {
        for (String actionId : Imabw.TREE_COMMANDS) {
            viewer.setActionEnable(actionId, enable);
        }
    }

    private String getUndoMessage() {
        String msg = undoManager.getPresentUndoMessage();
        return app.getText("undo.message.undoBase", msg != null ? msg : "");
    }

    private String getRedoMessage() {
        String msg = undoManager.getPresentRedoMessage();
        return app.getText("undo.message.redoBase", msg != null ? msg : "");
    }

    private void updateUndoRedoActions() {
        Action action = viewer.getMenuAction(Imabw.EDIT_UNDO);
        action.setEnabled(undoManager.canUndo());
        action.putValue(Action.NAME, getUndoMessage());

        action = viewer.getMenuAction(Imabw.EDIT_REDO);
        action.setEnabled(undoManager.canRedo());
        action.putValue(Action.NAME, getRedoMessage());
    }

    public void mapKeyboardAction(Action action) {
        mapKeyboardAction((String) action.getValue(Action.ACTION_COMMAND_KEY),
                (KeyStroke) action.getValue(Action.ACCELERATOR_KEY), action);
    }

    public void mapKeyboardAction(String command, KeyStroke keyStroke, Action action) {
        tree.getInputMap(WHEN_FOCUSED).put(keyStroke, command);
        tree.getActionMap().put(command, action);
    }

    public JTree getTree() {
        return tree;
    }

    public BookTreeModel getModel() {
        return model;
    }

    public TreeUndoManager getUndoManager() {
        return undoManager;
    }

    public TreeClipboard getClipboard() {
        return clipboard;
    }

    public Icon getChapterIcon() {
        return render.getChapterIcon();
    }

    public Icon getSectionIcon() {
        return render.getSectionIcon();
    }

    public Icon getBookIcon() {
        return render.getBookIcon();
    }

    public void setBook(Chapter book) {
        undoManager.reset();
        clipboard.reset();
        setReadonly(false);
        model.setBook(book);
        focusToRow(0);
    }

    public String generateChapterTip(Chapter chapter) {
        return generateChapterTip(BookTreeModel.getPathToRoot(chapter));
    }

    public String generateChapterTip(Chapter[] paths) {
        String[] names = new String[paths.length];
        for (int i = 0; i < paths.length; i++) {
            names[i] = paths[i].getTitle();
        }
        return String.join(CHAPTER_SEPARATOR, names);
    }

    private void ensureLengthMatched(Chapter[] chapters, int[] indices) {
        if (chapters.length != indices.length) {
            throw new IllegalArgumentException("length of chapters and indices is different");
        }
    }

    public void insertChaptersInto(Chapter[] chapters, Chapter target, int[] indices,
                                   boolean modified) {
        ensureLengthMatched(chapters, indices);
        if (chapters.length == 0) {
            return;
        }
        for (int i = chapters.length - 1; i >= 0; i--) {
            target.insert(indices[i], chapters[i]);
            indices[i] += i;
        }
        app.getManager().getActiveTask().chapterChildrenModified(target, modified);
        model.chaptersInserted(target, indices, chapters);
    }

    // with undo manager
    public void insertChaptersInto(Chapter[] chapters, Chapter target, int[] indices,
                                   String undoMessage) {
        ContentsUndoQueue undoQueue = new ContentsUndoQueue(this, tree.getSelectionPaths());
        undoQueue.newItem(target, chapters, indices);
        undoManager.chaptersInserted(undoQueue, undoMessage);
        insertChaptersInto(chapters, target, indices, true);
    }

    public void removeChaptersFrom(Chapter[] chapters, Chapter parent, int[] indices,
                                   boolean modified) {
        ensureLengthMatched(chapters, indices);
        if (chapters.length == 0) {
            return;
        }
        for (int i = chapters.length - 1; i >= 0; i--) {
            Chapter chapter = parent.removeAt(indices[i]);
            viewer.getTabbedEditor().closeTab(chapter);
            clipboard.itemDeleted(chapter);
            indices[i] -= i;
        }
        app.getManager().getActiveTask().chapterChildrenModified(parent, modified);
        model.chaptersRemoved(parent, indices, chapters);
    }

    // notify and with undo manager
    public void removeChaptersFrom(Chapter[] chapters, Chapter parent, int[] indices,
                                   String undoMessage) {
        ContentsUndoQueue undoQueue = new ContentsUndoQueue(this, tree.getSelectionPaths());
        undoQueue.newItem(parent, chapters, indices);
        undoManager.chaptersRemoved(undoQueue, undoMessage);
        removeChaptersFrom(chapters, parent, indices, true);
    }

    public void fireAttributesUpdated(Chapter chapter) {
        model.chapterUpdated(chapter);
        viewer.getTabbedEditor().updateTabTitle(chapter);
        updateUndoRedoActions();
    }

    public void fireTextModified(Chapter chapter) {
        model.chapterUpdated(chapter);
    }

    public Map<String, Object> updateChapterAttributes(Chapter chapter,
                                                       Map<String, Object> newAttributes,
                                                       boolean removePresent,
                                                       boolean modified) {
        Map<String, Object> oldAttributes;
        if (removePresent) {
            // backup all attributes
            oldAttributes = BookUtils.dumpAttributes(chapter);
            chapter.clearAttributes();
        } else {
            // only backup modified attributes
            oldAttributes = new HashMap<>();
            for (String name : newAttributes.keySet()) {
                oldAttributes.put(name, chapter.getAttribute(name, null));
            }
        }
        chapter.updateAttributes(newAttributes);
        app.getManager().getActiveTask().chapterAttributeModified(chapter, modified);
        fireAttributesUpdated(chapter);
        return oldAttributes;
    }

    // with undo manager
    public void updateChapterAttributes(Chapter chapter,
                                        Map<String, Object> attributes,
                                        boolean removePresent, String undoMessage) {
        AttributesUndoQueue undoQueue = new AttributesUndoQueue(this, tree.getSelectionPaths());
        AttributesUndoItem item = undoQueue.newItem(chapter, null, removePresent);
        undoManager.attributesUpdated(undoQueue, undoMessage);
        item.attributes = updateChapterAttributes(chapter, attributes, removePresent, true);
    }

    public void editContent(Chapter chapter) {
        if (!chapter.isSection()) {
            TabbedEditor tabbedEditor = viewer.getTabbedEditor();
            EditorTab tab = tabbedEditor.findTab(chapter);
            if (tab == null) {
                tab = tabbedEditor.newTab(chapter, generateChapterTip(chapter));
            }
            tabbedEditor.activateTab(tab);
        }
    }

    public void editContent(TreePath path) {
        editContent((Chapter) path.getLastPathComponent());
    }

    public void focusToRow(int row) {
        tree.setSelectionRow(row);
        tree.scrollRowToVisible(row);
    }

    public void focusToPath(TreePath path) {
        tree.setSelectionPath(path);
        tree.scrollPathToVisible(path);
    }

    public void focusToChapter(Chapter chapter) {
        focusToPath(pathOfChapter(chapter));
    }

    public TreePath pathOfChapter(Chapter chapter) {
        return new TreePath(BookTreeModel.getPathToRoot(chapter));
    }

    public TreePath[] pathOfChapters(Chapter[] chapters) {
        TreePath[] paths = new TreePath[chapters.length];
        for (int i = 0; i < chapters.length; i++) {
            paths[i] = pathOfChapter(chapters[i]);
        }
        return paths;
    }

    public Chapter chapterForPath(TreePath path) {
        return BookTreeModel.chapterForPath(path);
    }

    public void ensureContentsMutable() {
        if (isReadonly()) {
            throw new RuntimeException("operation cannot be executed when contents is locked");
        }
    }

    public TreePath[] ensureHasSelections(TreePath[] paths) {
        if (paths == null || paths.length == 0) {
            throw new RuntimeException("operation requires at one or more selections");
        }
        return paths;
    }

    public TreePath ensureSingleSelection(TreePath[] paths) {
        if (paths == null || paths.length != 1) {
            throw new RuntimeException("operation requires only one selection");
        }
        return paths[0];
    }

    public TreePath[] ensureMultiSelections(TreePath[] paths) {
        if (paths == null || paths.length < 2) {
            throw new RuntimeException("operation requires more than one selections");
        }
        return paths;
    }

    public TreePath getSingleSelection() {
        return ensureSingleSelection(tree.getSelectionPaths());
    }

    public Chapter getSingleChapter() {
        return chapterForPath(getSingleSelection());
    }

    public TreePath[] getMultiSelections() {
        return ensureMultiSelections(tree.getSelectionPaths());
    }

    public TreePath[] getCurrentSelections() {
        return ensureHasSelections(tree.getSelectionPaths());
    }

    public TreePath[] getSortedSelections() {
        int[] rows = tree.getSelectionRows();
        if (rows == null) {
            return null;
        }
        Arrays.sort(rows);
        TreePath[] paths = new TreePath[rows.length];
        for (int i = 0; i < rows.length; ++i) {
            paths[i] = tree.getPathForRow(rows[i]);
        }
        return paths;
    }

    public boolean isBookInSelections() {
        return tree.isPathSelected(tree.getPathForRow(0));
    }

    // return the first occurred section or null if no section in selections
    public Chapter findSectionInSelections(TreePath[] selections) {
        for (TreePath path : selections) {
            Chapter chapter = chapterForPath(path);
            if (chapter.isSection()) {
                return chapter;
            }
        }
        return null;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
        viewer.setActionSelected(Imabw.LOCK_CONTENTS, readonly);
    }

    public void newChapter() {
        ensureContentsMutable();
        TreePath path = getSingleSelection();
        String title = app.getText("contents.newChapter.title");

        Chapter chapter = chapterForPath(path);
        String name = chapter.getTitle();
        // ask if node is leaf
        if (!chapter.isSection() && !BookTreeModel.isRoot(chapter)) {     // not section and book
            if (!localizedConfirm(viewer, title, "contents.newChapter.noSection", name)) {
                return;
            }
        }

        Chapter newChapter = worker.newChapter(viewer, title);
        if (newChapter == null) {
            return;
        }
        // close the editor if the chapter is in editing
        viewer.getTabbedEditor().closeTab(chapter);

        insertChaptersInto(new Chapter[]{newChapter}, chapter, new int[]{chapter.size()},
                app.getText("undo.message.newChapter"));

        tree.expandPath(path);
        focusToChapter(newChapter);

        app.localizedMessage("contents.newChapter.finished", newChapter, name);
    }

    public void insertChapter() {
        ensureContentsMutable();
        Chapter chapter = getSingleChapter();
        String title = app.getText("contents.insertChapter.title");

        Chapter newChapter = worker.newChapter(viewer, title);
        if (newChapter == null) {
            return;
        }

        Chapter parent = chapter.getParent();
        int row = tree.getMinSelectionRow(), index = parent.indexOf(chapter);
        insertChaptersInto(new Chapter[]{newChapter}, parent, new int[]{index},
                app.getText("undo.message.insertChapter"));

        focusToRow(row);
        app.localizedMessage("contents.insertChapter.finished", newChapter, parent);
    }

    public void importChapter() {
        ensureContentsMutable();
        TreePath path = getSingleSelection();
        String title = app.getText("contents.importChapter.title");

        OpenResult od = worker.selectOpenBook(viewer, title, null, null, true);
        if (od == null) {   // no selection
            return;
        }

        File[] files = od.getFiles();
        ParserData[] pds = new ParserData[files.length];
        for (int i = 0; i < files.length; i++) {
            pds[i] = new ParserData(files[i], false);
        }
        openWaiting(viewer, title, "",
                app.getText("contents.importChapter.waitingText"),
                new ImportChapterWork(title, pds, path), false);
    }

    public void exportChapter() {
        Book book = Jem.toBook(getSingleChapter());
        String title = app.getText("contents.exportChapter.title", book.getTitle());

        MakerData md = worker.makeMakerData(viewer, title, book, null, null, null);
        if (md == null) {
            return;
        }
        openWaiting(viewer, title,
                app.getText("contents.exportChapter.tipText", md.file),
                app.getText("contents.exportChapter.waitingText"),
                new ExportChapterWork(title, md), false);
    }

    public void renameChapter() {
        ensureContentsMutable();
        Chapter chapter = getSingleChapter();
        String title = app.getText("contents.renameChapter.title");

        String oldTitle = chapter.getTitle();
        String newTitle = inputText(viewer, title,
                app.getText("contents.renameChapter.inputTip"), oldTitle, true, false);

        if (newTitle == null) {
            return;
        }
        HashMap<String, Object> newAttr = new HashMap<>();
        newAttr.put(Chapter.TITLE, newTitle);
        updateChapterAttributes(chapter, newAttr, false,
                app.getText("undo.message.renameChapter"));
        app.localizedMessage("contents.renameChapter.finished", oldTitle, newTitle);
    }

    public void joinChapter() {
        ensureContentsMutable();
        TreePath[] paths = getMultiSelections();
        String title = app.getText("contents.joinChapter.title");

        if (isBookInSelections()) {
            throw new RuntimeException("operation requires selections without root");
        }

        // check section in selections
        Chapter section = findSectionInSelections(paths);
        if (section != null) {
            localizedError(viewer, title,
                    "contents.joinChapter.prohibitSection", section);
            return;
        }

        // target chapter
        Chapter target = worker.newChapter(viewer, title);
        if (target == null) {
            return;
        }

        JCheckBox cbWithTitle = new JCheckBox(
                new IQuietAction("contents.joinChapter.checkBoxWithTitle"));
        Object[] messages = {
                app.getText("contents.joinChapter.askContinue", paths.length, target),
                cbWithTitle
        };
        if (!showConfirm(viewer, title, messages)) {
            return;
        }
        boolean withTitle = cbWithTitle.isSelected();
        Chapter chapter = chapterForPath(paths[0]);
        Chapter parent = chapter.getParent();
        int index = parent.indexOf(chapter);
        insertChaptersInto(new Chapter[]{target}, parent, new int[]{index}, true);

        StringBuilder builder = new StringBuilder();
        ContentsUndoQueue undoQueue = new ContentsUndoQueue(this, paths);
        deleteSelectedPaths(paths, null, undoQueue, ch -> {
            if (withTitle) {
                builder.append(ch.getTitle()).append("\n");
            }
            EditorTab tab = viewer.getTabbedEditor().findTab(ch);
            if (tab != null) {  // opened
                builder.append(tab.getEditor().getTextComponent().getText()).append("\n");
            } else {
                builder.append(BookUtils.contentOfChapter(ch, "")).append("\n");
            }
        });
        target.setContent(TextFactory.fromString(builder.toString()));
        // undo supported
        undoManager.chaptersJoined(target, index, undoQueue,
                app.getText("undo.message.joinChapter"));
        focusToChapter(target);
        editContent(target);
        app.localizedMessage("contents.joinChapter.finished", paths.length, target);
    }

    public void chapterAttributes(Chapter chapter) {
        editAttributes(viewer, chapter);
    }

    public void bookExtensions() {
        editExtensions(viewer, (Book) model.getRoot());
    }

    @Override
    public Viewer getViewer() {
        return viewer;
    }

    @Override
    public void undo() {
        ensureContentsMutable();
        if (undoManager.canUndo()) {
            app.message(getUndoMessage());
            undoManager.undo();
            updateUndoRedoActions();
        }
    }

    @Override
    public void redo() {
        ensureContentsMutable();
        if (undoManager.canRedo()) {
            app.message(getRedoMessage());
            undoManager.redo();
            updateUndoRedoActions();
        }
    }

    @Override
    public void cut() {
        ensureContentsMutable();
        clipboard.cut();
        viewer.setActionEnable(Imabw.EDIT_PASTE, canPaste());
    }

    @Override
    public void copy() {
        clipboard.copy();
        viewer.setActionEnable(Imabw.EDIT_PASTE, canPaste());
    }

    public boolean canPaste() {
        return !isReadonly() && clipboard.canPaste();
    }

    @Override
    public void paste() {
        ensureContentsMutable();
        clipboard.paste();
        viewer.setActionEnable(Imabw.EDIT_PASTE, canPaste());
    }

    // return recommend new row
    public int deleteSelectedPaths(TreePath[] paths,
                                   String undoMessage,
                                   ContentsUndoQueue undoQueue,
                                   Consumer<Chapter> action) {
        int row = tree.getRowForPath(paths[0]);
        Chapter chapter, parent;
        for (TreePath path : paths) {
            chapter = chapterForPath(path);
            if (action != null) {
                action.accept(chapter);
            }
            parent = chapter.getParent();
            Chapter[] chapters = {chapter};
            int[] indices = {parent.indexOf(chapter)};
            removeChaptersFrom(chapters, parent, indices, true);
            undoQueue.newItem(parent, chapters, indices);
        }
        if (undoMessage != null) {
            undoManager.chaptersRemoved(undoQueue, undoMessage);
        }
        return (row == tree.getRowCount()) ? row - 1 : row;
    }

    public void delete() {
        ensureContentsMutable();
        TreePath[] paths = getCurrentSelections();
        String title = app.getText("contents.deleteChapter.title");

        String tip;
        if (paths.length == 1) {
            Chapter ch = chapterForPath(paths[0]);
            if (ch.isSection()) {
                tip = app.getText("contents.deleteChapter.deleteSectionTip", ch.getTitle());
            } else {
                tip = app.getText("contents.deleteChapter.deleteChapterTip", ch.getTitle());
            }
        } else {
            tip = app.getText("contents.deleteChapter.deleteSomeTip", paths.length);
        }

        if (!showConfirm(viewer, title, tip)) {
            return;
        }
        focusToRow(deleteSelectedPaths(paths, app.getText("undo.message.deleteChapter"),
                new ContentsUndoQueue(this, paths), null));
        app.localizedMessage("contents.deleteChapter.finished", paths.length);
    }

    public void selectAll() {
        tree.addSelectionInterval(0, tree.getRowCount());
    }

    private String transRegex(String key) {
        return ".*" + key + ".*";
    }

    @Override
    public void find() {
        Chapter chapter = getSingleChapter();
        String key = inputText(this, app.getText("contents.find.title"),
                app.getText("contents.find.tip"), chapter.getTitle(), true, false);
        if (key == null) {
            return;
        }
        chapter = Jem.find(chapter, c -> c.getTitle().matches(transRegex(key)), 0, true);
        if (chapter != null) {
            focusToChapter(chapter);
        }
    }

    @Override
    public void findNext() {
        featureDeveloping(viewer);
    }

    @Override
    public void findPrevious() {
        featureDeveloping(viewer);
    }

    private String positionOf(Chapter chapter) {
        LinkedList<String> indices = new LinkedList<>();
        Chapter parent = chapter.getParent();
        while (parent != null) {
            indices.addFirst(Integer.toString(parent.indexOf(chapter) + 1));
            chapter = parent;
            parent = parent.getParent();
        }
        return String.join(":", indices);
    }

    private int[] parseIndexes(String str) {
        ArrayList<Integer> indices = new ArrayList<>(5);
        String[] parts = str.split(":");
        for (String s : parts) {
            try {
                int n = Integer.parseInt(s);
                if (n == 0) {
                    return null;
                }
                indices.add(n - 1);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        int[] r = new int[indices.size()];
        Arrays.setAll(r, indices::get);
        return r;
    }

    @Override
    public void gotoPosition() {
        Chapter chapter = getSingleChapter();

        String str = inputText(viewer, app.getText("contents.goto.title"),
                app.getText("contents.goto.tip"),
                positionOf(chapter), true, false);
        if (str == null) {
            return;
        }
        int[] indices = parseIndexes(str);
        if (indices == null) {
            return;
        }
        try {
            chapter = Jem.locate(chapter, indices);
        } catch (IndexOutOfBoundsException e) {
            return;
        }
        focusToChapter(chapter);
    }

    private class ShowMenuAction extends AbstractAction {
        {
            putValue(ACTION_COMMAND_KEY, "show-context-menu");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Rectangle rect = tree.getPathBounds(tree.getSelectionPath());
            if (rect == null) {
                return;
            }
            contextMenu.show(tree, (int) rect.getX(), (int) rect.getY());
        }
    }

    private class EditContentAction extends AbstractAction {
        {
            putValue(ACTION_COMMAND_KEY, "edit-selected-chapter");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (TreePath path : getCurrentSelections()) {
                Chapter chapter = chapterForPath(path);
                if (chapter.isSection()) {
                    if (tree.isExpanded(path)) {
                        tree.collapsePath(path);
                    } else {
                        tree.expandPath(path);
                    }
                } else {
                    editContent(chapter);
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
            for (TreePath path : getCurrentSelections()) {
                Chapter chapter = chapterForPath(path);
                Chapter parent = chapter.getParent();
                if (parent == null) {   // root
                    continue;
                }
                for (Chapter sub : parent) {
                    tree.addSelectionPath(pathOfChapter(sub));
                }
            }
        }
    }

    private class GotoRootAction extends AbstractAction {
        {
            putValue(ACTION_COMMAND_KEY, "go-to-root-node");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_HOME,
                    KeyEvent.ALT_DOWN_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            focusToRow(0);
        }
    }

    private class ExportChapterWork extends WriteBook {
        protected ExportChapterWork(String title, MakerData md) {
            super(title, md);
        }

        @Override
        protected void onSuccess(File result) {
            hideWaitingDialog();
            localizedInformation(viewer, title, "contents.exportChapter.finished", md.book, result);
        }
    }

    private class ImportChapterWork extends ReadBook {
        private TreePath path;
        private Chapter target;
        private boolean cleared = false;

        public ImportChapterWork(String title, ParserData[] pds, TreePath path) {
            super(title, pds);
            this.path = path;
            target = chapterForPath(path);
        }

        @Override
        protected void itemStarted(ParserData pd) {
            updateTipText(app.getText("contents.importChapter.tipTip", pd.file));
        }

        @Override
        protected void process(List<ParseResult> chunks) {
            if (!cleared) {
                tree.clearSelection();
                cleared = true;
            }
            for (ParseResult pr : chunks) {
                target.append(pr.book);
                tree.addSelectionPath(pathOfChapter(pr.book));
            }
        }

        @Override
        protected void onSuccess(List<ParseResult> result) {
            // undo supported
            Chapter[] chapters = new Chapter[result.size()];
            int[] indices = new int[chapters.length];
            int i = 0;
            for (ParseResult pr : result) {
                chapters[i] = pr.book;
                indices[i] = target.size() - chapters.length + i;
                ++i;
            }
            ContentsUndoQueue undoQueue =
                    new ContentsUndoQueue(ContentsTree.this, new TreePath[]{path});
            undoQueue.newItem(target, chapters, indices);
            undoManager.chaptersInserted(undoQueue, app.getText("undo.message.importChapter"));
            app.getManager().getActiveTask().chapterChildrenModified(target, true);
            model.chaptersInserted(target, indices, chapters);
            updateUndoRedoActions();
            hideWaitingDialog();
            app.localizedMessage("contents.importChapter.finished", result.size(), target);
        }
    }

    private Viewer viewer;
    private Worker worker;
    private JTree tree;
    private BookTreeModel model;
    private BookCellRender render;
    private JPopupMenu contextMenu;
    private TreeClipboard clipboard;
    private TreeUndoManager undoManager;
    private boolean readonly = false;
}
