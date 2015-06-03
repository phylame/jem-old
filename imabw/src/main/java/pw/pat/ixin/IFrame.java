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

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JMenu;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;
import javax.swing.JSeparator;
import javax.swing.JPopupMenu;
import javax.swing.JCheckBoxMenuItem;

import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Simple frame model.
 */
public abstract class IFrame extends JFrame implements ActionListener, IStatusTipListener, I18nSupport {
    public static final String SHOW_HIDE_TOOLBAR   = "show-hide-toolbar";
    public static final String LOCK_UNLOCK_TOOLBAR = "lock-unlock-toolbar";
    public static final String SHOW_HIDE_STATUSBAR = "show-hide-status-bar";

    public IFrame(String title) {
        this(title, null, null, null);
    }

    public IFrame(String title, Object[][][] actionModel, Object[][] menuBarModel, String[] toolBarModel) {
        super(title);

        initializing();

        createComponents(actionModel, menuBarModel, toolBarModel);
    }

    /**
     * Initialize frame before creating components.
     */
    protected void initializing() {
    }

    private void createComponents(Object[][][] actionModel, Object[][] menuBarModel, String[] toolBarModel) {
        Container topPane = getContentPane();

        createActions(actionModel);

        createMenuBar(menuBarModel);

        createToolBar(toolBarModel);
        topPane.add(toolBar, BorderLayout.NORTH);

        contentArea = new JPanel(new BorderLayout());
        topPane.add(contentArea, BorderLayout.CENTER);

        createStatusBar();
        topPane.add(statusBar, BorderLayout.SOUTH);
    }

    private void createActions(Object[][][] actionModel) {
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
        IAction action = IToolkit.createAction(command, key, this, this);
        addAction(action);
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

    private void createToolBar(String[] toolBarModel) {
        toolBar = new JToolBar();

        if (toolBarModel != null) {
            IToolkit.addButtons(toolBar, Arrays.asList(toolBarModel), actions, this);
        }

        setToolBarLocked(false);
        toolBar.setRollover(true);

        /* lock toolbar menu */
        IAction action = getAction(LOCK_UNLOCK_TOOLBAR);
        JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(action);
        menuItem.setSelected(isToolBarLocked());
        IToolkit.addStatusTipListener(menuItem, action, this);

        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(menuItem);
        toolBar.setComponentPopupMenu(popupMenu);
    }

    private void createStatusBar() {
        statusBar = new JPanel(new BorderLayout());
        statusBar.add(new JSeparator(), BorderLayout.NORTH);
        statusLabel = new JLabel();
        statusBar.add(statusLabel, BorderLayout.WEST);
    }

    public void addAction(IAction action) {
        actions.put(action.getCommand(), action);
    }

    public Map<String, IAction> getActions() {
        return actions;
    }

    public IAction getAction(String id) {
        return actions.get(id);
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

    public void showOrHideToolbar() {
        setToolBarVisible(! isToolBarVisible());
    }

    public boolean isToolBarLocked() {
        return ! toolBar.isFloatable();
    }

    public void setToolBarLocked(boolean locked) {
        toolBar.setFloatable(! locked);
    }

    public void lockOrUnlockToolbar() {
        setToolBarLocked(! isToolBarLocked());
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

    public void showOrHideStatusBar() {
        setStatusBarVisible(! isStatusBarVisible());
    }

    public String getStatusText() {
        return statusLabel.getText();
    }

    public void setStatusText(String text) {
        oldStatusText = text;
        statusLabel.setText(" "+text);
    }

    @Override
    public void showingTip(IStatusTipEvent e) {
        statusLabel.setText(" " + e.getStatusTip());
    }

    @Override
    public void closingTip(IStatusTipEvent e) {
        statusLabel.setText(" "+oldStatusText);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case SHOW_HIDE_TOOLBAR:
                showOrHideToolbar();
                break;
            case LOCK_UNLOCK_TOOLBAR:
                lockOrUnlockToolbar();
                break;
            case SHOW_HIDE_STATUSBAR:
                showOrHideStatusBar();
                break;
        }
    }

    /** Actions map */
    private Map<String, IAction> actions = new HashMap<>();

    private JToolBar toolBar;

    private JPanel   contentArea = null;

    private JPanel statusBar;
    private JLabel statusLabel;
    private String oldStatusText = null;
}
