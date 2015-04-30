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

package pw.phylame.ixin;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Tree with title label and scroll panel.
 */
public class ITree extends JPanel {

    public ITree() {
        this("ITree");
    }

    public ITree(String title) {
        super(new BorderLayout());
        jTree = new JTree();
        initComp(title);
    }

    public ITree(String title, Object[] value) {
        super(new BorderLayout());
        jTree = new JTree(value);
        initComp(title);
    }

    public ITree(String title, TreeNode root) {
        super(new BorderLayout());
        jTree = new JTree(root);
        initComp(title);
    }

    public ITree(String title, TreeNode root, boolean asksAllowsChildren) {
        super(new BorderLayout());
        jTree = new JTree(root, asksAllowsChildren);
        initComp(title);
    }

    public ITree(String title, TreeModel newModel) {
        super(new BorderLayout());
        jTree = new JTree(newModel);
        initComp(title);
    }

    private void initComp(String title) {
        titleBar = new JPanel(new BorderLayout());
        titleLabel = new JLabel(title);
        titleBar.add(titleLabel, BorderLayout.WEST);
        add(titleBar, BorderLayout.NORTH);
        add(new JScrollPane(jTree), BorderLayout.CENTER);
    }

    public JPanel getTitleBar() {
        return titleBar;
    }

    public String getTitle() {
        return titleLabel.getText();
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public Icon getTitleIcon() {
        return titleLabel.getIcon();
    }

    public void setTitleIcon(Icon icon) {
        titleLabel.setIcon(icon);
    }

    public void setSelectionPath(TreePath path) {
        jTree.setSelectionPath(path);
    }

    public void setSelectionRow(int row) {
        jTree.setSelectionRow(row);
    }

    public int getSelectionCount() {
        return jTree.getSelectionCount();
    }

    public int getSelectionRow() {
        return jTree.getLeadSelectionRow();
    }

    public TreePath getSelectionPath() {
        return jTree.getSelectionPath();
    }

    public TreePath[] getSelectionPaths() {
        return jTree.getSelectionPaths();
    }

    public void focusToTreeNode(TreePath parent, TreeNode node) {
        int row = jTree.getRowForPath(parent);
        row += node.getParent().getChildCount();
        jTree.setSelectionRow(row);
    }

    public void focusToTreeNode(TreePath parent, int index) {
        int row = jTree.getRowForPath(parent);
        if (! jTree.isExpanded(parent)) {
            jTree.expandPath(parent);
        }
        jTree.setSelectionRow(row + index + 1);
    }


    /** Focus to tree. */
    @Override
    public void requestFocus() {
        jTree.requestFocus();
    }

    public JTree getTree() {
        return jTree;
    }

    // private data
    private JPanel titleBar;
    private JLabel titleLabel;
    private JTree jTree;
}
