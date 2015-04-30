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

package pw.phylame.imabw.ui.dialog;

import pw.phylame.imabw.Application;
import pw.phylame.imabw.ui.com.PartNode;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.*;

public class SelectPartDialog extends JDialog {
    /** Select chapter and section */
    public static final int CHAPTER_OR_SECTION = 0;
    /** Only select chapter */
    public static final int CHAPTER_ONLY = 1;
    /** Only select section */
    public static final int SECTION_ONLY = 2;

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel labelTip;
    private JTree tree;

    private TreePath treePath = null;

    public SelectPartDialog(Frame owner, String title, final int model, TreeNode root, TreePath initPath) {
        super(owner, title, true);
        init(model, root, initPath);
    }

    public SelectPartDialog(Dialog owner, String title, final int model, TreeNode root, TreePath initPath) {
        super(owner, title, true);
        init(model, root, initPath);
    }

    private void init(final int model, TreeNode root, TreePath initPath) {
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);

        Application app = Application.getApplication();

        labelTip.setText(app.getText("Dialog.SelectPart.Tip"));

        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        ((DefaultTreeModel) tree.getModel()).setRoot(root);
        tree.setSelectionRow(0);
        tree.requestFocus();
        if (model == SECTION_ONLY || model == CHAPTER_OR_SECTION) {
            buttonOK.setEnabled(true);
        }
        if (initPath != null) {
            tree.setSelectionPath(initPath);
        }
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // for open chapter text, only left key
                if (e.getClickCount() != 2 || e.isMetaDown()) {
                    return;
                }
                TreePath selectionPath = tree.getSelectionPath();
                if (selectionPath == null) {
                    return;
                }
                TreeNode node = (TreeNode) selectionPath.getLastPathComponent();
                if (! node.isLeaf()) {   // not base chapter
                    return;
                }
                onOK();
            }
        });
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                PartNode node = PartNode.getPartNode(e.getPath());
                if (node == null) {
                    buttonOK.setEnabled(false);
                    return;
                }
                if (model == CHAPTER_ONLY) {   // select chapter
                    if (! node.isLeaf()) {
                        buttonOK.setEnabled(false);
                        return;
                    }
                }
                buttonOK.setEnabled(true);
            }
        });
        tree.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TreePath path = tree.getSelectionPath();
                PartNode node = PartNode.getPartNode(path);
                if (node == null) {
                    return;
                }
                if (! node.isLeaf()) {
                    tree.expandPath(path);
                    return;
                }
                onOK();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        setSize(323, 471);
        setLocationRelativeTo(getOwner());
    }

    private void onOK() {
// add your code here
        treePath = tree.getSelectionPath();
        dispose();
    }

    private void onCancel() {
// add your code here if necessary
        treePath = null;
        dispose();
    }

    public static TreePath selectPart(Component owner, String title, int model, TreeNode root, TreePath initPath) {
        SelectPartDialog dialog;
        if (owner instanceof Frame) {
            dialog = new SelectPartDialog((Frame) owner, title, model, root, initPath);
        } else if (owner instanceof Dialog) {
            dialog = new SelectPartDialog((Dialog) owner, title, model, root, initPath);
        } else {
            throw new IllegalArgumentException("owner must be Frame or Dialog");
        }
        dialog.setVisible(true);
        return dialog.treePath;
    }
}
