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

package pw.phylame.imabw.app.ui;

import java.io.File;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

import pw.phylame.gaf.ixin.*;
import pw.phylame.jem.core.Book;
import pw.phylame.imabw.app.Imabw;
import pw.phylame.imabw.app.Constants;
import pw.phylame.imabw.app.model.BookTask;
import pw.phylame.imabw.app.model.FileHistory;
import pw.phylame.imabw.app.ui.com.WelcomePage;
import pw.phylame.imabw.app.ui.editor.EditorTab;
import pw.phylame.imabw.app.ui.editor.TabbedEditor;
import pw.phylame.imabw.app.ui.tree.ContentsTree;
import pw.phylame.imabw.app.ui.com.StatusIndicator;

/**
 * Main frame of Imabw.
 */
public class Viewer extends IForm implements Constants {
    private static final Imabw app = Imabw.sharedInstance();
    private JSplitPane splitPane;
    private WelcomePage welcomePage;
    private ContentsTree contentsTree;
    private TabbedEditor tabbedEditor;
    private StatusIndicator statusIndicator;

    private JComponent activeComponent;

    public Viewer() throws HeadlessException {
        super(app.getText("app.name"), UISnap.sharedInstance());
        initialize();
        updateHistory();
    }

    private void initialize() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                app.commandPerformed(Imabw.EXIT_APP);
            }
        });
        setIconImage(app.localizedImage("app.icon"));

        createCommonActions();
        createContentArea();
        createComponents(UIDesign.mainMenu, UIDesign.toolBar);

        restore();

        prepareToolBar();
        prepareStatusBar();
    }

    private void createCommonActions() {
        new ShowToolBarAction();
        new LockToolBarAction();
        new ShowStatusBarAction();
        new ShowSideBarAction();
        new EditableAction(EDIT_UNDO) {
            @Override
            protected void doEdit(Editable comp) {
                comp.undo();
            }
        };
        new EditableAction(EDIT_REDO) {
            @Override
            protected void doEdit(Editable comp) {
                comp.redo();
            }
        };
        new EditableAction(EDIT_CUT) {
            @Override
            protected void doEdit(Editable comp) {
                comp.cut();
            }
        };
        new EditableAction(EDIT_COPY) {
            @Override
            protected void doEdit(Editable comp) {
                comp.copy();
            }
        };
        new EditableAction(EDIT_PASTE) {
            @Override
            protected void doEdit(Editable comp) {
                comp.paste();
            }
        };
        new EditableAction(EDIT_DELETE) {
            @Override
            protected void doEdit(Editable comp) {
                comp.delete();
            }
        };
        new EditableAction(EDIT_SELECT_ALL) {
            @Override
            protected void doEdit(Editable comp) {
                comp.selectAll();
            }
        };
        new EditableAction(FIND_CONTENT) {
            @Override
            protected void doEdit(Editable comp) {
                comp.find();
            }
        };
        new EditableAction(FIND_NEXT) {
            @Override
            protected void doEdit(Editable comp) {
                comp.findNext();
            }
        };
        new EditableAction(FIND_PREVIOUS) {
            @Override
            protected void doEdit(Editable comp) {
                comp.findPrevious();
            }
        };
        new EditableAction(GOTO_POSITION) {
            @Override
            protected void doEdit(Editable comp) {
                comp.gotoPosition();
            }
        };
    }

    private void prepareToolBar() {
        IActionModel model = new IActionModel(LOCK_TOOL_BAR, IButtonType.Check);
        JToolBar toolBar = getToolBar();
        toolBar.setComponentPopupMenu(createPopupMenu(getTitle(), model));
        setActionSelected(SHOW_TOOL_BAR, toolBar.isVisible());
        setActionSelected(LOCK_TOOL_BAR, !toolBar.isFloatable());
    }

    private void prepareStatusBar() {
        IStatusBar statusBar = getStatusBar();
        statusBar.add(new JSeparator(), BorderLayout.PAGE_START);
        statusBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));
        statusIndicator = new StatusIndicator(this);
        statusBar.add(statusIndicator, BorderLayout.LINE_END);
        setActionSelected(SHOW_STATUS_BAR, statusBar.isVisible());
    }

    private void createContentArea() {
        splitPane = new JSplitPane();
        ISettings snap = getSnap();
        splitPane.setDividerSize(snap.getInteger(DIVIDER_SIZE, dividerSize));
        splitPane.setDividerLocation(snap.getInteger(DIVIDER_LOCATION, dividerLocation));

        welcomePage = new WelcomePage();
        contentsTree = new ContentsTree(this);
        tabbedEditor = new TabbedEditor(this);

        splitPane.setLeftComponent(contentsTree);
        setSideBarVisible(snap.getBoolean(SIDE_BAR_VISIBLE, true));
        setActionSelected(SHOW_SIDE_BAR, contentsTree.isVisible());

        showWelcomePage();

        getContentPane().add(splitPane, BorderLayout.CENTER);
    }

    public boolean isSideBarVisible() {
        return contentsTree.isVisible();
    }

    public void setSideBarVisible(boolean visible) {
        ISettings snap = getSnap();
        snap.setBoolean(SIDE_BAR_VISIBLE, visible);

        if (visible) {
            splitPane.setDividerSize(snap.getInteger(DIVIDER_SIZE, dividerSize));
            splitPane.setDividerLocation(snap.getInteger(DIVIDER_LOCATION, dividerLocation));
        } else {    // hide the tree window
            snap.setInteger(DIVIDER_SIZE, splitPane.getDividerSize());
            snap.setInteger(DIVIDER_LOCATION, splitPane.getDividerLocation());
            splitPane.setDividerSize(0);
            splitPane.setDividerLocation(0);
        }

        contentsTree.setVisible(visible);
    }

    public ContentsTree getContentsTree() {
        return contentsTree;
    }

    public TabbedEditor getTabbedEditor() {
        return tabbedEditor;
    }

    public StatusIndicator getStatusIndicator() {
        return statusIndicator;
    }

    public JComponent getActiveComponent() {
        return activeComponent;
    }

    public void setActiveComponent(JComponent activeComponent) {
        this.activeComponent = activeComponent;
    }

    private void setRightComponent(Component component) {
        if (component == splitPane.getRightComponent()) {
            return;
        }
        int size = splitPane.getDividerSize();
        int location = splitPane.getDividerLocation();
        splitPane.setRightComponent(component);
        splitPane.setDividerSize(size);
        splitPane.setDividerLocation(location);
    }

    public void showWelcomePage() {
        setRightComponent(welcomePage);
        setActiveComponent(contentsTree);
    }

    public void showTabbedEditor() {
        setRightComponent(tabbedEditor);
        setActiveComponent(tabbedEditor);
    }

    public JMenu getFileMenu() {
        return getJMenuBar().getMenu(UIDesign.FILE_MENU_INDEX);
    }

    public JMenu getEditMenu() {
        return getJMenuBar().getMenu(UIDesign.EDIT_MENU_INDEX);
    }

    public JMenu getViewMenu() {
        return getJMenuBar().getMenu(UIDesign.VIEW_MENU_INDEX);
    }

    public JMenu getBookMenu() {
        return getJMenuBar().getMenu(UIDesign.BOOK_MENU_INDEX);
    }

    public JMenu getToolsMenu() {
        return getJMenuBar().getMenu(UIDesign.TOOLS_MENU_INDEX);
    }

    public JMenu getWindowMenu() {
        return getJMenuBar().getMenu(UIDesign.WINDOW_MENU_INDEX);
    }

    public JMenu getHelpMenu() {
        return getJMenuBar().getMenu(UIDesign.HELP_MENU_INDEX);
    }

    // name[*] - [author] - [imported] - path - opened chapter - app name and version
    public void updateTitle() {
        StringBuilder sb = new StringBuilder();

        BookTask task = app.getManager().getActiveTask();

        Book book = task.getBook();

        sb.append(book.getTitle());
        // modified
        if (task.isBookModified()) {
            sb.append("*");
        }
        sb.append(" - ");

        // author
        String author = book.getAuthor();
        if (!author.isEmpty()) {
            sb.append("[").append(author).append("] - ");
        }

        // file
        File file = task.getSource();
        if (file != null) {
            // imported
            if (task.getFormat() == null) {
                sb.append("[").append(app.getText("viewer.title.imported"));
                sb.append("] - ");
            }
            sb.append(file.getPath()).append(" - ");
        }

        // opened chapter
        EditorTab tab = tabbedEditor.getActiveTab();
        if (tab != null) {
            sb.append(tab.getChapter().getTitle()).append(" - ");
        }

        sb.append(app.getText("app.name")).append(" ").append(VERSION);
        setTitle(sb.toString());
    }

    public void updateHistory() {
        JMenu menuFile = getFileMenu();
        JMenu menu = (JMenu) menuFile.getItem(UIDesign.HISTORY_MENU_INDEX);
        menu.removeAll();

        int count = 0;
        for (String path : FileHistory.histories()) {
            Action action = new OpenHistoryAction(path);
            menu.add(IxinUtilities.createMenuItem(action, null, this));
            ++count;
        }

        Action action = getMenuAction(Imabw.CLEAR_HISTORY);
        if (count > 0) {
            menu.addSeparator();
            action.setEnabled(true);
        } else {
            action.setEnabled(false);
        }
        menu.add(IxinUtilities.createMenuItem(action, null, this));
    }

    public void setBook(Book book) {
        tabbedEditor.clearTabs(false);
        contentsTree.setBook(book);
        updateTitle();
    }

    private static final String DIVIDER_SIZE = "form.divider.size";
    private static final String DIVIDER_LOCATION = "form.divider.location";
    private static final String SIDE_BAR_VISIBLE = "form.sidebar.visible";

    public static int dividerSize = 7;
    public static int dividerLocation = 171;

    private abstract class EditableAction extends MenuAction {
        EditableAction(String actionKey) {
            super(actionKey);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JComponent comp = getActiveComponent();
            if (comp instanceof Editable) {
                doEdit((Editable) comp);
            }
        }

        protected abstract void doEdit(Editable comp);
    }

    private class ShowToolBarAction extends MenuAction {
        ShowToolBarAction() {
            super(SHOW_TOOL_BAR);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setToolBarVisible(!isToolBarVisible());
        }
    }

    private class LockToolBarAction extends MenuAction {
        LockToolBarAction() {
            super(LOCK_TOOL_BAR);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setToolBarLocked(!isToolBarLocked());
        }
    }

    private class ShowStatusBarAction extends MenuAction {
        ShowStatusBarAction() {
            super(SHOW_STATUS_BAR);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setStatusBarVisible(!isStatusBarVisible());
        }
    }

    private class ShowSideBarAction extends MenuAction {
        ShowSideBarAction() {
            super(SHOW_SIDE_BAR);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setSideBarVisible(!isSideBarVisible());
        }
    }

    private class OpenHistoryAction extends AbstractAction {
        private String path;

        OpenHistoryAction(String path) {
            this.path = path;
            putValue(NAME, path);
            putValue(LONG_DESCRIPTION, app.getText("openHistory.details", path));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            app.getManager().openFile(new File(path));
        }
    }
}
