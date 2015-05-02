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

package pw.phylame.imabw.ui.com;

import pw.phylame.jem.core.Part;

import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultMutableTreeNode;

public class PartNode extends DefaultMutableTreeNode {
    public static PartNode makePartTree(Part part) {
        PartNode node = new PartNode(part);

        for (Part sub: part) {
            node.add(makePartTree(sub));
        }

        return node;
    }

    public static PartNode getPartNode(TreePath treePath) {
        if (treePath == null) {
            return null;
        }

        Object value = treePath.getLastPathComponent();
        if (value instanceof PartNode) {
            return (PartNode) value;
        }

        return null;
    }

    public PartNode(Part part) {
        setUserObject(part);
    }

    public Part getPart() {
        return (Part) getUserObject();
    }

    public void insertNode(PartNode newChild, int childIndex) {
        super.insert(newChild, childIndex);
        getPart().insert(childIndex, newChild.getPart());
    }

    public void appendNode(PartNode newChild) {
        super.add(newChild);
        getPart().append(newChild.getPart());
    }

    public void removeNode(PartNode aChild) {
        super.remove(aChild);
        getPart().remove(aChild.getPart());
    }

    @Override
    public String toString() {
        return getPart().getTitle();
    }
}
