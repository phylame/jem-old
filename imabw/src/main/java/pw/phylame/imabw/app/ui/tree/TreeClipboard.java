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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.ArrayList;
import javax.swing.tree.TreePath;

import pw.phylame.imabw.app.ui.tree.undo.*;
import pw.phylame.jem.core.Chapter;
import pw.phylame.imabw.app.Imabw;
import pw.phylame.imabw.app.ui.dialog.DialogFactory;

import static pw.phylame.imabw.app.ui.dialog.DialogFactory.localizedConfirm;

/**
 * Clipboard operations for book tree.
 */
class TreeClipboard {
    private static final Imabw app = Imabw.sharedInstance();

    private ContentsTree contentsTree;

    private ArrayList<TreePath> selections = new ArrayList<>();
    private ArrayList<Chapter> chapters = new ArrayList<>();
    private HashSet<Chapter> checks = new HashSet<>();
    private boolean copying = true;

    TreeClipboard(ContentsTree contentsTree) {
        this.contentsTree = contentsTree;
    }

    void reset() {
        selections.clear();
        chapters.clear();
        checks.clear();
    }

    boolean contains(Chapter chapter) {
        return checks.contains(chapter);
    }

    void itemDeleted(Chapter chapter) {
        if (copying) {
            return;
        }
        chapters.remove(chapter);
        checks.remove(chapter);
        selections.remove(contentsTree.pathOfChapter(chapter));
    }

    void cut() {
        TreePath[] paths = contentsTree.getSortedSelections();
        contentsTree.ensureHasSelections(paths);

        copying = false;
        refreshClipboard(paths);
    }

    void copy() {
        TreePath[] paths = contentsTree.getSortedSelections();
        contentsTree.ensureHasSelections(paths);

        Chapter section = contentsTree.findSectionInSelections(paths);
        if (section != null) {
            DialogFactory.localizedError(contentsTree,
                    app.getText("contents.copyChapter.title"),
                    "contents.copyChapter.prohibitSection", section);
            return;
        }

        copying = true;
        refreshClipboard(paths);
    }

    private void refreshClipboard(TreePath[] paths) {
        for (Chapter chapter : checks) {
            contentsTree.getModel().chapterUpdated(chapter);
        }
        checks.clear();
        selections.clear();
        Collections.addAll(selections, paths);
        chapters.clear();
        Chapter chapter;
        for (TreePath path : paths) {
            chapter = contentsTree.chapterForPath(path);
            contentsTree.getModel().chapterUpdated(chapter);
            chapters.add(chapter);
            checks.add(chapter);
        }
    }

    boolean canPaste() {
        return !selections.isEmpty();
    }

    boolean isCopying() {
        return copying;
    }

    void paste() {
        if (!canPaste()) {
            throw new RuntimeException("clipboard is empty, program has bug found");
        }
        paste0(contentsTree.getSingleSelection());
    }

    private void paste0(TreePath target) {
        String what = copying ? "copyChapter" : "cutChapter";
        ContentsUndoQueue undoQueue =
                new ContentsUndoQueue(contentsTree, contentsTree.getTree().getSelectionPaths());
        Chapter parent = contentsTree.chapterForPath(target);
        // ask if node is leaf
        if (!parent.isSection() && !BookTreeModel.isRoot(parent)) {     // not section and book
            if (!localizedConfirm(contentsTree.getViewer(),
                    app.getText("contents." + what + ".title"),
                    "contents." + what + ".noSection", parent.getTitle())) {
                return;
            }
        }

        Chapter[] dumps = chapters.toArray(new Chapter[chapters.size()]);

        if (copying) {
            Arrays.setAll(dumps, ix -> dumps[ix].copy());   // make copy
        } else {
            TreePath[] paths = contentsTree.pathOfChapters(dumps);
            contentsTree.deleteSelectedPaths(paths, null, undoQueue, null); // delete origin
        }

        int[] indices = new int[dumps.length];
        Arrays.fill(indices, parent.size());

        ContentsUndoItem targetItem = new ContentsUndoItem(parent, dumps, indices);
        contentsTree.insertChaptersInto(dumps, parent, indices, true);
        contentsTree.getUndoManager().insertUndoTask(
                new UndoCutChapters(targetItem, undoQueue, app.getText("undo.message." + what)));

        if (!copying) {
            reset();
        }
        contentsTree.getTree().setSelectionPaths(contentsTree.pathOfChapters(dumps));

        app.localizedMessage("contents." + what + ".finished", dumps.length, parent);
    }

    private class UndoCutChapters extends UndoContentsRemoving {
        private ContentsUndoItem targetItem;

        private UndoCutChapters(ContentsUndoItem targetItem,
                                ContentsUndoQueue undoQueue, String message) {
            super(undoQueue, message);
            this.targetItem = targetItem;
        }

        @Override
        protected void undo() {
            TreePath[] selections = undoQueue.backupCurrentSelections();
            undoQueue.removeItem(targetItem, false);
            deRemoving();
            undoQueue.resetPreviousSelections();
            undoQueue.selections = selections;
            setRelatedRedoTask(new RedoCutChapter(targetItem, undoQueue, getMessage()));
        }
    }

    private class RedoCutChapter extends RedoContentsRemoving {
        private ContentsUndoItem targetItem;

        private RedoCutChapter(ContentsUndoItem targetItem,
                               ContentsUndoQueue undoQueue, String message) {
            super(undoQueue, message);
            this.targetItem = targetItem;
        }

        @Override
        protected void redo() {
            TreePath[] selections = undoQueue.backupCurrentSelections();
            doRemoving();
            undoQueue.insertItem(targetItem, true);
            undoQueue.resetPreviousSelections();
            undoQueue.selections = selections;
            contentsTree.getUndoManager().insertUndoTask(
                    new UndoCutChapters(targetItem, undoQueue, getMessage()));
        }
    }
}
