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

package pw.phylame.jem.imabw.app.ui.editor;

import javax.swing.*;

import pw.phylame.gaf.ixin.IResource;
import pw.phylame.gaf.ixin.IToolkit;
import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.imabw.app.Imabw;
import pw.phylame.jem.imabw.app.config.GUISnap;
import pw.phylame.jem.imabw.app.ui.Viewer;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;

public class TabbedEditor extends JPanel {
    private static Object[] CONTEXT_MENU_MODEL = {
            Imabw.CLOSE_ACTIVE_TAB, Imabw.CLOSE_OTHER_TABS, Imabw.CLOSE_ALL_TABS,
            Imabw.CLOSE_UNMODIFIED_TABS,
            null,
            Imabw.SELECT_NEXT_TAB, Imabw.SELECT_PREVIOUS_TAB
    };

    public TabbedEditor(Viewer viewer) {
        super(new BorderLayout());
        this.viewer = viewer;
        createComponents();
        initialize();
        addTabListener(new MyTabListener());
    }

    private void createComponents() {
        tabbedPane = new MyTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);
    }

    private void initialize() {
        GUISnap guiSnap = GUISnap.getInstance();
        tabbedPane.setTabPlacement(guiSnap.getFrameTabPlacement());
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        JPopupMenu popupMenu = new JPopupMenu();
        IToolkit.addMenuItems(popupMenu, CONTEXT_MENU_MODEL,
                viewer.getMenuActions(), viewer);
        tabbedPane.setComponentPopupMenu(popupMenu);

        tabbedPane.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                EditorTab tab = getActiveTab();
                if (tab != null) {
                    fireTabActivated(tab);
                }
            }
        });
    }

    /**
     * Updates state of all default tab actions
     *
     * @param enable <tt>true</tt> to enable those actions, otherwise <tt>false</tt>
     */
    private void updateTabActions(boolean enable) {
        for (String actionId : Imabw.TAB_ACTIONS) {
            viewer.setActionEnable(actionId, enable);
        }
    }

    /**
     * Updates state of all default text editor actions.
     *
     * @param enable <tt>true</tt> to enable those actions, otherwise <tt>false</tt>
     */
    public void updateTextActions(boolean enable) {
        for (String actionId : Imabw.TEXT_ACTIONS) {
            viewer.setActionEnable(actionId, enable);
        }
    }

    public void destroy() {
        GUISnap guiSnap = GUISnap.getInstance();
        guiSnap.setFrameTabPlacement(tabbedPane.getTabPlacement());
    }

    public Viewer getOwner() {
        return viewer;
    }

    // *********************************** //
    // ** Tab event listener management ** //
    // *********************************** //

    public void addTabListener(TabListener listener) {
        listenerList.add(TabListener.class, listener);
    }

    public void removeTabListener(TabListener listener) {
        listenerList.remove(TabListener.class, listener);
    }

    public TabListener[] getTabListeners() {
        return listenerList.getListeners(TabListener.class);
    }

    private void fireTabCreated(EditorTab tab) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TabListener.class) {
                TabEvent e = new TabEvent(TabbedEditor.this, tab);
                ((TabListener) listeners[i + 1]).tabCreated(e);
            }
        }
    }

    private void fireTabActivated(EditorTab tab) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TabListener.class) {
                TabEvent e = new TabEvent(TabbedEditor.this, tab);
                ((TabListener) listeners[i + 1]).tabActivated(e);
            }
        }
    }

    private void fireTabInactivated(EditorTab tab) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TabListener.class) {
                TabEvent e = new TabEvent(TabbedEditor.this, tab);
                ((TabListener) listeners[i + 1]).tavInactivated(e);
            }
        }
    }

    private void fireTabClosed(EditorTab tab) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TabListener.class) {
                TabEvent e = new TabEvent(TabbedEditor.this, tab);
                ((TabListener) listeners[i + 1]).tabClosed(e);
            }
        }
    }

    // ******************** //
    // ** Tab operations ** //
    // ******************** //

    public EditorTab newTab(Chapter chapter, String tip) {
        Icon icon = IResource.loadIcon("tree/chapter.png");
        return newTab(chapter, icon, tip);
    }

    public EditorTab newTab(Chapter chapter, Icon icon, String tip) {
        EditorTab tab = new EditorTab(chapter);
        editorTabs.add(tab);

        String title = chapter.getTitle();
        // NOTE: if there is no tab existed, setSelectedIndex will be invoked.
        tabbedPane.addTab(title, icon, tab.getEditor(), tip);

        // tab component
        TabComponent titleBar = new TabComponent(icon, title, tab, this);
        tabbedPane.setTabComponentAt(getTabCount() - 1, titleBar);

        fireTabCreated(tab);

        return tab;
    }

    public int getTabCount() {
        return tabbedPane.getTabCount();
    }

    public void activateTab(EditorTab tab) {
        if (tab == null) {
            throw new NullPointerException();
        }
        tabbedPane.setSelectedComponent(tab.getEditor());
    }

    public void activateTab(int index) {
        tabbedPane.setSelectedIndex(index);
    }

    public boolean isTabActive(EditorTab tab) {
        if (tab == null) {
            throw new NullPointerException("tab");
        }
        return tab == getActiveTab();
    }

    public boolean isTabActive(Chapter chapter) {
        EditorTab tab = findTab(chapter);
        return tab != null && isTabActive(tab);
    }

    public EditorTab getActiveTab() {
        int index = tabbedPane.getSelectedIndex();
        if (index != -1) {
            return editorTabs.get(index);
        } else {
            return null;
        }
    }

    public void nextTab() {
        int index = tabbedPane.getSelectedIndex(), count = getTabCount();
        if (count < 2 || index == -1) {
            return;
        }

        if (index == count - 1) {
            index = 0;
        } else {
            ++index;
        }

        activateTab(index);
    }

    public void previousTab() {
        int index = tabbedPane.getSelectedIndex(), count = getTabCount();
        if (count < 2 || index == -1) {
            return;
        }

        if (index == 0) {
            index = count - 1;
        } else {
            --index;
        }

        activateTab(index);
    }

    private int indexOfChapter(Chapter chapter) {
        int index = 0;
        for (EditorTab tab : editorTabs) {
            if (chapter.equals(tab.getChapter())) {
                return index;
            }
            ++index;
        }

        return -1;
    }

    public EditorTab findTab(Chapter chapter) {
        int index = indexOfChapter(chapter);
        if (index != -1) {
            return editorTabs.get(index);
        }
        return null;
    }

    public EditorTab getTab(int index) {
        return editorTabs.get(index);
    }

    public void updateTabTitle(Chapter chapter) {
        int index = indexOfChapter(chapter);
        if (index < 0) {    // not opening
            return;
        }
        String title = chapter.stringAttribute(Chapter.TITLE);
        ((TabComponent) tabbedPane.getTabComponentAt(index)).setTitle(title);
    }

    public void closeActiveTab() {
        EditorTab tab = getActiveTab();
        if (tab != null) {
            closeTab(tab);
        }
    }

    public void closeTab(EditorTab tab) {
        if (tab == null) {
            throw new NullPointerException("tab");
        }
        tabbedPane.remove(tab.getEditor());
    }

    public void closeTab(Chapter chapter) {
        int index = indexOfChapter(chapter);
        if (index != -1) {
            closeTab(index);
        }
    }

    public void closeTab(int index) {
        tabbedPane.removeTabAt(index);
    }

    public void cacheAllTabs() {
        editorTabs.forEach(EditorTab::cacheIfNeed);
    }

    public void clearTabs() {
        clearTabs(true);
    }

    public void clearTabs(boolean cacheText) {
        if (cacheText) {
            cacheAllTabs();
        }
        tabbedPane.removeAll();
        editorTabs.clear();
    }

    public void closeOtherTabs() {
        EditorTab currentTab = getActiveTab();
        if (currentTab == null) {
            return;
        }

        // make a copy of list
        for (EditorTab tab : editorTabs.toArray(new EditorTab[0])) {
            if (currentTab != tab) {
                closeTab(tab);
            }
        }
    }

    public void closeUnmodifiedTabs() {
        // make a copy of list to prohibit ConcurrentModificationException
        for (EditorTab tab : editorTabs.toArray(new EditorTab[0])) {
            if (!tab.isModified()) {
                closeTab(tab);
            }
        }
    }

    private class MyTabbedPane extends JTabbedPane {
        @Override
        public void setSelectedIndex(int index) {
            int oldIndex = getSelectedIndex();
            super.setSelectedIndex(index);

            if (oldIndex == -1) {
                // firstly add new tab, we ignore this event
                return;
            }

            if (oldIndex != index) {
                fireTabInactivated(editorTabs.get(oldIndex));
            }

            fireTabActivated(editorTabs.get(index));
        }

        @Override
        public void removeTabAt(int index) {
            super.removeTabAt(index);
            EditorTab tab = editorTabs.get(index);
            editorTabs.remove(index);
            fireTabClosed(tab);
        }
    }

    private class MyTabListener implements TabListener {
        @Override
        public void tabCreated(TabEvent e) {
            switch (getTabCount()) {
                case 1:             // firstly create tab
                    viewer.showTabbedEditor();
                    updateTabActions(true);
                    viewer.getStatusIndicator().setEditorStatus(true);
                    viewer.setActionEnable(Imabw.CLOSE_OTHER_TABS, false);
                    viewer.setActionEnable(Imabw.SELECT_NEXT_TAB, false);
                    viewer.setActionEnable(Imabw.SELECT_PREVIOUS_TAB, false);
                    break;
                case 2:             // secondly create tab
                    viewer.setActionEnable(Imabw.CLOSE_OTHER_TABS, true);
                    viewer.setActionEnable(Imabw.SELECT_NEXT_TAB, true);
                    viewer.setActionEnable(Imabw.SELECT_PREVIOUS_TAB, true);
                    break;
            }
        }

        @Override
        public void tabActivated(TabEvent e) {
            viewer.updateTitle();

            // set focus to text area
            e.getTab().getEditor().requestFocus();
        }

        @Override
        public void tavInactivated(TabEvent e) {
            // do nothing
        }

        private void cacheTab(EditorTab tab) {
            // not modified
            new Thread(tab::cacheIfNeed).start();
        }

        @Override
        public void tabClosed(TabEvent e) {
            cacheTab(e.getTab());
            switch (getTabCount()) {
                case 0:             // closed all tabs
                    updateTabActions(false);
                    viewer.updateTitle();
                    viewer.showWelcomePane();
                    viewer.getStatusIndicator().setEditorStatus(false);
                    break;
                case 1:             // exists one tab
                    viewer.setActionEnable(Imabw.CLOSE_OTHER_TABS, false);
                    viewer.setActionEnable(Imabw.SELECT_NEXT_TAB, false);
                    viewer.setActionEnable(Imabw.SELECT_PREVIOUS_TAB, false);
                    break;
            }
        }
    }

    private Viewer viewer;
    public JTabbedPane tabbedPane;
    private ArrayList<EditorTab> editorTabs = new ArrayList<>();
}
