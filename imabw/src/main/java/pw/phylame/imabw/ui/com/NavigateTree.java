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

import java.awt.BorderLayout;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

/**
 * Common navigate tree component.
 */
public class NavigateTree extends JPanel {
    private JTree tree;
    private JPanel titleBar;
    private JLabel titleLabel;

    public NavigateTree(String title, Icon icon, TreeNode root) {
        super(new BorderLayout());
        tree = new JTree(root);
        createComponents(title, icon);
    }

    public NavigateTree(String title, Icon icon, TreeNode root, boolean asksAllowsChildren) {
        super(new BorderLayout());
        tree = new JTree(root, asksAllowsChildren);
        createComponents(title, icon);
    }

    public NavigateTree(String title, Icon icon, TreeModel model) {
        super(new BorderLayout());
        tree = new JTree(model);
        createComponents(title, icon);
    }

    private void createComponents(String title, Icon icon) {
        titleBar = new JPanel(new BorderLayout());
        titleLabel = new JLabel(title, icon, JLabel.LEADING);
        titleBar.add(titleLabel, BorderLayout.WEST);

        add(titleBar, BorderLayout.NORTH);
        add(new JScrollPane(tree), BorderLayout.CENTER);
    }

    public JTree getTree() {
        return tree;
    }

    public JPanel getTitleBar() {
        return titleBar;
    }

    public String getTitleText() {
        return titleLabel.getText();
    }

    public void setTitleText(String title) {
        titleLabel.setText(title);
    }

    public Icon getTitleIcon() {
        return titleLabel.getIcon();
    }

    public void setTitleIcon(Icon icon) {
        titleLabel.setIcon(icon);
    }
}
