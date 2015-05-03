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

package pw.phylame.ixin.frame;

import pw.phylame.ixin.com.IAction;
import pw.phylame.ixin.IToolkit;
import pw.phylame.ixin.com.IPaneRender;
import pw.phylame.ixin.event.IActionEvent;
import pw.phylame.ixin.event.IActionListener;
import pw.phylame.ixin.event.IStatusTipEvent;
import pw.phylame.ixin.event.IStatusTipListener;

import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.Map;
import java.util.HashMap;

/**
 * Simple frame model.
 */
public abstract class IFrame extends JFrame implements IStatusTipListener {

    public static void setActionsModel(Object[][][] actionsModel) {
        IFrame.actionsModel = actionsModel;
    }

    public static void setMenuBarModel(Object[][] menuBarModel) {
        IFrame.menuBarModel = menuBarModel;
    }

    public static void setToolBarModel(Object[] toolBarModel) {
        IFrame.toolBarModel = toolBarModel;
    }

    public IFrame() {
        super();
        initialized();
        initComp();
    }

    public IFrame(String title) {
        super(title);
        initialized();
        initComp();
    }

    protected void initialized() {

    }

    private void initComp() {
        Container topPane = getContentPane();
        createMenu();
        createToolBar();
        topPane.add(toolBar, BorderLayout.NORTH);
        contentArea = new JPanel(new BorderLayout());
        topPane.add(contentArea, BorderLayout.CENTER);
        createStatusBar();
        topPane.add(statusBar, BorderLayout.SOUTH);
    }

    private void createMenuActions() {
        IActionListener actionListener = new IActionListener() {
            @Override
            public void actionPerformed(IActionEvent e) {
                onAction(e.getAction().getId());
            }
        };
        if (actionsModel == null) {
            return;
        }
        for (Object[][] model: actionsModel) {
            menuActions.putAll(IToolkit.createActions(model, actionListener));
        }
        toolBarActions.putAll(menuActions);
    }

    private void createMenuBar() {
        if (getJMenuBar() != null) {    // already created
            return;
        }
        if (menuBarModel == null) {
            return;
        }
        JMenuBar menuBar = new JMenuBar();
        for (Object[] menuModel: menuBarModel) {
            JMenu menu = new JMenu();
            IToolkit.addMenuItem(menu, menuModel, menuActions, this);
            menuBar.add(menu);
        }
        setJMenuBar(menuBar);
    }

    private void createMenu() {
        createMenuActions();
        createMenuBar();
    }

    public Map<Object, IAction> getMenuActions() {
        return menuActions;
    }

    public IAction getMenuAction(Object id) {
        return menuActions.get(id);
    }

    public IAction getToolBarAction(Object id) {
        return toolBarActions.get(id);
    }

    private void createToolBar() {
        toolBar = new JToolBar();
        if (toolBarModel != null) {
            IToolkit.addButton(toolBar, java.util.Arrays.asList(toolBarModel), toolBarActions, this);
        }
        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        /* lock toolbar menu */
        toolBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (!e.isMetaDown()) {
                    return;
                }
                JPopupMenu popupMenu = new JPopupMenu();
                JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(getText("Frame.Toolbar.LockToolbar"));
                final boolean locked = !toolBar.isFloatable();
                menuItem.setState(locked);
                menuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        toolBar.setFloatable(locked);
                    }
                });
                popupMenu.add(menuItem);
                popupMenu.show(toolBar, e.getX(), e.getY());
            }
        });
    }

    private void createStatusBar() {
        statusLabel = new JLabel();
        statusBar = new JPanel(new BorderLayout());
        statusBar.add(new JSeparator(), BorderLayout.NORTH);
        statusBar.add(statusLabel, BorderLayout.WEST);
    }

    @Override
    public void showingTip(IStatusTipEvent e) {
        statusLabel.setText(" " + e.getStatusTip());
    }

    @Override
    public void closingTip(IStatusTipEvent e) {
        statusLabel.setText(" "+oldStatusText);
    }

    public abstract String getText(String key, Object... args);

    public abstract void onAction(Object actionID);

    public JToolBar getToolBar() {
        return toolBar;
    }

    public void showOrHideToolBar() {
        toolBar.setVisible(! toolBar.isVisible());
    }

    public IPaneRender getPaneRender() {
        return paneRender;
    }

    public void setPaneRender(IPaneRender paneRender) {
        this.paneRender = paneRender;
        contentArea.removeAll();
        if (paneRender != null) {
            contentArea.add(paneRender.getPane(), BorderLayout.CENTER);
        }
        contentArea.updateUI();
    }

    public JPanel getContentArea() {
        return contentArea;
    }

    public JPanel getStatusBar() {
        return statusBar;
    }

    public void showOrHideStatusBar() {
        statusBar.setVisible(! statusBar.isVisible());
    }

    public String getStatusText() {
        return statusLabel.getText();
    }

    public void setStatusText(String text) {
        oldStatusText = text;
        statusLabel.setText(" "+text);
    }


    // *****************
    // ** user data
    // *****************

    private static Object[][][] actionsModel = null;
    private static Object[][] menuBarModel = null;

    private static Object[] toolBarModel = null;

    private JToolBar toolBar;
    private JPanel contentArea;

    /** Content pane */
    private IPaneRender paneRender = null;

    private JPanel statusBar;
    private JLabel statusLabel;
    private String oldStatusText = null;

    /** Menu actions */
    private Map<Object, IAction> menuActions = new HashMap<>();

    /** Toolbar actions */
    private Map<Object, IAction> toolBarActions = new HashMap<>();

}
