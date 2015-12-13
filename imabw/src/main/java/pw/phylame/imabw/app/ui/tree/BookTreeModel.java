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

import java.util.LinkedList;

import pw.phylame.jem.core.Chapter;

import javax.swing.tree.TreePath;
import javax.swing.tree.TreeModel;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.EventListenerList;

public class BookTreeModel implements TreeModel {
    /**
     * The book instance (aka: the root node of tree)
     */
    private Chapter book;

    protected EventListenerList listenerList = new EventListenerList();

    public static boolean isRoot(Chapter chapter) {
        return chapter.getParent() == null;
    }

    public static Chapter[] getPathToRoot(Chapter chapter) {
        LinkedList<Chapter> paths = new LinkedList<>();
        Chapter node = chapter;
        while (node != null) {
            paths.addFirst(node);
            node = node.getParent();
        }
        return paths.toArray(new Chapter[paths.size()]);
    }

    public static Chapter chapterForPath(TreePath path) {
        return (Chapter) path.getLastPathComponent();
    }

    public BookTreeModel() {
    }

    public BookTreeModel(Chapter book) {
        setBook(book);
    }

    public void setBook(Chapter book) {
        if (book == null) {
            throw new NullPointerException();
        }
        this.book = book;
        childrenChanged(book);
    }

    @Override
    public Object getRoot() {
        return book;
    }

    @Override
    public Object getChild(Object parent, int index) {
        return ((Chapter) parent).chapterAt(index);
    }

    @Override
    public int getChildCount(Object parent) {
        return ((Chapter) parent).size();
    }

    @Override
    public boolean isLeaf(Object node) {
        return !((Chapter) node).isSection();
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        throw new UnsupportedOperationException("operation is unsupported");
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return ((Chapter) parent).indexOf((Chapter) child);
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        listenerList.add(TreeModelListener.class, l);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        listenerList.remove(TreeModelListener.class, l);
    }

    /**
     * Invoke this method after you've inserted some chapters into
     * <tt>parent</tt>.  <tt>indices</tt> should be the index of the new chapters and
     * must be sorted in ascending order.
     */
    public void chaptersInserted(Chapter parent, int[] indices, Chapter[] children) {
        if (listenerList != null && parent != null && indices != null && indices.length > 0) {
            fireChaptersInserted(this, getPathToRoot(parent), indices, children);
        }
    }

    /**
     * Invoke this method after you've removed some chapters from
     * <tt>parent</tt>.  <tt>indices</tt> should be the index of the removed chapters and
     * must be sorted in ascending order. And <tt>children</tt> should be
     * the array of the children chapters that were removed.
     */
    public void chaptersRemoved(Chapter parent, int[] indices, Chapter[] children) {
        if (parent != null && indices != null) {
            fireChaptersRemoved(this, getPathToRoot(parent), indices, children);
        }
    }

    /**
     * Invoke this method after you've changed how the children identified by
     * <tt>indices</tt> are to be represented in the tree.
     */
    public void chapterUpdated(Chapter parent, int[] indices, Chapter[] children) {
        if (parent != null) {
            if (indices != null && indices.length > 0) {
                fireChapterUpdated(this, getPathToRoot(parent), indices, children);
            } else if (parent == getRoot()) {
                fireChapterUpdated(this, getPathToRoot(parent), null, null);
            }
        }
    }

    public void chapterUpdated(Chapter chapter) {
        Chapter parent = chapter.getParent();
        if (parent != null) {
            int index = parent.indexOf(chapter);
            if (index != -1) {
                chapterUpdated(parent, new int[]{index}, new Chapter[]{chapter});
            }
        } else if (chapter == getRoot()) {
            chapterUpdated(chapter, null, null);
        }
    }

    /**
     * Invoke this method if you've totally changed the children of
     * chapter and its children's children...  This will post a
     * treeStructureChanged event.
     */
    public void childrenChanged(Chapter parent) {
        if (parent != null) {
            fireChildrenChanged(this, getPathToRoot(parent), null, null);
        }
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the parameters passed into
     * the fire method.
     *
     * @param source   the source of the {@code TreeModelEvent};
     *                 typically {@code this}
     * @param path     the path to the parent chapter that changed; use
     *                 {@code null} to identify the root has changed
     * @param indices  the indices of the changed chapters
     * @param children the updated chapters
     */
    private void fireChapterUpdated(Object source, Chapter[] path,
                                    int[] indices, Chapter[] children) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeModelListener.class) {
                // Lazily create the event:
                if (e == null)
                    e = new TreeModelEvent(source, path, indices, children);
                ((TreeModelListener) listeners[i + 1]).treeNodesChanged(e);
            }
        }
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the parameters passed into
     * the fire method.
     *
     * @param source   the source of the {@code TreeModelEvent};
     *                 typically {@code this}
     * @param path     the path to the parent chapter were added to
     * @param indices  the indices of the new chapters
     * @param children the new chapters
     */
    private void fireChaptersInserted(Object source, Chapter[] path,
                                      int[] indices, Chapter[] children) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeModelListener.class) {
                // Lazily create the event:
                if (e == null)
                    e = new TreeModelEvent(source, path, indices, children);
                ((TreeModelListener) listeners[i + 1]).treeNodesInserted(e);
            }
        }
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the parameters passed into
     * the fire method.
     *
     * @param source   the source of the {@code TreeModelEvent};
     *                 typically {@code this}
     * @param path     the path to the parent chapter were removed from
     * @param indices  the indices of the removed chapters
     * @param children the removed chapters
     */
    private void fireChaptersRemoved(Object source, Chapter[] path,
                                     int[] indices, Chapter[] children) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeModelListener.class) {
                // Lazily create the event:
                if (e == null)
                    e = new TreeModelEvent(source, path, indices, children);
                ((TreeModelListener) listeners[i + 1]).treeNodesRemoved(e);
            }
        }
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the parameters passed into
     * the fire method.
     *
     * @param source   the source of the {@code TreeModelEvent};
     *                 typically {@code this}
     * @param path     the path to the parent of the structure that has changed;
     *                 use {@code null} to identify the root has changed
     * @param indices  the indices of the affected chapter
     * @param children the affected chapter
     */
    private void fireChildrenChanged(Object source, Chapter[] path,
                                     int[] indices, Chapter[] children) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TreeModelListener.class) {
                // Lazily create the event:
                if (e == null)
                    e = new TreeModelEvent(source, path, indices, children);
                ((TreeModelListener) listeners[i + 1]).treeStructureChanged(e);
            }
        }
    }
}
