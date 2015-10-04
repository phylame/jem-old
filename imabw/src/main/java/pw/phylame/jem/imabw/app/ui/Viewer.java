/*
 * Copyright 2015 Peng Wan <phylame@163.com>
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

package pw.phylame.jem.imabw.app.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;

import pw.phylame.gaf.ixin.*;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.imabw.app.Imabw;
import pw.phylame.jem.imabw.app.Constants;
import pw.phylame.jem.imabw.app.config.GUISnap;
import pw.phylame.jem.imabw.app.data.BookTask;
import pw.phylame.jem.imabw.app.ui.editor.EditorTab;
import pw.phylame.jem.imabw.app.data.FileHistory;
import pw.phylame.jem.imabw.app.data.ParserData;
import pw.phylame.jem.imabw.app.ui.dialog.DialogFactory;
import pw.phylame.jem.imabw.app.ui.com.StatusIndicator;
import pw.phylame.jem.imabw.app.ui.com.WelcomePane;
import pw.phylame.jem.imabw.app.ui.editor.TextEditor;
import pw.phylame.jem.imabw.app.ui.tree.NavigateTree;
import pw.phylame.jem.imabw.app.ui.editor.TabbedEditor;

/**
 * The main frame of Imabw.
 */
public class Viewer extends IFrame implements Constants {
    // Imabw application instance
    private static Imabw app = Imabw.getInstance();

    private JSplitPane      mSplitPane;
    private NavigateTree navigateTree;
    private TabbedEditor tabbedEditor;
    private WelcomePane     mWelcomePane;
    private StatusIndicator mStatusIndicator;

    private enum ActiveComponent {
        None, NavigateTree, TabbedEditor
    }

    private ActiveComponent activeComponent = ActiveComponent.None;

    public Viewer() {
        createComponents();
        initialize();
    }

    private void createComponents() {
        // menu actions
        createMenuActions();

        // IFrame components
        super.createComponents(UIDesign.MENU_BAR_MODEL, UIDesign.TOOL_BAR_MODEL);
        updateHistoryMenu();

        GUISnap guiSnap = GUISnap.getInstance();

        // tool bar
        IToolBar toolBar = getToolBar();
        toolBar.getDefaultToolBar().setRollover(true);
        toolBar.setVisible(guiSnap.isShowToolBar());

        createContentArea();

        // status indicator
        mStatusIndicator = new StatusIndicator(this);

        // status bar
        IStatusBar statusBar = getStatusBar();
        statusBar.add(mStatusIndicator, BorderLayout.LINE_END);
        statusBar.setVisible(guiSnap.isShowStatusBar());
    }

    private void createMenuActions() {
        createFileActions();
        createEditActions();
        createViewActions();
        createBookActions();
        createWindowActions();
        createHelpActions();
    }

    private void createFileActions() {
        new MenuAction(NEW_FILE) {
            @Override
            public void actionPerformed(ActionEvent e) {
                app.newFile(null);
            }
        };

        new MenuAction(OPEN_FILE) {
            @Override
            public void actionPerformed(ActionEvent e) {
                app.openFile();

            }
        };

        new MenuAction(SAVE_FILE) {
            @Override
            public void actionPerformed(ActionEvent e) {
                app.saveFile();
            }
        };

        new MenuAction(SAVE_AS_FILE) {
            @Override
            public void actionPerformed(ActionEvent e) {
                app.saveAsFile();
            }
        };

        new MenuAction(FILE_DETAILS, false) {
            @Override
            public void actionPerformed(ActionEvent e) {
                DialogFactory.fileDetails(Viewer.this, app.getActiveTask().getSource());
            }
        };

        new MenuAction(EXIT_APP) {
            @Override
            public void actionPerformed(ActionEvent e) {
                app.exitApp();
            }
        };

        new MenuAction(CLEAR_HISTORY) {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileHistory.clear();
            }
        };
    }

    private void createEditActions() {
        new MenuAction(EDIT_UNDO, false) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Editable obj = getEditableComponent();
                if (obj != null) {
                    obj.undo();
                }
            }
        };
        new MenuAction(EDIT_REDO, false) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Editable obj = getEditableComponent();
                if (obj != null) {
                    obj.redo();
                }
            }
        };
        new MenuAction(EDIT_CUT, false) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Editable obj = getEditableComponent();
                if (obj != null) {
                    obj.cut();
                }
            }
        };
        new MenuAction(EDIT_COPY, false) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Editable obj = getEditableComponent();
                if (obj != null) {
                    obj.copy();
                }
            }
        };
        new MenuAction(EDIT_PASTE, false) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Editable obj = getEditableComponent();
                if (obj != null) {
                    obj.paste();
                }
            }
        };
        new MenuAction(EDIT_DELETE, false) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Editable obj = getEditableComponent();
                if (obj != null) {
                    obj.delete();
                }
            }
        };
        new MenuAction(FIND_CONTENT) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Editable obj = getEditableComponent();
                if (obj != null) {
                    obj.find();
                }
            }
        };
        new MenuAction(REPLACE_TEXT, false) {
            @Override
            public void actionPerformed(ActionEvent e) {
                // todo: replace action
            }
        };
        new MenuAction(FIND_NEXT, false) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Editable obj = getEditableComponent();
                if (obj != null) {
                    obj.findNext();
                }
            }
        };
        new MenuAction(FIND_PREV, false) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Editable obj = getEditableComponent();
                if (obj != null) {
                    obj.findPrevious();
                }
            }
        };
        new MenuAction(GOTO_POSITION) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Editable obj = getEditableComponent();
                if (obj != null) {
                    obj.gotoPosition();
                }
            }
        };
        new MenuAction(EDIT_SELECT_ALL, false) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Editable obj = getEditableComponent();
                if (obj != null) {
                    obj.selectAll();
                }
            }
        };

        new MenuAction(EDIT_JOIN_LINES, false) {
            @Override
            public void actionPerformed(ActionEvent e) {
                EditorTab tab = tabbedEditor.getActiveTab();
                if (tab != null) {
                    tab.getEditor().joinLines();
                }
            }
        };

        new MenuAction(EDIT_TO_LOWER, false) {
            @Override
            public void actionPerformed(ActionEvent e) {
                EditorTab tab = tabbedEditor.getActiveTab();
                if (tab != null) {
                    tab.getEditor().toLower();
                }
            }
        };
        new MenuAction(EDIT_TO_UPPER, false) {
            @Override
            public void actionPerformed(ActionEvent e) {
                EditorTab tab = tabbedEditor.getActiveTab();
                if (tab != null) {
                    tab.getEditor().toUpper();
                }
            }
        };
        new MenuAction(EDIT_TO_CAPITALIZED, false) {
            @Override
            public void actionPerformed(ActionEvent e) {
                EditorTab tab = tabbedEditor.getActiveTab();
                if (tab != null) {
                    tab.getEditor().toCapitalized();
                }
            }
        };
        new MenuAction(EDIT_TO_TITLED, false) {
            @Override
            public void actionPerformed(ActionEvent e) {
                EditorTab tab = tabbedEditor.getActiveTab();
                if (tab != null) {
                    tab.getEditor().toTitled();
                }
            }
        };


        new MenuAction(EDIT_SETTINGS) {
            @Override
            public void actionPerformed(ActionEvent e) {
                DialogFactory.openSettings(Viewer.this);
            }
        };
    }

    private void createViewActions() {
        final GUISnap guiSnap = GUISnap.getInstance();

        new MenuAction(VIEW_TOOL_BAR) {

            {
                putValue(SELECTED_KEY, guiSnap.isShowToolBar());
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                IToolBar toolBar = getToolBar();
                toolBar.setVisible(!toolBar.isVisible());
            }
        };

        new MenuAction(VIEW_STATUS_BAR) {
            {
                putValue(SELECTED_KEY, guiSnap.isShowStatusBar());
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                IStatusBar statusBar = getStatusBar();
                statusBar.setVisible(!statusBar.isVisible());
            }
        };

        new MenuAction(VIEW_SIDE_BAR) {
            {
                putValue(SELECTED_KEY, guiSnap.isShowSideBar());
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                setNavigateTreeVisible(!guiSnap.isShowSideBar());
            }
        };

    }

    private void createBookActions() {
        new MenuAction(NEW_CHAPTER) {
            @Override
            public void actionPerformed(ActionEvent e) {
                navigateTree.newChapter();
            }
        };
        new MenuAction(INSERT_CHAPTER) {
            @Override
            public void actionPerformed(ActionEvent e) {
                navigateTree.insertChapter();
            }
        };
        new MenuAction(IMPORT_CHAPTER) {
            @Override
            public void actionPerformed(ActionEvent e) {
                navigateTree.importChapter();
            }
        };
        new MenuAction(EXPORT_CHAPTER) {
            @Override
            public void actionPerformed(ActionEvent e) {
                navigateTree.exportChapter();
            }
        };
        new MenuAction(RENAME_CHAPTER) {
            @Override
            public void actionPerformed(ActionEvent e) {
                navigateTree.renameChapter();
            }
        };
        new MenuAction(JOIN_CHAPTER) {
            @Override
            public void actionPerformed(ActionEvent e) {
                navigateTree.joinChapter();
            }
        };
        new MenuAction(CHAPTER_PROPERTIES) {
            @Override
            public void actionPerformed(ActionEvent e) {
                navigateTree.editAttributes();
            }
        };
        new MenuAction(BOOK_EXTRA) {
            @Override
            public void actionPerformed(ActionEvent e) {
                navigateTree.editExtra();
            }
        };
    }

    private void createWindowActions() {
        new MenuAction(CLOSE_ACTIVE_TAB, false) {
            @Override
            public void actionPerformed(ActionEvent e) {
                tabbedEditor.closeActiveTab();
            }
        };
        new MenuAction(CLOSE_OTHER_TABS, false) {
            @Override
            public void actionPerformed(ActionEvent e) {
                tabbedEditor.closeOtherTabs();
            }
        };
        new MenuAction(CLOSE_ALL_TABS, false) {
            @Override
            public void actionPerformed(ActionEvent e) {
                tabbedEditor.clearTabs();
            }
        };
        new MenuAction(CLOSE_UNMODIFIED_TABS, false) {
            @Override
            public void actionPerformed(ActionEvent e) {
                tabbedEditor.closeUnmodifiedTabs();
            }
        };
        new MenuAction(SELECT_NEXT_TAB, false) {
            @Override
            public void actionPerformed(ActionEvent e) {
                tabbedEditor.nextTab();
            }
        };
        new MenuAction(SELECT_PREVIOUS_TAB, false) {
            @Override
            public void actionPerformed(ActionEvent e) {
                tabbedEditor.previousTab();
            }
        };
    }

    private void createHelpActions() {
        new MenuAction(SHOW_HELP) {
            @Override
            public void actionPerformed(ActionEvent e) {
                // open help message
            }
        };
        new MenuAction(ABOUT_APP) {
            @Override
            public void actionPerformed(ActionEvent e) {
                DialogFactory.aboutApp(Viewer.this);
            }
        };
    }

    private void createContentArea() {
        GUISnap guiSnap = GUISnap.getInstance();

        mSplitPane = new JSplitPane();
        mSplitPane.setDividerSize(guiSnap.getFrameDividerSize());
        mSplitPane.setDividerLocation(guiSnap.getFrameDividerLocation());
        mSplitPane.setBorder(BorderFactory.createEmptyBorder());

        navigateTree = new NavigateTree(this);
        mSplitPane.setLeftComponent(navigateTree);

        tabbedEditor = new TabbedEditor(this);
        mWelcomePane = new WelcomePane();

        setNavigateTreeVisible(guiSnap.isShowSideBar());

        // now, no editor tab opened, show welcome pane
        showWelcomePane();

        getContentPane().add(mSplitPane, BorderLayout.CENTER);
    }

    private void setNavigateTreeVisible(boolean visible) {
        GUISnap guiSnap = GUISnap.getInstance();
        guiSnap.setShowSideBar(visible);

        if (visible) {
            mSplitPane.setDividerSize(guiSnap.getFrameDividerSize());
            mSplitPane.setDividerLocation(guiSnap.getFrameDividerLocation());
        } else {    // hide the tree window
            guiSnap.setFrameDividerSize(mSplitPane.getDividerSize());
            guiSnap.setFrameDividerLocation(mSplitPane.getDividerLocation());
            mSplitPane.setDividerSize(0);
            mSplitPane.setDividerLocation(0);

            // focus to tabbed editor
            tabbedEditor.requestFocus();
        }

        navigateTree.setVisible(visible);
    }

    private Editable getEditableComponent() {
        switch (activeComponent) {
            case NavigateTree:
                return navigateTree;
            case TabbedEditor:
                EditorTab tab = tabbedEditor.getActiveTab();
                if (tab != null) {
                    return tab.getEditor();
                }
            default:
                return null;
        }
    }

    public void notifyActivated(Editable com) {
        if (com instanceof NavigateTree) {
            activeComponent = ActiveComponent.NavigateTree;
        } else if (com instanceof TextEditor) {
            activeComponent = ActiveComponent.TabbedEditor;
        }
    }

    public boolean isActive(Editable com) {
        if (com instanceof NavigateTree) {
            return activeComponent == ActiveComponent.NavigateTree;
        } else if (com instanceof TextEditor) {
            return activeComponent == ActiveComponent.TabbedEditor;
        }
        return false;
    }

    public void showTabbedEditor() {
        int size = mSplitPane.getDividerSize();
        int location = mSplitPane.getDividerLocation();
        mSplitPane.setRightComponent(tabbedEditor);
        mSplitPane.setDividerSize(size);
        mSplitPane.setDividerLocation(location);
        tabbedEditor.requestFocus();
    }

    public void showWelcomePane() {
        int size = mSplitPane.getDividerSize();
        int location = mSplitPane.getDividerLocation();
        mSplitPane.setRightComponent(mWelcomePane);
        mSplitPane.setDividerSize(size);
        mSplitPane.setDividerLocation(location);
    }

    /**
     * Initializes frame state and property.
     */
    private void initialize() {
        setIconImage(IResource.loadImage(app.getText("App.Icon")));

        GUISnap guiSnap = GUISnap.getInstance();

        setSize(guiSnap.getFrameSize());

        Point location = guiSnap.getFrameLocation();
        if (location == null) {
            setLocationByPlatform(true);
        } else {
            setLocation(location);
        }

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                app.exitApp();
            }
        });
    }

    public void destroy() {
        saveState();
        dispose();
    }

    private void saveState() {
        GUISnap guiSnap = GUISnap.getInstance();

        guiSnap.setFrameSize(getSize());
        guiSnap.setFrameLocation(getLocation());

        guiSnap.setShowToolBar(getToolBar().isVisible());
        guiSnap.setShowStatusBar(getStatusBar().isVisible());

        if (guiSnap.isShowSideBar()) {
            guiSnap.setFrameDividerSize(mSplitPane.getDividerSize());
            guiSnap.setFrameDividerLocation(mSplitPane.getDividerLocation());
        }

        tabbedEditor.destroy();
    }

    public void updateHistoryMenu() {
        JMenu menuFile = getJMenuBar().getMenu(0);
        JMenu menu = (JMenu) menuFile.getItem(UIDesign.HISTORY_MENU_INDEX);
        menu.removeAll();

        File currentFile = null;
        BookTask task = app.getActiveTask();
        if (task != null) {
            currentFile = task.getSource();
        }
        int count = 0;
        for (String path: FileHistory.iterator()) {
            File file = new File(path);
            if (file.equals(currentFile)) {
                continue;
            }
            Action action = new OpenHistoryAction(path);
            menu.add(IToolkit.createMenuItem(action, null, this));
            ++count;
        }

        Action action = getMenuAction(Imabw.CLEAR_HISTORY);
        if (count > 0) {
            menu.addSeparator();
            action.setEnabled(true);
        } else {
            action.setEnabled(false);
        }
        menu.add(IToolkit.createMenuItem(action, null, this));
    }

    public void setActionEnable(String actionId, boolean enable) {
        Action action = getMenuAction(actionId);
        if (action == null) {
            app.debug("no such menu action: {0}", actionId);
        } else {
            action.setEnabled(enable);
        }
    }

    public void invokeAction(String actionId) {
        Action action = getMenuAction(actionId);
        if (action == null) {
            app.debug("no such menu action: {0}", actionId);
        } else {
            ActionEvent e = new ActionEvent(
                    this,
                    ActionEvent.ACTION_PERFORMED,
                    (String) action.getValue(Action.ACTION_COMMAND_KEY));
            action.actionPerformed(e);
        }
    }

    /**
     * Updates state of all common edit actions.
     * @param enable <tt>true</tt> to enable those actions, otherwise <tt>false</tt>
     */
    public void updateEditActions(boolean enable) {
        for (String actionId: EDIT_ACTIONS) {
            setActionEnable(actionId, enable);
        }
    }

    public JMenu getToolsMenu() {
        return getJMenuBar().getMenu(4);
    }

    /**
     * Updates all UI components of viewer when changing L&F.
     */
    public void lafUpdated() {
        SwingUtilities.updateComponentTreeUI(this);
    }

    public NavigateTree getNavigateTree() {
        return navigateTree;
    }

    public TabbedEditor getTabbedEditor() {
        return tabbedEditor;
    }

    public StatusIndicator getStatusIndicator() {
        return mStatusIndicator;
    }

    public void setBook(Book book) {
        tabbedEditor.clearTabs(false);
        navigateTree.setBook(book);
        updateTitle();
    }

    public void updateTitle() {
        StringBuilder sb = new StringBuilder();

        BookTask task = app.getActiveTask();

        Book book = task.getBook();
        // name
        sb.append(book.stringAttribute(Book.TITLE));

        // modified
        if (task.isBookModified()) {
            sb.append("*");
        }
        sb.append(" - [");

        // author
        String author = book.stringAttribute(Book.AUTHOR, "");
        if (! author.isEmpty()) {
            sb.append(author);
        } else {
            sb.append(app.getText("Common.NoAuthor"));
        }
        sb.append("] - ");

        // file
        File file = task.getSource();
        if (file != null) {
            sb.append(file.getPath()).append(" - ");
        }

        // opened chapter
        EditorTab tab = tabbedEditor.getActiveTab();
        if (tab != null) {
            sb.append(tab.getChapter().stringAttribute(Book.TITLE)).append(" - ");
        }

        sb.append(app.getText("App.Name")).append(" ").append(Imabw.VERSION);
        setTitle(sb.toString());
    }

    static {
        IAction.LARGE_ICON_SUFFIX = "-large";
    }
    private abstract class MenuAction extends IAction {
        MenuAction(String key) {
            this(key, true);
        }

        MenuAction(String key, boolean enable) {
            super("Menu."+key);
            setEnabled(enable);
            Viewer.this.addMenuAction(key, this);
        }
    }

    private class OpenHistoryAction extends AbstractAction {
        private String path;

        OpenHistoryAction(String path) {
            this.path = path;
            putValue(NAME, path);
            putValue(SHORT_DESCRIPTION, app.getText("Menu.File.OpenHistory.Details", path));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (! app.maybeSaving(app.getText("Dialog.OpenBook.Title"))) {
                return;
            }
            app.openBook(new ParserData(new File(path)));
        }
    }
}
