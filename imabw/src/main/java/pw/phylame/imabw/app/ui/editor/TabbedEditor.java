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

package pw.phylame.imabw.app.ui.editor;

import javax.swing.*;

import pw.phylame.imabw.app.Imabw;
import pw.phylame.imabw.app.config.EditorConfig;
import pw.phylame.imabw.app.ui.Editable;
import pw.phylame.jem.core.Chapter;
import pw.phylame.imabw.app.ui.Viewer;
import pw.phylame.jem.formats.util.text.TextUtils;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;

/**
 * Tabbed editor for chapter content editing.
 */
public class TabbedEditor extends JPanel implements Editable {
    private static final Object[] CONTEXT_MENU_MODEL = {
            Imabw.CLOSE_ACTIVE_TAB, Imabw.CLOSE_OTHER_TABS,
            Imabw.CLOSE_ALL_TABS, Imabw.CLOSE_UNMODIFIED_TABS,
            null,
            Imabw.GOTO_NEXT_TAB, Imabw.GOTO_PREVIOUS_TAB
    };

    private Viewer viewer;
    private JTabbedPane tabbedPane;
    private ArrayList<EditorTab> editorTabs = new ArrayList<>();

    public TabbedEditor(Viewer viewer) {
        super(new BorderLayout());
        this.viewer = viewer;
        add(tabbedPane = new MyTabbedPane(), BorderLayout.CENTER);
        createTabActions();
        createTextActions();
        initialize();
        addTabListener(new MyTabListener());
        updateTabActions(false);
        updateTextActions(false);
    }

    private void initialize() {
        tabbedPane.setComponentPopupMenu(viewer.createPopupMenu(null, CONTEXT_MENU_MODEL));

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

    private void createTabActions() {
        viewer.new MenuAction(Imabw.GOTO_NEXT_TAB) {
            @Override
            public void actionPerformed(ActionEvent e) {
                nextTab();
            }
        };
        viewer.new MenuAction(Imabw.GOTO_PREVIOUS_TAB) {
            @Override
            public void actionPerformed(ActionEvent e) {
                previousTab();
            }
        };
        viewer.new MenuAction(Imabw.CLOSE_ACTIVE_TAB) {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeActiveTab();
            }
        };
        viewer.new MenuAction(Imabw.CLOSE_OTHER_TABS) {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeOtherTabs();
            }
        };
        viewer.new MenuAction(Imabw.CLOSE_ALL_TABS) {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearTabs();
            }
        };
        viewer.new MenuAction(Imabw.CLOSE_UNMODIFIED_TABS) {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeUnmodifiedTabs();
            }
        };
    }

    private void createTextActions() {
        viewer.new MenuAction(Imabw.REPLACE_TEXT) {
            @Override
            public void actionPerformed(ActionEvent e) {
                getActiveTab().getEditor().findReplace();
            }
        };
        viewer.new MenuAction(Imabw.TO_LOWER) {
            @Override
            public void actionPerformed(ActionEvent e) {
                getActiveTab().getEditor().formatSelection(String::toLowerCase);
            }
        };
        viewer.new MenuAction(Imabw.TO_UPPER) {
            @Override
            public void actionPerformed(ActionEvent e) {
                getActiveTab().getEditor().formatSelection(String::toUpperCase);
            }
        };
        viewer.new MenuAction(Imabw.TO_TITLED) {
            @Override
            public void actionPerformed(ActionEvent e) {
                getActiveTab().getEditor().formatSelection(TextUtils::titled);
            }
        };
        viewer.new MenuAction(Imabw.TO_CAPITALIZED) {
            @Override
            public void actionPerformed(ActionEvent e) {
                getActiveTab().getEditor().formatSelection(TextUtils::capitalized);
            }
        };
        viewer.new MenuAction(Imabw.JOIN_LINES) {
            @Override
            public void actionPerformed(ActionEvent e) {
                getActiveTab().getEditor().joinLines();
            }
        };
    }

    public void updateTabActions(boolean enable) {
        for (String command : Imabw.TAB_COMMANDS) {
            viewer.setActionEnable(command, enable);
        }
    }

    public void updateTextActions(boolean enable) {
        for (String command : Imabw.TEXT_COMMANDS) {
            viewer.setActionEnable(command, enable);
        }
    }

    @Override
    public Viewer getViewer() {
        return viewer;
    }

    @Override
    public void undo() {
        getActiveTab().getEditor().undo();
    }

    @Override
    public void redo() {
        getActiveTab().getEditor().redo();
    }

    @Override
    public void cut() {
        getActiveTab().getEditor().cut();
    }

    @Override
    public void copy() {
        getActiveTab().getEditor().copy();
    }

    @Override
    public void paste() {
        getActiveTab().getEditor().paste();
    }

    @Override
    public void delete() {
        getActiveTab().getEditor().delete();
    }

    @Override
    public void selectAll() {
        getActiveTab().getEditor().selectAll();
    }

    @Override
    public void find() {
        getActiveTab().getEditor().find();
    }

    @Override
    public void findNext() {
        getActiveTab().getEditor().findNext();
    }

    @Override
    public void findPrevious() {
        getActiveTab().getEditor().findPrevious();
    }

    @Override
    public void gotoPosition() {
        getActiveTab().getEditor().gotoPosition();
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
        return newTab(chapter, viewer.getContentsTree().getChapterIcon(), tip);
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
            throw new NullPointerException();
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
        ((TabComponent) tabbedPane.getTabComponentAt(index)).setTitle(chapter.getTitle());
        tabbedPane.setToolTipTextAt(index, viewer.getContentsTree().generateChapterTip(chapter));
    }

    public void closeActiveTab() {
        EditorTab tab = getActiveTab();
        if (tab != null) {
            closeTab(tab);
        }
    }

    public void closeTab(EditorTab tab) {
        if (tab == null) {
            throw new NullPointerException();
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
        for (EditorTab tab : editorTabs.toArray(new EditorTab[editorTabs.size()])) {
            if (currentTab != tab) {
                closeTab(tab);
            }
        }
    }

    public void closeUnmodifiedTabs() {
        // make a copy of list to prohibit ConcurrentModificationException
        for (EditorTab tab : editorTabs.toArray(new EditorTab[editorTabs.size()])) {
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
                    EditorConfig config = EditorConfig.sharedInstance();
                    tabbedPane.setTabPlacement(config.getTabPlacement());
                    tabbedPane.setTabLayoutPolicy(config.getTabLayout());

                    updateTabActions(true);
                    viewer.getStatusIndicator().setEditorStatus(true);
                    viewer.setActionEnable(Imabw.CLOSE_OTHER_TABS, false);
                    viewer.setActionEnable(Imabw.GOTO_NEXT_TAB, false);
                    viewer.setActionEnable(Imabw.GOTO_PREVIOUS_TAB, false);
                    break;
                case 2:             // secondly create tab
                    viewer.setActionEnable(Imabw.CLOSE_OTHER_TABS, true);
                    viewer.setActionEnable(Imabw.GOTO_NEXT_TAB, true);
                    viewer.setActionEnable(Imabw.GOTO_PREVIOUS_TAB, true);
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
                    updateTextActions(false);
                    viewer.updateTitle();
                    viewer.showWelcomePage();
                    viewer.getStatusIndicator().setEditorStatus(false);
                    break;
                case 1:             // exists one tab
                    viewer.setActionEnable(Imabw.CLOSE_OTHER_TABS, false);
                    viewer.setActionEnable(Imabw.GOTO_NEXT_TAB, false);
                    viewer.setActionEnable(Imabw.GOTO_PREVIOUS_TAB, false);
                    break;
            }
        }
    }
}
