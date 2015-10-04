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

import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.imabw.app.Imabw;
import pw.phylame.jem.imabw.app.ui.dialog.DialogFactory;

import javax.swing.tree.TreePath;
import java.util.LinkedList;

/**
 * Clipboard operations for book tree.
 */
class TreeClipboard {
    private static Imabw                app = Imabw.getInstance();
    private LinkedList<Chapter>         chapterStack;

    private static TreeClipboard instance = null;

    static TreeClipboard getInstance() {
        if (instance == null) {
            instance = new TreeClipboard();
        }
        return instance;
    }

    private TreeClipboard() {
        chapterStack = new LinkedList<>();
    }

    private NavigateTree getBookTree() {
        return app.getActiveViewer().getNavigateTree();
    }

    void cut() {
        NavigateTree bookTree = getBookTree();
        TreePath[] paths = bookTree.getTree().getSelectionPaths();
        if (paths != null) {
            chapterStack.clear();

            int row = bookTree.getTree().getMinSelectionRow();

            TreeUndoManager undoManager = bookTree.getUndoManager();
            LinkedList<TreeUndoManager.UndoItem> undoItems = new LinkedList<>();
            for (TreePath path : paths) {
                Chapter chapter = (Chapter) path.getLastPathComponent();
                Chapter parent = chapter.getParent();
                int index = parent.indexOf(chapter);
                bookTree.removeChapterFrom(chapter, parent, index);
                undoItems.addLast(
                        new TreeUndoManager.UndoItem(new Chapter[]{chapter}, parent, new int[]{index}));
                chapterStack.push(chapter);
            }

            undoManager.addUndoAction(
                    new UndoCutChapters(undoItems.toArray(new TreeUndoManager.UndoItem[0]), paths));

            if (row == bookTree.getTree().getRowCount()) {       // focus to the next row
                --row;
            }
            bookTree.focusToRow(row);

            app.notifyMessage(app.getText("Dialog.CutChapter.Result", paths.length));
        }
    }

    boolean canPaste() {
        return ! chapterStack.isEmpty();
    }

    @SuppressWarnings("unchecked")
    void paste() {
        NavigateTree bookTree = getBookTree();
        TreePath path = bookTree.getTree().getSelectionPath();
        if (path == null || chapterStack.isEmpty()) {
            return;
        }

        Chapter target = (Chapter) path.getLastPathComponent();
        // ask if node is leaf
        if (!target.isSection() && ! BookTreeModel.isRoot(target)) {     // not section and book
            if (! DialogFactory.showConfirm(bookTree.getOwner(),
                    app.getText("Dialog.InsertChapter.Title"),
                    app.getText("Dialog.NewChapter.NoSection", target))) {
                return;
            }
        }
        bookTree.getTree().clearSelection();
        bookTree.getTree().expandPath(bookTree.getChapterPath(target));

        int[] indices = new int[chapterStack.size()];
        TreePath[] paths = new TreePath[chapterStack.size()];
        int i = 0;
        for (Chapter chapter: chapterStack) {
            int index = target.size();
            target.insert(index, chapter);
            bookTree.getTreeModel().chaptersInserted(target, new int[]{index}, new Chapter[]{chapter});
            paths[i] = bookTree.getChapterPath(chapter);
            indices[i] = index;
            ++i;
        }

        TreeUndoManager undoManager = bookTree.getUndoManager();
        TreeUndoManager.UndoItem undoItem = new TreeUndoManager.UndoItem(
                chapterStack.toArray(new Chapter[0]), target, indices);
        undoManager.addUndoAction(new UndoPasteChapters(new TreeUndoManager.UndoItem[]{undoItem},
                new TreePath[]{path}, (LinkedList<Chapter>)chapterStack.clone()));

        chapterStack.clear();
        bookTree.getTree().addSelectionPaths(paths);

        app.getActiveTask().chapterChildrenModified(target, true);
        app.notifyMessage(app.getText("Dialog.PasteChapter.Result", chapterStack.size()));
    }

    private class UndoCutChapters extends TreeUndoManager.CommonUndoRemoving {
        UndoCutChapters(TreeUndoManager.UndoItem[] items, TreePath[] selections) {
            super(items, app.getText("Undo.Message.CutChapter"), selections);
        }

        @SuppressWarnings("unchecked")
        @Override
        void undo() {
            addRedoAction(new RedoCutChapters(this, items, getBookTree().getTree().getSelectionPaths(),
                    (LinkedList<Chapter>)chapterStack.clone()));
            chapterStack.clear();
            super.undo();
        }
    }

    private class RedoCutChapters extends TreeUndoManager.CommonRedoRemoving {
        private LinkedList<Chapter> chapters;

        RedoCutChapters(TreeUndoManager.UndoAction invoker, TreeUndoManager.UndoItem[] items,
                        TreePath[] selections, LinkedList<Chapter> chapters) {
            super(invoker, items, app.getText("Undo.Message.CutChapter"), selections);
            this.chapters = chapters;
        }

        @Override
        void redo() {
            chapterStack.addAll(chapters);
            super.redo();
        }
    }

    private class UndoPasteChapters extends TreeUndoManager.CommonUndoInserting {
        private LinkedList<Chapter> chapters;

        UndoPasteChapters(TreeUndoManager.UndoItem[] items, TreePath[] selections,
                          LinkedList<Chapter> chapters) {
            super(items, app.getText("Undo.Message.PasteChapter"), selections);
            this.chapters = chapters;
        }

        @Override
        void undo() {
            addRedoAction(new RedoPasteChapters(this, items, getBookTree().getTree().getSelectionPaths()));
            chapterStack.addAll(chapters);
            super.undo();
        }
    }

    private class RedoPasteChapters extends TreeUndoManager.CommonRedoInserting {
        RedoPasteChapters(TreeUndoManager.UndoAction invoker, TreeUndoManager.UndoItem[] items,
                          TreePath[] selections) {
            super(invoker, items, app.getText("Undo.Message.PasteChapter"), selections);
        }

        @Override
        void redo() {
            System.out.println("redo paste");
            chapterStack.clear();
            super.redo();
        }
    }
}
