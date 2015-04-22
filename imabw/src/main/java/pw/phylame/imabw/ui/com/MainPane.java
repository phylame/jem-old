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

import pw.phylame.ixin.IToolkit;
import pw.phylame.ixin.ITree;
import pw.phylame.ixin.com.IAction;
import pw.phylame.ixin.com.IPaneRender;
import pw.phylame.ixin.event.IActionEvent;
import pw.phylame.ixin.event.IActionListener;
import pw.phylame.jem.core.Book;

import pw.phylame.imabw.Application;
import pw.phylame.imabw.ui.UIDesign;
import static pw.phylame.imabw.Constants.*;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;

/**
 * Main pane of Imabw.
 */
public class MainPane extends IPaneRender {
    private Application app = Application.getApplication();
    private JPanel rootPane;
    // split pane
    private JSplitPane splitPane;
    // status
    private int dividerLocation, dividerSize;
    // ******************
    // ** Contents tree
    // ******************
    private ITree contentsTree;
    private BookTreeModel treeModel;
    private JPopupMenu treeContextMenu;
    private Map<Object, IAction> treeActions;

    // ******************
    // ** Tabbed editor
    // ******************
    private JTabbedPane editorWindow;
    private JPopupMenu tabContextMenu;
    private Map<Object, IAction> tabActions;

    public MainPane() {
        initComp();

        dividerLocation = splitPane.getDividerLocation();
        dividerSize = splitPane.getDividerSize();

        focusToTreeWindow();
    }

    private void initComp() {
        createContentsWindow();
        splitPane.setLeftComponent(contentsTree);
        createEditorWindow();
    }

    private void createContentsPopupMenu() {
        if (treeContextMenu != null) {
            return;
        }
        treeActions = IToolkit.createActions(UIDesign.TREE_POPUP_MENU_ACTIONS, new IActionListener() {
            @Override
            public void actionPerformed(IActionEvent e) {
                app.onTreeAction(e.getAction().getId());
            }
        });
        treeContextMenu = new JPopupMenu();
        IToolkit.addMenuItem(treeContextMenu, UIDesign.TREE_POPUP_MENU_MODEL, treeActions,
                app.getViewer());
    }

    private void createContentsWindow() {
        if (contentsTree != null) {
            return;
        }
        createContentsPopupMenu();
        treeModel = new BookTreeModel();
        contentsTree = new ITree(app.getText("Frame.Contents.Title"), treeModel);
        contentsTree.setTitleIcon(IToolkit.createImageIcon(app.getText("Frame.Contents.TitleIcon")));
        final JTree tree = contentsTree.getTree();
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (!e.isMetaDown()) {
                    return;
                }
                TreePath treePath = tree.getPathForLocation(e.getX(), e.getY());
                if (treePath != null && !tree.isPathSelected(treePath)) {    // not in selection paths
                    tree.setSelectionPath(treePath);
                }
                showContentsMenu(e.getX(), e.getY());
            }
        });
        tree.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                java.awt.Rectangle rect = tree.getPathBounds(tree.getSelectionPath());
                if (rect == null) {
                    return;
                }
                showContentsMenu((int) rect.getX(), (int) rect.getY());
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void showContentsMenu(int x, int y) {
        JTree tree = contentsTree.getTree();
        TreePath[] selectedPaths = tree.getSelectionPaths();
        if (selectedPaths == null) {
//            log.trace("No selected path");
            return;
        }

        Map<Object, Boolean> menuStatus = new java.util.HashMap<>();
        for (Object id: treeActions.keySet()) {
            menuStatus.put(id, true);
        }

        switch (selectedPaths.length) {
            case 0:     // all disable
                for (Object id: treeActions.keySet()) {
                    menuStatus.put(id, false);
                }
                break;
            case 1:
                menuStatus.put(TREE_MERGE, false);
                /* section cannot import */
                BookTreeModel.PartNode node = BookTreeModel.getPartNode(selectedPaths[0]);
                if (node.getChildCount() != 0) {
                    menuStatus.put(TREE_IMPORT, false);
                }
                break;
            default:
                // cannot add, insert, save as, rename, properties, import
                Object[] keys = {TREE_NEW, TREE_INSERT, TREE_IMPORT, TREE_SAVE_AS, TREE_RENAME,
                        TREE_PROPERTIES};
                for (Object id: keys) {
                    menuStatus.put(id, false);
                }
                break;
        }
        if (tree.isPathSelected(tree.getPathForRow(0))) {
            // root cannot insert, save as, move, delete, merge
            Object[] keys = {TREE_INSERT, TREE_SAVE_AS, TREE_IMPORT, TREE_MOVE, TREE_DELETE, TREE_MERGE};
            for (Object id: keys) {
                menuStatus.put(id, false);
            }
        }
        for (Map.Entry<Object, Boolean> entry: menuStatus.entrySet()) {
            IAction action = treeActions.get(entry.getKey());
            if (action != null) {
                action.setEnabled(entry.getValue());
            }
        }
        treeContextMenu.show(tree, x, y);
    }

    private void createEditorPopupMenu() {
        if (tabContextMenu != null) {
            return;
        }
        tabActions = IToolkit.createActions(UIDesign.TAB_POPUP_MENU_ACTIONS, new IActionListener() {
            @Override
            public void actionPerformed(IActionEvent e) {
                app.onTabAction(e.getAction().getId());
            }
        });
        tabContextMenu = new JPopupMenu();
        IToolkit.addMenuItem(tabContextMenu, UIDesign.TAB_POPUP_MENU_MODEL, tabActions,
                app.getViewer());
    }

    private void createEditorWindow() {
        createEditorPopupMenu();
        editorWindow.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (! e.isMetaDown() || editorWindow.getTabCount() == 0) {  // not meta key or empty editor
                    return;
                }
                tabContextMenu.show(editorWindow, e.getX(), e.getY());
            }
        });
    }

    // ****************************************
    // ** Contents operation (The Sidebar)
    // ****************************************
    public void showOrHideSideBar() {
        boolean visible = ! contentsTree.isVisible();
        if (! visible) {    // hide
            dividerLocation = splitPane.getDividerLocation();
            dividerSize = splitPane.getDividerSize();
            splitPane.setDividerLocation(0);
            splitPane.setDividerSize(0);
        } else {
            splitPane.setDividerLocation(dividerLocation);
            splitPane.setDividerSize(dividerSize);
        }
        contentsTree.setVisible(visible);
    }

    public void focusToTreeWindow() {
        contentsTree.requestFocus();
    }

    public void setBook(Book book) {
        treeModel.setBook(book);
        contentsTree.setSelectionRow(0);
        focusToTreeWindow();
    }

    // ******************************
    // ** Editor operations
    // ******************************
    public void focusToEditorWindow() {
        Component comp = editorWindow.getSelectedComponent();
        if (comp != null) {
            comp.requestFocus();
        }
    }

    public void closeAllTabs() {

    }

    public void nextTab() {

    }

    public void prevTab() {

    }

    public void setEditorState(int row, int column) {

    }

    @Override
    public void destroy() {

    }

    @Override
    public JPanel getPane() {
        return rootPane;
    }

}
