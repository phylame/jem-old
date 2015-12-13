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

import pw.phylame.imabw.app.Imabw;
import pw.phylame.imabw.app.config.EditorConfig;
import pw.phylame.imabw.app.ui.Viewer;
import pw.phylame.imabw.app.ui.dialog.DialogFactory;
import pw.phylame.imabw.app.ui.com.StatusIndicator;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;

/**
 * The text editor component.
 */
public class TextEditor extends JPanel {
    private static final Imabw app = Imabw.sharedInstance();

    private static Object[] POPUP_MENU_MODEL = {
            Imabw.EDIT_CUT, Imabw.EDIT_COPY, Imabw.EDIT_PASTE, null, Imabw.GOTO_POSITION
    };

    TextEditor(EditorTab owner, String text) {
        this.owner = owner;
        viewer = app.getForm();
        undoAction = viewer.getMenuAction(Imabw.EDIT_UNDO);
        redoAction = viewer.getMenuAction(Imabw.EDIT_REDO);
        undoManager = new UndoManager();
        createComponents(text);
        initialize();
        searchHelper = new SearchHelper(this);
    }

    private void createComponents(String text) {
        setLayout(new BorderLayout());
        textArea = new JTextArea(text);
        add(new JScrollPane(textArea), BorderLayout.CENTER);
    }

    private void initialize() {
        contextMenu = viewer.createPopupMenu(null, POPUP_MENU_MODEL);

        EditorConfig config = EditorConfig.sharedInstance();

        Font font = config.getFont();
        if (font != null) {
            textArea.setFont(font);
        }
        textArea.setBackground(config.getBackground());
        textArea.setForeground(config.getForeground());
        textArea.setWrapStyleWord(config.isWordWarp());
        textArea.setLineWrap(config.isLineWarp());

        Document doc = textArea.getDocument();
        doc.addUndoableEditListener(e -> {
            undoManager.addEdit(e.getEdit());
            updateUndoRedoActions();
        });

        doc.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!prohibitNotify) {
                    owner.textModified();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!prohibitNotify) {
                    owner.textModified();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        textArea.addCaretListener(e -> {
            updateCorrelatedActions();
            updateStatusIndicator();
        });

        // disable default popup menu if present
        textArea.setComponentPopupMenu(null);
        // set caret to the position where mouse is at when press mouse right key
        //      and show context menu
        textArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getClickCount() != 1) {
                    return;
                }
                if (!textArea.isFocusOwner()) {
                    textArea.requestFocus();
                }
                int position = textArea.viewToModel(e.getPoint());
                // if not in selection then update new caret
                if (position < textArea.getSelectionStart() || position > textArea.getSelectionEnd()) {
                    textArea.setCaretPosition(position);
                }
                if (SwingUtilities.isRightMouseButton(e)) {
                    // show context menu
                    contextMenu.show(textArea, e.getX(), e.getY());
                }
            }
        });

        textArea.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                viewer.setActiveComponent(viewer.getTabbedEditor());
                updateCorrelatedActions();
                updateStatusIndicator();
            }
        });

        // add some useful shortcut editor actions
        mapKeyboardAction(new ShowContextMenuAction());
        mapKeyboardAction(new GotoPrevLineAction());
        mapKeyboardAction(new GotoNextLineAction());
        mapKeyboardAction(new CutCurrentLineAction());
    }

    private void updateStatusIndicator() {
        StatusIndicator indicator = viewer.getStatusIndicator();
        indicator.setRuler(getCurrentRow() + 1, getCurrentColumn() + 1, getSelectionCount());
        indicator.setWords(textArea.getDocument().getLength());
        indicator.setReadonly(isReadonly());
    }

    private void updateCorrelatedActions() {
        // disable all common edit actions
        viewer.getTabbedEditor().updateEditActions(false);

        // disable all text editor actions
        viewer.getTabbedEditor().updateTextActions(false);

        viewer.setActionEnable(Imabw.EDIT_SELECT_ALL, true);

        if (!textArea.isEditable()) {
            // has text selection
            if (getSelectionCount() > 0) {
                // can copy
                viewer.setActionEnable(Imabw.EDIT_COPY, true);
            }
        } else {
            updateUndoRedoActions();

            // has text selection
            if (getSelectionCount() > 0) {
                viewer.setActionEnable(Imabw.EDIT_CUT, true);
                viewer.setActionEnable(Imabw.EDIT_COPY, true);

                viewer.setActionEnable(Imabw.TO_LOWER, true);
                viewer.setActionEnable(Imabw.TO_UPPER, true);
                viewer.setActionEnable(Imabw.TO_CAPITALIZED, true);
                viewer.setActionEnable(Imabw.TO_TITLED, true);
            }

            viewer.setActionEnable(Imabw.EDIT_PASTE, canPaste());
            viewer.setActionEnable(Imabw.EDIT_DELETE, true);

            viewer.setActionEnable(Imabw.JOIN_LINES, true);

            viewer.setActionEnable(Imabw.REPLACE_TEXT, true);
        }
    }

    private void updateUndoRedoActions() {
        redoAction.setEnabled(undoManager.canRedo());
        redoAction.putValue(Action.NAME, undoManager.getRedoPresentationName());

        undoAction.setEnabled(undoManager.canUndo());
        undoAction.putValue(Action.NAME, undoManager.getUndoOrRedoPresentationName());
    }

    @Override
    public void requestFocus() {
        textArea.requestFocus();
    }

    public JTextComponent getTextComponent() {
        return textArea;
    }

    public void mapKeyboardAction(Action action) {
        mapKeyboardAction((String) action.getValue(Action.ACTION_COMMAND_KEY),
                (KeyStroke) action.getValue(Action.ACCELERATOR_KEY), action);
    }

    public void mapKeyboardAction(String command, KeyStroke keyStroke, Action action) {
        textArea.getInputMap(WHEN_FOCUSED).put(keyStroke, command);
        textArea.getActionMap().put(command, action);
    }

    public int getCurrentRow() {
        int row = -1;
        try {
            row = textArea.getLineOfOffset(textArea.getCaretPosition());
        } catch (BadLocationException ex) {
            app.error("cannot get current row", ex);
        }
        return row;
    }

    public int getCurrentColumn() {
        int column = -1;
        try {
            int row = textArea.getLineOfOffset(textArea.getCaretPosition());
            column = textArea.getCaretPosition() - textArea.getLineStartOffset(row);
        } catch (BadLocationException ex) {
            app.error("cannot get current column", ex);
        }
        return column;
    }

    public int getSelectionCount() {
        return textArea.getSelectionEnd() - textArea.getSelectionStart();
    }

    public boolean isReadonly() {
        return !textArea.isEditable();
    }

    public void setReadonly(boolean enable) {
        textArea.setEditable(!enable);
        updateCorrelatedActions();
    }

    public Viewer getViewer() {
        return null;
    }

    public void undo() {
        if (undoManager.canUndo()) {
            prohibitNotify = true;
            undoManager.undo();
            prohibitNotify = false;
            app.getManager().getActiveTask().chapterTextModified(owner.getChapter(), false);
            app.getForm().getContentsTree().fireTextModified(owner.getChapter());
        }
    }

    public void redo() {
        if (undoManager.canRedo()) {
            undoManager.redo();
        }
    }

    private boolean canPaste() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable clipT = clipboard.getContents(null);
        return clipT.isDataFlavorSupported(DataFlavor.stringFlavor);
    }

    public void cut() {
        textArea.cut();
    }

    public void copy() {
        textArea.copy();
        viewer.setActionEnable(Imabw.EDIT_PASTE, canPaste());
    }

    public void paste() {
        textArea.paste();
    }

    public void delete() {
        if (getSelectionCount() > 0) {  // has selection, delete them
            textArea.replaceSelection("");
        } else {                        // delete next character
            Document doc = textArea.getDocument();
            if (textArea.getCaretPosition() < doc.getLength()) {
                try {
                    doc.remove(textArea.getCaretPosition(), 1);
                } catch (BadLocationException ex) {
                    app.error("cannot remove caret", ex);
                }
            }
        }
    }

    public void selectAll() {
        textArea.selectAll();
    }

    void showSearcher() {
        if (searchBar == null) {
            searchBar = new SearchBar(this);
            add(searchBar, BorderLayout.PAGE_START);
        }
        searchBar.setText(textArea.getSelectedText());
    }

    void hideSearcher() {
        if (searchBar != null) {
            remove(searchBar);
            updateUI();
            searchBar = null;
        }
    }

    public void find() {
        showSearcher();
        searchBar.hideReplacePane();
    }

    public void findNext() {
        searchHelper.findNext();
    }

    public void findPrevious() {
        searchHelper.findPrevious();
    }

    public void findReplace() {
        showSearcher();
        searchBar.showReplacePane();
    }

    void replaceNext() {
        searchHelper.replaceNext();
    }

    void replaceAll() {
        searchHelper.replaceAll();
    }

    public void gotoPosition() {
        String str = DialogFactory.inputText(viewer, app.getText("editors.gotoLine.title"),
                app.getText("editors.gotoLine.inputTip"),
                (getCurrentRow() + 1) + ":" + (getCurrentColumn() + 1), false, false);
        if (str == null) {
            return;
        }
        String[] parts = str.split(":", 2);
        int line, column = 0;
        try {
            line = Integer.valueOf(parts[0]) - 1;
        } catch (NumberFormatException e) {
            app.error("invalid line number: " + parts[0]);
            return;
        }

        if (parts.length == 2) {
            try {
                column = Integer.valueOf(parts[1]) - 1;
            } catch (NumberFormatException e) {
                app.error("invalid column number: " + parts[1]);
                return;
            }
        }

        int total = textArea.getLineCount();
        if (line >= total) {
            line = total - 1;
        }

        try {
            int position = textArea.getLineStartOffset(line) + column;
            textArea.setCaretPosition(position);
        } catch (BadLocationException e) {
            // ignored
            app.error("cannot go to: " + e.offsetRequested(), e);
        }
    }

    public void joinLines() {
        // todo: join lines
    }

    public void formatSelection(TextFormatter formatter) {
        int start = textArea.getSelectionStart(), end = textArea.getSelectionEnd();
        String text = textArea.getSelectedText();
        textArea.replaceSelection(formatter.format(text));
        textArea.setSelectionStart(start);
        textArea.setSelectionEnd(end);
    }

    private class GotoPrevLineAction extends AbstractAction {
        {
            putValue(ACTION_COMMAND_KEY, "goto-prev-line");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.CTRL_DOWN_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                int line = textArea.getLineOfOffset(textArea.getCaretPosition());
                if (line != 0) {
                    textArea.setCaretPosition(textArea.getLineStartOffset(line - 1));
                }
            } catch (BadLocationException ex) {
                app.error("cannot get row number", ex);
            }
        }
    }

    private class GotoNextLineAction extends AbstractAction {
        {
            putValue(ACTION_COMMAND_KEY, "goto-next-line");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.CTRL_DOWN_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                int line = textArea.getLineOfOffset(textArea.getCaretPosition());
                if (line != textArea.getLineCount() - 1) {
                    textArea.setCaretPosition(textArea.getLineStartOffset(line + 1));
                }
            } catch (BadLocationException ex) {
                app.error("cannot get row number", ex);
            }
        }
    }

    private class ShowContextMenuAction extends AbstractAction {
        {
            putValue(ACTION_COMMAND_KEY, "show-context-menu");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                Rectangle r = textArea.modelToView(textArea.getCaretPosition());
                contextMenu.show(textArea, (int) r.getX(), (int) r.getY());
            } catch (BadLocationException ex) {
                app.error("cannot get rectangle in cursor");
            }
        }
    }

    private class CutCurrentLineAction extends AbstractAction {
        {
            putValue(ACTION_COMMAND_KEY, "cut-current-line");
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, KeyEvent.SHIFT_DOWN_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String text;
            // has selection, delete them
            if (getSelectionCount() > 0) {
                text = textArea.getSelectedText();
                textArea.replaceSelection(null);
            } else {
                try {
                    int line = textArea.getLineOfOffset(textArea.getCaretPosition());
                    int start = textArea.getLineStartOffset(line), end = textArea.getLineEndOffset(line);
                    text = textArea.getText(start, end - start);
                    textArea.replaceRange(null, start, end);
                } catch (BadLocationException ex) {
                    app.error("cannot get row number", ex);
                    return;
                }
            }
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(text), null);
            viewer.setActionEnable(Imabw.EDIT_PASTE, canPaste());
        }
    }

    private Viewer viewer;
    private EditorTab owner;
    private JTextArea textArea;
    private UndoManager undoManager;
    private Action undoAction, redoAction;
    private JPopupMenu contextMenu;

    SearchHelper searchHelper;
    SearchBar searchBar = null;

    // prohibit notify chapter modified when text changed
    private boolean prohibitNotify = false;
}
