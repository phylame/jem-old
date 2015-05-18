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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

import pw.phylame.imabw.Imabw;
import pw.phylame.imabw.ui.com.PartNode;

public class PartSelectionDialog extends JDialog {
    /** Select chapter and section */
    public static final int CHAPTER_OR_SECTION = 0;
    /** Only select chapter */
    public static final int CHAPTER_ONLY       = 1;
    /** Only select section */
    public static final int SECTION_ONLY       = 2;

    private JPanel  contentPane;
    private JLabel  labelTip;
    private JTree   contentsTree;
    private JButton buttonOK;
    private JButton buttonCancel;

    private TreePath treePath = null;

    public PartSelectionDialog(Frame owner, String title, int mode, TreeNode root, TreePath initPath) {
        super(owner, title, true);
        init(mode, root, initPath);
    }

    public PartSelectionDialog(Dialog owner, String title, int mode, TreeNode root, TreePath initPath) {
        super(owner, title, true);
        init(mode, root, initPath);
    }

    private void init(final int mode, TreeNode root, TreePath initPath) {
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);

        Imabw app = Imabw.getApplication();

        labelTip.setText(app.getText("Dialog.SelectPart.Tip"));

        contentsTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        ((DefaultTreeModel) contentsTree.getModel()).setRoot(root);
        contentsTree.setSelectionRow(0);
        contentsTree.requestFocus();

        if (mode == SECTION_ONLY || mode == CHAPTER_OR_SECTION) {
            buttonOK.setEnabled(true);
        }
        if (initPath != null) {
            contentsTree.setSelectionPath(initPath);
        }
        contentsTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // for open chapter text, only left key
                if (e.getClickCount() != 2 || e.isMetaDown()) {
                    return;
                }
                TreePath selectionPath = contentsTree.getSelectionPath();
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
        contentsTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                PartNode node = PartNode.getPartNode(e.getPath());
                if (node == null) {
                    buttonOK.setEnabled(false);
                    return;
                }
                if (mode == CHAPTER_ONLY) {   // select chapter
                    if (!node.isLeaf()) {
                        buttonOK.setEnabled(false);
                        return;
                    }
                }
                buttonOK.setEnabled(true);
            }
        });
        contentsTree.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TreePath path = contentsTree.getSelectionPath();
                PartNode node = PartNode.getPartNode(path);
                if (node == null) {
                    return;
                }
                if (!node.isLeaf()) {
                    contentsTree.expandPath(path);
                    return;
                }
                onOK();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        buttonOK.setText(app.getText("Dialog.SelectPart.ButtonSelect"));
        buttonOK.setToolTipText(app.getText("Dialog.SelectPart.ButtonSelect.Tip"));
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.setText(app.getText("Dialog.SelectPart.ButtonCancel"));
        buttonCancel.setToolTipText(app.getText("Dialog.SelectPart.ButtonCancel.Tip"));
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
        treePath = contentsTree.getSelectionPath();
        dispose();
    }

    private void onCancel() {
        treePath = null;
        dispose();
    }

    public static TreePath selectPart(Dialog owner, String title, int mode, TreeNode root, TreePath initPath) {
        PartSelectionDialog dialog = new PartSelectionDialog(owner, title, mode, root, initPath);
        dialog.setVisible(true);
        return dialog.treePath;
    }

    public static TreePath selectPart(Frame owner, String title, int mode, TreeNode root, TreePath initPath) {
        PartSelectionDialog dialog = new PartSelectionDialog(owner, title, mode, root, initPath);
        dialog.setVisible(true);
        return dialog.treePath;
    }
}
