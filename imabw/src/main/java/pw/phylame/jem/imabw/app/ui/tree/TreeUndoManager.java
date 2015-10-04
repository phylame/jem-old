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

import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import javax.swing.tree.TreePath;
import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.imabw.app.Imabw;

class TreeUndoManager {
    private static Imabw app = Imabw.getInstance();
    private NavigateTree bookTree;

    private LinkedList<UndoAction> undoStack = new LinkedList<>();
    private LinkedList<RedoAction> redoStack = new LinkedList<>();

    public TreeUndoManager(NavigateTree navigateTree) {
        bookTree = navigateTree;
    }

    boolean canUndo() {
        return !undoStack.isEmpty();
    }

    void undo() {
        assert canUndo();
        String tip = getUndoTitle();
        undoStack.pop().undo();
        app.notifyMessage(tip);
    }

    boolean canRedo() {
        return !redoStack.isEmpty();
    }

    void redo() {
        assert canRedo();
        String tip = getRedoTitle();
        redoStack.pop().redo();
        app.notifyMessage(tip);
    }

    String getUndoTitle() {
        String base = app.getText("Undo.Message.UndoBase");
        return base + (canUndo() ? " " + undoStack.getFirst().getMessage() : "");
    }

    String getRedoTitle() {
        String base = app.getText("Undo.Message.RedoBase");
        return base + (canRedo() ? " " + redoStack.getFirst().getMessage() : "");
    }

    void addUndoAction(UndoAction action) {
        undoStack.push(action);
        action.setUndoManager(this);
        if (!redoStack.isEmpty()) {
            redoStack.pop();
        }
    }

    void chaptersRemoved(UndoItem[] items, String message, TreePath[] selections) {
        app.debug("add removed undo");
        addUndoAction(new UndoRemoveChapters(items, message, selections));
    }

    void chaptersInserted(UndoItem[] items, String message, TreePath[] selections) {
        addUndoAction(new UndoInsertChapters(items, message, selections));
    }

    void attributesUpdated(Chapter chapter, Map<String, Object> oldAttributes, String message) {
        addUndoAction(new UndoUpdateAttributes(chapter, oldAttributes, message));
    }

    static class UndoItem {
        Chapter[] chapters;
        Chapter parent;
        int[] indices;

        UndoItem(Chapter[] chapters, Chapter parent, int[] indices) {
            this.chapters = chapters;
            this.parent = parent;
            this.indices = indices;
        }
    }

    static class UndoRemoveChapters extends CommonUndoRemoving {
        UndoRemoveChapters(UndoItem[] items, String message, TreePath[] selections) {
            super(items, message, selections);
        }

        @Override
        void undo() {
            addRedoAction(new CommonRedoRemoving(this, items, getMessage(),
                    undoManager.bookTree.getTree().getSelectionPaths()));
            super.undo();
        }
    }

    static class UndoInsertChapters extends CommonUndoInserting {
        UndoInsertChapters(UndoItem[] items, String message, TreePath[] selections) {
            super(items, message, selections);
        }

        @Override
        void undo() {
            addRedoAction(new CommonRedoInserting(this, items, getMessage(),
                    undoManager.bookTree.getTree().getSelectionPaths()));
            super.undo();
        }
    }

    static class UndoUpdateAttributes extends UndoAction {
        private Chapter chapter;
        private Map<String, Object> oldAttributes;

        UndoUpdateAttributes(Chapter chapter, Map<String, Object> oldAttributes, String message) {
            super(message, null);
            this.chapter = chapter;
            this.oldAttributes = oldAttributes;
        }

        @Override
        void undo() {
            HashMap<String, Object> newAttributes = new HashMap<>();
            for (String name : oldAttributes.keySet()) {
                newAttributes.put(name, chapter.getAttribute(name));
                chapter.setAttribute(name, oldAttributes.get(name));
            }
            addRedoAction(new RedoUpdateAttributes(this, chapter, newAttributes, getMessage()));
            undoManager.bookTree.focusToChapter(chapter);

            undoManager.bookTree.attributesUpdated(chapter);
            app.getActiveTask().chapterAttributeModified(chapter, false);
        }
    }

    static class RedoUpdateAttributes extends RedoAction {
        private Chapter chapter;
        private Map<String, Object> newAttributes;

        RedoUpdateAttributes(UndoAction invoker,
                             Chapter chapter, Map<String, Object> newAttributes, String message) {
            super(invoker, message, null);
            this.chapter = chapter;
            this.newAttributes = newAttributes;
        }

        @Override
        void redo() {
            undoManager.undoStack.push(invoker);
            chapter.updateAttributes(newAttributes);
            undoManager.bookTree.focusToChapter(chapter);

            undoManager.bookTree.attributesUpdated(chapter);
            app.getActiveTask().chapterAttributeModified(chapter, true);
        }
    }

    private static abstract class Action {
        private String message;
        protected TreePath[] selections;
        protected TreeUndoManager undoManager;

        Action(String message, TreePath[] selections) {
            this.message = message;
            this.selections = selections;
        }

        String getMessage() {
            return message;
        }

        final void setUndoManager(TreeUndoManager undoManager) {
            this.undoManager = undoManager;
        }
    }

    static abstract class UndoAction extends Action {
        UndoAction(String message, TreePath[] selections) {
            super(message, selections);
        }

        abstract void undo();

        final void addRedoAction(RedoAction action) {
            undoManager.redoStack.push(action);
            action.setUndoManager(undoManager);
        }
    }

    static abstract class RedoAction extends Action {
        protected UndoAction invoker;

        RedoAction(UndoAction invoker, String message, TreePath[] selections) {
            super(message, selections);
            this.invoker = invoker;
        }

        abstract void redo();
    }

    static class CommonUndoRemoving extends UndoAction {
        protected UndoItem[] items;

        CommonUndoRemoving(UndoItem[] items, String message, TreePath[] selections) {
            super(message, selections);
            this.items = items;
        }

        @Override
        void undo() {
            for (int i = items.length-1; i >= 0; --i) {
                UndoItem item = items[i];
                for (int j = 0; j < item.chapters.length; ++j) {
                    item.parent.insert(item.indices[j], item.chapters[j]);
                }
                app.getActiveTask().chapterChildrenModified(item.parent, false);
                undoManager.bookTree.getTreeModel().chaptersInserted(item.parent, item.indices, item.chapters);
            }
            undoManager.bookTree.getTree().setSelectionPaths(selections);
        }
    }

    static class CommonRedoRemoving extends RedoAction {
        protected UndoItem[] items;

        CommonRedoRemoving(UndoAction invoker, UndoItem[] items, String message, TreePath[] selections) {
            super(invoker, message, selections);
            this.items = items;
        }

        @Override
        void redo() {
            undoManager.undoStack.push(invoker);
            for (UndoItem item : items) {
                undoManager.bookTree.removeChaptersFrom(item.chapters, item.parent, item.indices);
            }
            undoManager.bookTree.getTree().setSelectionPaths(selections);
        }
    }

    static class CommonUndoInserting extends UndoAction {
        protected UndoItem[] items;

        CommonUndoInserting(UndoItem[] items, String message, TreePath[] selections) {
            super(message, selections);
            this.items = items;
        }

        @Override
        void undo() {
            for (int i = items.length-1; i >= 0; --i) {
                UndoItem item = items[i];
                for (int j = item.chapters.length-1; j >= 0; --j) {
                    Chapter chapter = item.parent.remove(item.indices[j]);
                    undoManager.bookTree.getOwner().getTabbedEditor().closeTab(chapter);
                }
                app.getActiveTask().chapterChildrenModified(item.parent, false);
                undoManager.bookTree.getTreeModel().chaptersRemoved(item.parent, item.indices, item.chapters);
            }
            undoManager.bookTree.getTree().setSelectionPaths(selections);
        }
    }

    static class CommonRedoInserting extends RedoAction {
        protected UndoItem[] items;

        CommonRedoInserting(UndoAction invoker, UndoItem[] items, String message, TreePath[] selections) {
            super(invoker, message, selections);
            this.items = items;
        }

        @Override
        void redo() {
            undoManager.undoStack.push(invoker);
            for (UndoItem item : items) {
                undoManager.bookTree.insertChaptersInto(item.chapters, item.parent, item.indices);
            }
            undoManager.bookTree.getTree().setSelectionPaths(selections);
        }
    }
}
