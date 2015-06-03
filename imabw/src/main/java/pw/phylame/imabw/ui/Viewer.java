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

package pw.phylame.imabw.ui;

import pw.pat.ixin.IFrame;
import pw.pat.ixin.IAction;
import pw.pat.ixin.IToolkit;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultTreeModel;

import pw.phylame.imabw.Imabw;
import pw.phylame.imabw.Constants;
import pw.phylame.imabw.ui.com.*;
import pw.phylame.jem.core.Part;

import java.util.Map;
import java.util.Arrays;
import java.util.ArrayList;

/**
 * Main frame board of Imabw.
 */
public class Viewer extends IFrame implements Constants {
    private static Imabw app = Imabw.getInstance();

    private JPanel rootPane;

    // split pane
    private JSplitPane splitPane;
    private Action nextWindowAction = null;
    private Action prevWindowAction = null;

    // ******************
    // ** Contents tree
    // ******************
    private NavigateTree                contentsTree;
    private DefaultTreeModel     treeModel;
    private JPopupMenu           treeContextMenu;
    private Map<String, IAction> treeActions;
    private TreeOptionsPane      treeOptionsPane;
    private boolean contentsLocked = false;

    // ******************
    // ** Tabbed editor
    // ******************
    private JTabbedPane          editorWindow;
    private JPopupMenu           tabContextMenu;
    private Map<String, IAction> tabActions;
    private ArrayList<EditorTab> editorTabs = new ArrayList<>();

    private EditorIndicator editorIndicator;

    public Viewer() {
        super(app.getText("App.Name"), UIDesign.MENU_ACTIONS, UIDesign.MENU_BAR_MODEL, UIDesign.TOOL_BAR_MODEL);
        setIconImage(IToolkit.createImage(app.getText("App.Icon")));

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                app.onCommand(Constants.EXIT_APP);
            }
        });

        createComponents();

        updateState();

        focusToTreeWindow();
    }

    @Override
    public void initializing() {
        // using ITextEdit edit actions
        getActions().putAll(ITextEdit.getContextActions());
    }

    public void destroy() {
        UIState uiState = UIState.getInstance();
        uiState.setViewerSize(getSize());
        uiState.setViewerLocation(getLocation());

        uiState.setDividerSize(splitPane.getDividerSize());
        uiState.setDividerLocation(splitPane.getDividerLocation());
    }

    private void createComponents() {
        // contents tree
        createContentsWindow();
        splitPane.setLeftComponent(contentsTree);

        // tabbed editor
        createEditorWindow();

        getContentArea().add(rootPane, BorderLayout.CENTER);

        // editor indicator
        editorIndicator = new EditorIndicator();
        getStatusBar().add(editorIndicator.getPane(), BorderLayout.EAST);
    }

    private void updateState() {
        setToolBarVisible(app.getConfig().isShowToolbar());
        setToolBarLocked(app.getConfig().isLockToolbar());
        setStatusBarVisible(app.getConfig().isShowStatusbar());
        setSideBarVisible(app.getConfig().isShowSidebar());

        JMenu menu = getViewMenu();
        menu.getItem(0).setSelected(getToolBar().isVisible());
        menu.getItem(1).setSelected(getStatusBar().isVisible());
        menu.getItem(2).setSelected(app.getConfig().isShowSidebar());
        ((JCheckBoxMenuItem) getToolBar().getComponentPopupMenu().getComponent(0)).setSelected(
                app.getConfig().isLockToolbar());

        UIState uiState = UIState.getInstance();
        setSize(uiState.getViewerSize()); // 16x9

        Point location = uiState.getViewerLocation();
        if (location != null) {
            setLocation(location);
        } else {
            setLocationByPlatform(true);
        }
    }

    @Override
    public String getText(String key) {
        return app.getText(key);
    }

    @Override
    public String getText(String key, Object... args) {
        return app.getText(key, args);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        app.onCommand(e.getActionCommand());
    }

    private void createContentsPopupMenu() {
        treeActions = IToolkit.createActions(UIDesign.TREE_POPUP_MENU_ACTIONS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                app.onTreeAction(e.getActionCommand());
            }
        }, this);
        treeContextMenu = new JPopupMenu();
        IToolkit.addMenuItems(treeContextMenu, UIDesign.TREE_POPUP_MENU_MODEL, treeActions, this);
    }

    private void createContentsWindow() {
        createContentsPopupMenu();

        treeModel = new DefaultTreeModel(null);
        contentsTree = new NavigateTree(app.getText("Frame.Contents.Title")+" ",
                IToolkit.createImageIcon(app.getText("Frame.Contents.TitleIcon")), treeModel);
//        contentsTree.setTitleIcon(IToolkit.createImageIcon(app.getText("Frame.Contents.TitleIcon")));

        treeOptionsPane = new TreeOptionsPane();
        contentsTree.getTitleBar().add(treeOptionsPane.getPane(), BorderLayout.CENTER);

        setTreeStyle(contentsTree.getTree());
    }

    private void setTreeStyle(final JTree tree) {
        // cell renderer
        tree.setCellRenderer(new PartTreeCellRender());

        // split action
        addSplitAction(tree);

        // add tree context actions to tree
        IToolkit.addKeyboardActions(tree, treeActions.values(), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // for open chapter text, only left key
                if (e.getClickCount() != 2 || e.isMetaDown()) {
                    return;
                }
                app.getManager().onTreeAction(VIEW_CHAPTER);
            }

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
        }, KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        tree.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                app.getManager().onTreeAction(VIEW_CHAPTER);
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void showContentsMenu(int x, int y) {
        JTree tree = contentsTree.getTree();
        TreePath[] selectedPaths = tree.getSelectionPaths();
        if (selectedPaths == null) {
            return;
        }

        // disable all
        for (IAction action: treeActions.values()) {
            action.setEnabled(false);
        }

        java.util.List<String> keys = new ArrayList<>();    // enable menu item
        switch (selectedPaths.length) {
            case 0:     // no selection disable all
                break;
            case 1:
                if (contentsLocked) {
                    keys.addAll(Arrays.asList(SAVE_CHAPTER, TREE_PROPERTIES));
                } else {
                    keys.addAll(treeActions.keySet());
                    keys.remove(MERGE_CHAPTER);     // one item cannot be merged
                }
                /* section cannot import */
//                PartNode node = PartNode.getPartNode(selectedPaths[0]);
//                if (node.getChildCount() != 0) {
//                    menuStatus.put(IMPORT_CHAPTER, false);
//                }
                break;
            default:    // multi selection
                if (! contentsLocked) {     // not locked
                    keys.addAll(Arrays.asList(MOVE_CHAPTER, DELETE_CHAPTER, MERGE_CHAPTER));
                }
                break;
        }
        if (tree.isPathSelected(tree.getPathForRow(0))) {       // selected root
            // root cannot insert, save, move, delete, merge
            String[] _keys = {INSERT_CHAPTER, SAVE_CHAPTER, MOVE_CHAPTER, DELETE_CHAPTER, MERGE_CHAPTER};
            keys.removeAll(Arrays.asList(_keys));
        }

        for (String key: keys) {
            IAction action = treeActions.get(key);
            if (action != null) {
                action.setEnabled(true);
            }
        }
        treeContextMenu.show(tree, x, y);
    }

    private void createEditorPopupMenu() {
        tabActions = IToolkit.createActions(UIDesign.TAB_POPUP_MENU_ACTIONS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                app.onTabAction(e.getActionCommand());
            }
        }, this);
        tabContextMenu = new JPopupMenu();
        IToolkit.addMenuItems(tabContextMenu, UIDesign.TAB_POPUP_MENU_MODEL, tabActions, this);
    }

    private void createEditorWindow() {
        createEditorPopupMenu();

        editorWindow.setComponentPopupMenu(tabContextMenu);

        editorWindow.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int index = editorWindow.getSelectedIndex();
                if (index == -1) {      // close all tabs
                    updateEditorIndicator(null);
                    switchEditableMenus(false);
                    focusToTreeWindow();
                    ITextEdit.updateContextMenu(null);
                } else {
                    EditorTab tab = editorTabs.get(index);
                    updateEditorIndicator(tab);
                    ITextEdit.updateContextMenu(tab.getTextEdit());
                }
            }
        });
    }

    public void setBook(Part part) {
        treeModel.setRoot(PartNode.makePartTree(part));
        focusToRoot();
        focusToTreeWindow();
    }

    // **********************
    // ** window operations
    // **********************
    public void focusNextWindow(JComponent currentWindow) {
        if (currentWindow == contentsTree.getTree()) {
            focusToEditorWindow();
        } else {
            focusToTreeWindow();
        }
    }

    public void focusPreviousWindow(JComponent currentWindow) {
        if (currentWindow == contentsTree.getTree()) {
            focusToEditorWindow();
        } else {
            focusToTreeWindow();
        }
    }

    private void addSplitAction(final JComponent comp) {
        if (nextWindowAction == null) {
            nextWindowAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    focusNextWindow((JComponent) e.getSource());
                }
            };
        }
        if (prevWindowAction == null) {
            prevWindowAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    focusPreviousWindow((JComponent) e.getSource());
                }
            };
        }
        comp.registerKeyboardAction(prevWindowAction, KeyStroke.getKeyStroke("alt shift LEFT"),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        comp.registerKeyboardAction(nextWindowAction, KeyStroke.getKeyStroke("alt shift RIGHT"),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    // *******************************************
    // ** Contents tree operation (The Sidebar) **
    // *******************************************

    public boolean isSideBarVisible() {
        return contentsTree.isVisible();
    }

    public void setSideBarVisible(boolean visible) {
        UIState uiState = UIState.getInstance();
        if (visible == isSideBarVisible()) {
            return;
        } else if (! visible) {    // hide
            uiState.setDividerLocation(splitPane.getDividerLocation());
            uiState.setDividerSize(splitPane.getDividerSize());
            splitPane.setDividerLocation(0);
            splitPane.setDividerSize(0);
        } else {
            int dividerLocation = uiState.getDividerLocation(), dividerSize = uiState.getDividerSize();
            splitPane.setDividerLocation(dividerLocation);
            splitPane.setDividerSize(dividerSize);
        }
        contentsTree.setVisible(visible);
    }

    public void showOrHideSideBar() {
        setSideBarVisible(! isSideBarVisible());
    }

    public boolean isContentsLocked() {
        return contentsLocked;
    }

    public void setContentsLocked(boolean contentsLocked) {
        this.contentsLocked = contentsLocked;
        treeOptionsPane.setButtonLock(contentsLocked);
    }

    public void focusToTreeWindow() {
        contentsTree.requestFocus();
    }

    public void expandPath(TreePath path) {
        contentsTree.getTree().expandPath(path);
    }

    public void collapsePath(TreePath path) {
        contentsTree.getTree().collapsePath(path);
    }

    public TreePath getSelectedPath() {
        return contentsTree.getTree().getSelectionPath();
    }

    public TreePath[] getSelectedPaths() {
        return contentsTree.getTree().getSelectionPaths();
    }

    public PartNode getSelectedNode() {
        TreePath path = getSelectedPath();
        if (path == null) {
            return null;
        }

        return PartNode.getPartNode(path);
    }

    public PartNode getRootNode() {
        return (PartNode) treeModel.getRoot();
    }

    public void focusToRoot() {
        JTree tree = contentsTree.getTree();
        tree.setSelectionRow(0);
        tree.scrollRowToVisible(0);
    }

    public void refreshNode(PartNode node) {
        treeModel.reload(node);
    }

    public int getRowForPath(TreePath path) {
        return contentsTree.getTree().getRowForPath(path);
    }

    public void focusToPath(TreePath path) {
        contentsTree.getTree().setSelectionPath(path);
    }

    public void focusToRow(int row) {
        contentsTree.getTree().setSelectionRow(row);
    }

    public void focusToNode(TreePath path, PartNode node) {
        Object[] objects = new Object[path.getPathCount()+1];
        System.arraycopy(path.getPath(), 0, objects, 0, objects.length-1);
        objects[objects.length-1] = node;
        TreePath childPath = new TreePath(objects);
        focusToPath(childPath);
    }

    /**
     * Notifies than the specified node has been updated.
     * @param node the node
     */
    public void updatedNode(PartNode node) {
        treeModel.nodeChanged(node);
    }

    /**
     * Notifies one node has been appened to the specified node.
     * @param parent the parent node
     */
    public void appendedNode(PartNode parent) {
        insertedNode(parent, parent.getChildCount() - 1);
    }

    public void insertedNode(PartNode parent, int index) {
        treeModel.nodesWereInserted(parent, new int[]{index});
    }

    public void insertedNodes(PartNode parent, int[] indexes) {
        treeModel.nodesWereInserted(parent, indexes);
    }

    public void removedNode(PartNode parent, int index, PartNode node) {
        treeModel.nodesWereRemoved(parent, new int[]{index}, new Object[]{node});
    }

    public void removedNodes(PartNode parent, int[] indexes, PartNode[] nodes) {
        treeModel.nodesWereRemoved(parent, indexes, nodes);
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

    public int getTabCount() {
        return editorTabs.size();
    }

    public void closeTab(EditorTab tab) {
        tab.cacheContent();
        editorWindow.remove(tab.getTextEdit());
        editorTabs.remove(tab);
    }

    public void closeTab(Part part) {
        EditorTab tab = findTab(part);
        if (tab != null) {
            closeTab(tab);
        }
    }

    public void closeActiveTab() {
        EditorTab tab = editorTabs.get(editorWindow.getSelectedIndex());
        closeTab(tab);
    }

    public void closeOtherTabs() {
        EditorTab tab = editorTabs.get(editorWindow.getSelectedIndex());
        EditorTab[] tabs = editorTabs.toArray(new EditorTab[0]);
        for (EditorTab item: tabs) {
            if (item != tab) {
                closeTab(item);
            }
        }
    }

    public void closeUnmodifiedTabs() {
        EditorTab[] tabs = editorTabs.toArray(new EditorTab[0]);
        for (EditorTab item: tabs) {
            if (! item.isModified()) {
                closeTab(item);
            }
        }
    }

    public void closeAllTabs() {
        cacheAllTabs();
        editorWindow.removeAll();
        editorTabs.clear();
    }

    public void cacheAllTabs() {
        for (EditorTab tab: editorTabs) {
            tab.cacheContent();
        }
    }

    public EditorTab getActiveTab() {
        try {
            return editorTabs.get(editorWindow.getSelectedIndex());
        } catch (IndexOutOfBoundsException e) {     // no tab opened
            return null;
        }
    }

    // add and view new tab
    public EditorTab newTab(Part part) {
        String text;
        try {
            text = part.getText();
        } catch (java.io.IOException e) {
            text = "";
        }
        EditorTab tab = new EditorTab(new ITextEdit(text, this), part,
                app.getConfig().getPmabMaTextEncoding());
        initEditorTab(tab);
        editorTabs.add(tab);
        editorWindow.addTab(tab.getPart().getTitle(),
                IToolkit.createImageIcon(":/res/gfx/tree/chapter.png"), tab.getTextEdit());

        if (editorTabs.size() == 1) {   // first open tab activate search menus
            switchEditableMenus(true);
        }

        return tab;
    }

    private void initEditorTab(final EditorTab tab) {
        final ITextEdit textEdit = tab.getTextEdit();

        IToolkit.addKeyboardActions(textEdit.getTextEditor(), tabActions.values(),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        setEditorStyle(textEdit);

        textEdit.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                ITextEdit.updateContextMenu(textEdit);
                editorIndicator.setWords(textEdit.getText().length());
                tab.setModified(true);
                app.getManager().notifyModified(app.getText("Task.ContentModified", tab.getPart().getTitle()));
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                ITextEdit.updateContextMenu(textEdit);
                tab.setModified(true);
                editorIndicator.setWords(textEdit.getText().length());
                app.getManager().notifyModified(app.getText("Task.ContentModified", tab.getPart().getTitle()));
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        textEdit.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                ITextEdit.updateContextMenu(textEdit, e.getMark() - e.getDot() != 0);   // has selection or not
                editorIndicator.setRuler(textEdit.getCurrentRow() + 1, textEdit.getCurrentColumn() + 1,
                        textEdit.getSelectionCount());
            }
        });

        addSplitAction(textEdit.getTextEditor());
        ITextEdit.updateContextMenu(textEdit);
        textEdit.setCaretPosition(0);
    }

    private void setEditorStyle(ITextEdit textEdit) {
        JTextArea textArea = textEdit.getTextEditor();
        textArea.setLineWrap(app.getConfig().isEditorLineWarp());
        textArea.setWrapStyleWord(app.getConfig().isEditorWordWarp());

        textArea.setFont(app.getConfig().getEditorFont());

        textArea.setBackground(app.getConfig().getEditorBackground());
        textArea.setForeground(app.getConfig().getEditorForeground());
    }

    private void switchEditableMenus(boolean enable) {
        String[] keys = {FIND_TEXT, FIND_AND_REPLACE, GO_TO_POSITION};
        IAction action;
        for (String key: keys) {
            action = getAction(key);
            if (action != null) {
                action.setEnabled(enable);
            }
        }
        if (! enable) {
            ITextEdit.updateContextMenu(null);
        }
    }

    public EditorTab findTab(Part part) {
        for (EditorTab tab: editorTabs) {
            if (part == tab.getPart()) {
                return tab;
            }
        }
        return null;
    }

    public void switchToTab(EditorTab tab) {
        ITextEdit textEdit = tab.getTextEdit();
        editorWindow.setSelectedComponent(textEdit);
        textEdit.requestFocus();
        updateEditorIndicator(tab);
    }

    private void updateEditorIndicator(EditorTab tab) {
        if (tab != null) {
            ITextEdit textEdit = tab.getTextEdit();
            editorIndicator.setRuler(textEdit.getCurrentRow() + 1, textEdit.getCurrentColumn() + 1,
                    textEdit.getSelectionCount());
            editorIndicator.setEncoding(tab.getEncoding());
            editorIndicator.setWords(textEdit.getText().length());
        } else {
            editorIndicator.setRuler(-1, -1, 0);
            editorIndicator.setEncoding(null);
            editorIndicator.setWords(-1);
        }
    }

    public void nextTab() {
        if (getTabCount() < 2) {
            return;
        }
        int index = editorWindow.getSelectedIndex();
        if (index == getTabCount() - 1) {
            index = 0;
        } else {
            ++index;
        }
        editorWindow.setSelectedIndex(index);
    }

    public void previousTab() {
        if (getTabCount() < 2) {
            return;
        }
        int index = editorWindow.getSelectedIndex();
        if (index == 0) {
            index = getTabCount() - 1;
        } else {
            --index;
        }
        editorWindow.setSelectedIndex(index);
    }

    // *******************
    // ** Menu operations
    // *******************

    public JMenu getFileMenu() {
        return getJMenuBar().getMenu(0);
    }

    public JMenu getEditMenu() {
        return getJMenuBar().getMenu(1);
    }

    public JMenu getViewMenu() {
        return getJMenuBar().getMenu(2);
    }

    public JMenu getToolsMenu() {
        return getJMenuBar().getMenu(3);
    }
}