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

import pw.phylame.pat.ixin.IToolkit;
import pw.phylame.imabw.Imabw;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * Cell render for Part tree.
 */
public class PartTreeCellRender extends DefaultTreeCellRenderer {
    private static Imabw app = Imabw.getInstance();

    Icon bookIcon    = IToolkit.createImageIcon(app.getText("Frame.Tree.Book.Icon"));
    Icon sectionIcon = IToolkit.createImageIcon(app.getText("Frame.Tree.Section.Icon"));
    Icon chapterIcon = IToolkit.createImageIcon(app.getText("Frame.Tree.Chapter.Icon"));

    @Override
    public Component getTreeCellRendererComponent(JTree tree,
                                                  Object value,
                                                  boolean selected,
                                                  boolean expanded,
                                                  boolean leaf,
                                                  int row,
                                                  boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        PartNode node = (PartNode) value;
        if (leaf) {
            setIcon(chapterIcon);
        } else if (node.isRoot()) {
            setIcon(bookIcon);
        } else {
            setIcon(sectionIcon);
        }
        return this;
    }
}
