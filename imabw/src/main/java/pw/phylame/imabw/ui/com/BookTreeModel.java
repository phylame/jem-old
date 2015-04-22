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

package pw.phylame.imabw.ui.com;

import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Part;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
* Created by Nanu on 2015-1-25.
*/
public class BookTreeModel extends DefaultTreeModel {

    public static class PartNode extends DefaultMutableTreeNode {
        public PartNode(Part part) {
            this(part, true);
        }

        public PartNode(Part part, boolean allowsChildren) {
            super(part, allowsChildren);
        }

        public void addAndPart(PartNode node) {
            super.add(node);
            getPart().append(node.getPart());
        }

        public void insertAndPart(PartNode node, int index) {
            super.insert(node, index);
            getPart().insert(index, node.getPart());
        }

        public void removeAndPart(PartNode node) {
            super.remove(node);
            getPart().remove(node.getPart());
        }

        public Part getPart() {
            return (Part) getUserObject();
        }

        public void setPart(Part part) {
            setUserObject(part);
        }

        @Override
        public String toString() {
            return getPart().getTitle();
        }
    }

    public static PartNode makePartNodeTree(Part part) {
        PartNode node = new PartNode(part);
        // make a copy
        java.util.ArrayList<Part> list = new java.util.ArrayList<>();
        // TODO init list from part
        for (Part p: list) {
            node.add(makePartNodeTree(p));
        }
        return node;
    }

    public static PartNode getPartNode(TreePath treePath) {
        if (treePath == null) {
            return null;
        }
        Object data = treePath.getLastPathComponent();
        if (data instanceof PartNode) {
            return (PartNode) data;
        } else {
            return null;
        }
    }

    public static PartNode[] getPartNodes(TreePath[] treePaths) {
        PartNode[] partNodes = new PartNode[treePaths.length];
        int i = 0;
        for (TreePath path: treePaths) {
            partNodes[i++] = getPartNode(path);
        }
        return partNodes;
    }

    public BookTreeModel(PartNode root, boolean asksAllowsChildren) {
        super(root, asksAllowsChildren);
    }

    public BookTreeModel(PartNode root) {
        super(root);
    }

    public BookTreeModel() {
        this(null);
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        if (book == null) {
            setRoot(null);
        } else if (this.book != book) {
            setRoot(makePartNodeTree(book));
        }
        this.book = book;
    }

    ///////// data /////////
    private Book book;
}
