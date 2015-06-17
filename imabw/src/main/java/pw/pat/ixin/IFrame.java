/*
 * Copyright 2015 Peng Wan <phylame@163.com>
 *
 * This file is part of PAT Core.
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

package pw.pat.ixin;

import pw.pat.gaf.I18nSupport;
import pw.pat.ixin.event.IStatusTipEvent;
import pw.pat.ixin.event.IStatusTipListener;

import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JMenu;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;
import javax.swing.JSeparator;
import javax.swing.JPopupMenu;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Simple frame model.
 */
public abstract class IFrame extends JFrame implements ActionListener,
        IStatusTipListener, I18nSupport {
    public static final String SHOW_HIDE_TOOLBAR   = "show-hide-toolbar";
    public static final String LOCK_UNLOCK_TOOLBAR = "lock-unlock-toolbar";
    public static final String SHOW_HIDE_STATUSBAR = "show-hide-status-bar";

    public IFrame(String title) {
        this(title, null, null, null);
    }

    public IFrame(String title, Object[][][] actionModel, Object[][] menuBarModel,
                  String[] toolBarModel) {
        super(title);

        initializing();

        createComponents(actionModel, menuBarModel, toolBarModel);
    }

    /**
     * Initialize frame before creating components.
     * NOTICE: no menu bar, tool bar, status bar created.
     * Recommend: add menu actions or initialize JFrame
     */
    protected void initializing() {
    }

    private void createComponents(Object[][][] actionModel, Object[][] menuBarModel,
                                  String[] toolBarModel) {
        Container topPane = getContentPane();

        createMenuActions(actionModel);

        createMenuBar(menuBarModel);

        createToolPane(toolBarModel);
        topPane.add(toolBar, BorderLayout.NORTH);

        contentArea = new JPanel(new BorderLayout());
        topPane.add(contentArea, BorderLayout.CENTER);

        createStatusPane();
        topPane.add(statusBar, BorderLayout.SOUTH);
    }

    private void createMenuActions(Object[][][] actionModel) {
        if (actionModel == null) {
            return;
        }

        for (Object[][] model : actionModel) {
            actions.putAll(IToolkit.createActions(model, this, this));
        }

        // add inner actions
        addInnerAction(SHOW_HIDE_TOOLBAR, "Ixin.Frame.ShowToolbar");
        addInnerAction(LOCK_UNLOCK_TOOLBAR, "Ixin.Frame.LockToolbar");
        addInnerAction(SHOW_HIDE_STATUSBAR, "Ixin.Frame.ShowStatusbar");
    }

    private void addInnerAction(String command, String key) {
        addMenuAction(IToolkit.createAction(command, key, this, this));
    }

    private void createMenuBar(Object[][] menuBarModel) {
        if (menuBarModel == null) {
            return;
        }

        JMenuBar menuBar = new JMenuBar();
        for (Object[] menuModel: menuBarModel) {
            JMenu menu = new JMenu();
            IToolkit.addMenuItems(menu, menuModel, actions, this);
            menuBar.add(menu);
        }
        setJMenuBar(menuBar);
    }

    private void createToolPane(String[] toolBarModel) {
        toolBar = new JToolBar();

        if (toolBarModel != null) {
            IToolkit.addButtons(toolBar, Arrays.asList(toolBarModel), actions, this);
        }

        setToolBarLocked(false);
        toolBar.setRollover(true);

        /* lock toolbar menu */
        final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(
                getMenuAction(LOCK_UNLOCK_TOOLBAR));

        IToolkit.addStatusTipListener(menuItem,
                (String) menuItem.getAction().getValue(IAction.STATUS_TIP), this);

        JPopupMenu popupMenu = new JPopupMenu();

        popupMenu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                menuItem.setSelected(isToolBarLocked());    // update menu state
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });

        popupMenu.add(menuItem);
        toolBar.setComponentPopupMenu(popupMenu);
    }

    private void createStatusPane() {
        statusBar = new JPanel(new BorderLayout());
        statusBar.add(new JSeparator(), BorderLayout.NORTH);

        statusText = new JLabel();
        statusBar.add(statusText, BorderLayout.WEST);
    }

    public void addMenuAction(Action action) {
        actions.put((String) action.getValue(Action.ACTION_COMMAND_KEY), action);
    }

    public Action getMenuAction(String id) {
        return actions.get(id);
    }

    public Map<String, Action> getMenuActions() {
        return actions;
    }

    public void invokeMenuAction(String id, Object source) {
        Action action = getMenuAction(id);
        if (action == null) {
            throw new IllegalArgumentException("No such action: '" + id + "'");
        }
        action.actionPerformed(new ActionEvent(source, ActionEvent.ACTION_PERFORMED,
                (String) action.getValue(Action.ACTION_COMMAND_KEY)));
    }

    public JToolBar getToolBar() {
        return toolBar;
    }

    public boolean isToolBarVisible() {
        return toolBar.isVisible();
    }

    public void setToolBarVisible(boolean visible) {
        toolBar.setVisible(visible);
    }

    public boolean isToolBarLocked() {
        return ! toolBar.isFloatable();
    }

    public void setToolBarLocked(boolean locked) {
        toolBar.setFloatable(! locked);
    }

    public JPanel getContentArea() {
        return contentArea;
    }

    public void setContentArea(JPanel panel) {
        Container topPane = getContentPane();
        if (contentArea != null) {
            topPane.remove(contentArea);
        }
        topPane.add(panel, BorderLayout.CENTER);
        contentArea = panel;
    }

    public JPanel getStatusBar() {
        return statusBar;
    }

    public boolean isStatusBarVisible() {
        return statusBar.isVisible();
    }

    public void setStatusBarVisible(boolean visible) {
        statusBar.setVisible(visible);
    }

    public String getStatusText() {
        return statusText.getText();
    }

    public void setStatusText(String text) {
        oldStatusText = text;
        statusText.setText(" "+text);
    }

    @Override
    public void showingTip(IStatusTipEvent e) {
        statusText.setText(" " + e.getStatusTip());
    }

    @Override
    public void closingTip(IStatusTipEvent e) {
        statusText.setText(" "+oldStatusText);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case SHOW_HIDE_TOOLBAR:
                setToolBarVisible(!isToolBarVisible());
                break;
            case LOCK_UNLOCK_TOOLBAR:
                setToolBarLocked(!isToolBarLocked());
                break;
            case SHOW_HIDE_STATUSBAR:
                setStatusBarVisible(!isStatusBarVisible());
                break;
        }
    }

    /** Actions map */
    private Map<String, Action> actions = new HashMap<>();

    private JToolBar toolBar;

    private JPanel contentArea = null;

    private JPanel statusBar;
    private JLabel statusText;
    private String oldStatusText = null;
}
