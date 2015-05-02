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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import pw.phylame.imabw.Application;
import pw.phylame.imabw.Constants;
import pw.phylame.imabw.ui.com.EditorIndicator;
import pw.phylame.imabw.ui.com.EditorTab;
import pw.phylame.imabw.ui.com.PartNode;
import pw.phylame.imabw.ui.com.TreeOptionsPane;
import pw.phylame.ixin.ITextEdit;
import pw.phylame.ixin.IToolkit;
import pw.phylame.ixin.ITree;
import pw.phylame.ixin.com.IAction;
import pw.phylame.ixin.event.IActionEvent;
import pw.phylame.ixin.event.IActionListener;
import pw.phylame.ixin.frame.IFrame;
import pw.phylame.jem.core.Part;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 * Main frame board of Imabw.
 */
public class Viewer extends IFrame implements Constants {
    private Application app = Application.getApplication();
    private JPanel rootPane;

    // split pane
    private JSplitPane splitPane;
    private Action nextWindowAction = null;
    private Action prevWindowAction = null;

    // status
    private int dividerLocation, dividerSize;
    // ******************
    // ** Contents tree
    // ******************
    private ITree contentsTree;
    private DefaultTreeModel treeModel;
    private JPopupMenu treeContextMenu;
    private Map<Object, IAction> treeActions;
    private TreeOptionsPane treeOptionsPane;
    private boolean contentsLocked = false;

    // ******************
    // ** Tabbed editor
    // ******************
    private JTabbedPane editorWindow;
    private JPopupMenu tabContextMenu;
    private Map<Object, IAction> tabActions;
    private ArrayList<EditorTab> editorTabs = new ArrayList<>();

    private EditorIndicator editorIndicator;

    // global menu and toolbar actions
    static {
        IFrame.setActionsModel(UIDesign.MENU_ACTIONS);
        IFrame.setMenuBarModel(UIDesign.MENU_BAR_MODEL);
        IFrame.setToolBarModel(UIDesign.TOOL_BAR_MODEL);
    }

    public Viewer() {
        super();

        createComponents();

        setTitle(app.getText("App.Name"));
        setIconImage(IToolkit.createImage(app.getText("App.Icon")));

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onAction(Constants.EXIT_APP);
            }
        });

        getToolBar().setVisible((boolean) app.getSetting("ui.window.showToolbar"));
        getToolBar().setFloatable(! (boolean) app.getSetting("ui.window.lockToolbar"));
        getStatusBar().setVisible((boolean) app.getSetting("ui.window.showStatusbar"));

        JMenu menu = getViewMenu();
        ((JCheckBoxMenuItem)menu.getItem(0)).setState(getToolBar().isVisible());
        ((JCheckBoxMenuItem)menu.getItem(1)).setState(getStatusBar().isVisible());
        ((JCheckBoxMenuItem)menu.getItem(2)).setState((boolean) app.getSetting("ui.window.showSidebar"));

        setSize(1066, 600);
        setLocationRelativeTo(null);

        focusToTreeWindow();
    }

    @Override
    public void initialized() {
        // using ITextEdit edit actions
        getMenuActions().putAll(ITextEdit.getContextActions());
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

        if (app.getSetting("ui.window.showSidebar").equals(false)) {
            showOrHideSideBar();
        }
    }

    @Override
    public String getText(String key, Object... args) {
        return app.getText(key, args);
    }

    @Override
    public void onAction(Object actionID) {
        app.onCommand(actionID);
    }

    private void createContentsPopupMenu() {
        treeActions = IToolkit.createActions(UIDesign.TREE_POPUP_MENU_ACTIONS, new IActionListener() {
            @Override
            public void actionPerformed(IActionEvent e) {
                app.onTreeAction(e.getAction().getId());
            }
        });
        treeContextMenu = new JPopupMenu();
        IToolkit.addMenuItem(treeContextMenu, UIDesign.TREE_POPUP_MENU_MODEL, treeActions, app.getViewer());
    }

    private void createContentsWindow() {
        createContentsPopupMenu();

        treeModel = new DefaultTreeModel(null);
        contentsTree = new ITree(app.getText("Frame.Contents.Title")+" ", treeModel);
        contentsTree.setTitleIcon(IToolkit.createImageIcon(app.getText("Frame.Contents.TitleIcon")));

        treeOptionsPane = new TreeOptionsPane();
        contentsTree.getTitleBar().add(treeOptionsPane.getPane(), BorderLayout.CENTER);

        setTreeStyle(contentsTree.getTree());
    }

    private void setTreeStyle(final JTree tree) {
        // cell renderer
        tree.setCellRenderer(new DefaultTreeCellRenderer() {
            Icon bookIcon = IToolkit.createImageIcon(":/res/img/tree/book.png");
            Icon sectionIcon = IToolkit.createImageIcon(":/res/img/tree/section.png");
            Icon chapterIcon = IToolkit.createImageIcon(":/res/img/tree/chapter.png");

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
        });

        // split action
        addSplitAction(tree);
        // add tree context actions to tree
        IToolkit.addInputActions(tree, treeActions.values());

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // for open chapter text, only left key
                if (e.getClickCount() != 2 || e.isMetaDown()) {
                    return;
                }
                viewSelectionNode();
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
                viewSelectionNode();
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

        java.util.List<Object> keys = new ArrayList<>();    // enable menu item
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
            Object[] _keys = {INSERT_CHAPTER, SAVE_CHAPTER, MOVE_CHAPTER, DELETE_CHAPTER, MERGE_CHAPTER};
            keys.removeAll(Arrays.asList(_keys));
        }

        for (Object key: keys) {
            IAction action = treeActions.get(key);
            if (action != null) {
                action.setEnabled(true);
            }
        }
        treeContextMenu.show(tree, x, y);
    }

    private void viewSelectionNode() {
        TreePath[] paths = contentsTree.getSelectionPaths();
        if (paths == null) {
            return;
        }
        for (TreePath tp: paths) {
            PartNode node = PartNode.getPartNode(tp);
            assert node != null;
            if (! node.isLeaf()) {
                continue;
            }
            app.getManager().viewPart(node.getPart());
        }
    }

    private void createEditorPopupMenu() {
        tabActions = IToolkit.createActions(UIDesign.TAB_POPUP_MENU_ACTIONS, new IActionListener() {
            @Override
            public void actionPerformed(IActionEvent e) {
                app.onTabAction(e.getAction().getId());
            }
        });
        tabContextMenu = new JPopupMenu();
        IToolkit.addMenuItem(tabContextMenu, UIDesign.TAB_POPUP_MENU_MODEL, tabActions, app.getViewer());
    }

    private void createEditorWindow() {
        createEditorPopupMenu();

        editorWindow.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                // not meta key or empty editor
                if (! e.isMetaDown() || editorWindow.getTabCount() == 0) {
                    return;
                }
                tabContextMenu.show(editorWindow, e.getX(), e.getY());
            }
        });
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

    // ****************************************
    // ** Contents tree operation (The Sidebar)
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

    public void expandTreePath(TreePath path) {
        contentsTree.getTree().expandPath(path);
    }

    public TreePath getSelectedPath() {
        return contentsTree.getSelectionPath();
    }

    public TreePath[] getSelectedPaths() {
        return contentsTree.getSelectionPaths();
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

    public void refreshNode(PartNode node) {
        treeModel.reload(node);
    }

    public void focusToRoot() {
        contentsTree.setSelectionRow(0);
    }

    public void focusToPath(TreePath path) {
        contentsTree.setSelectionPath(path);
    }

    // focus to child node in parent, index is child index in parent
    public void focusToRow(TreePath parent, int index) {
        JTree tree = contentsTree.getTree();
        int row = tree.getRowForPath(parent);
        if (! tree.isExpanded(parent)) {
            tree.expandPath(parent);
        }
        tree.setSelectionRow(row + index + 1);
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
        for (EditorTab tab: editorTabs) {
            tab.cacheContent();
        }
        editorWindow.removeAll();
        editorTabs.clear();
    }

    public EditorTab getActiveTab() {
        return editorTabs.get(editorWindow.getSelectedIndex());
    }

    // add and view new tab
    public EditorTab newTab(Part part) {
        String text;
        try {
            text = part.getText();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            text = "";
        }
        EditorTab tab = new EditorTab(new ITextEdit(text, app.getViewer()), part,
                (String) app.getSetting("jem.pmab.textEncoding"));
        initEditorTab(tab);
        editorTabs.add(tab);
        editorWindow.addTab(tab.getPart().getTitle(), tab.getTextEdit());

        if (editorTabs.size() == 1) {   // first open tab activate search menus
            switchEditableMenus(true);
        }

        return tab;
    }

    private void initEditorTab(final EditorTab tab) {
        final ITextEdit textEdit = tab.getTextEdit();

        IToolkit.addInputActions(textEdit.getTextEditor(), tabActions.values());

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
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        textArea.setFont((Font) app.getSetting("editor.style.font"));

        textArea.setBackground((Color) app.getSetting("editor.style.background"));
        textArea.setForeground((Color) app.getSetting("editor.style.foreground"));
    }

    private void switchEditableMenus(boolean enable) {
        String[] keys = {FIND_TEXT, FIND_AND_REPLACE, GO_TO_POSITION};
        IAction action;
        for (String key: keys) {
            action = app.getViewer().getMenuAction(key);
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
        if (editorWindow.getTabCount() < 2) {
            return;
        }
        int index = editorWindow.getSelectedIndex();
        if (index == editorWindow.getTabCount() - 1) {
            index = 0;
        } else {
            ++index;
        }
        editorWindow.setSelectedIndex(index);
    }

    public void previousTab() {
        if (editorWindow.getTabCount() < 2) {
            return;
        }
        int index = editorWindow.getSelectedIndex();
        if (index == 0) {
            index = editorWindow.getTabCount() - 1;
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