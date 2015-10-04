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

import pw.phylame.gaf.ixin.IToolkit;
import pw.phylame.jem.imabw.app.Imabw;
import pw.phylame.jem.imabw.app.config.EditorStyle;
import pw.phylame.jem.imabw.app.ui.Editable;
import pw.phylame.jem.imabw.app.ui.dialog.DialogFactory;
import pw.phylame.jem.imabw.app.ui.com.StatusIndicator;
import pw.phylame.jem.imabw.app.ui.Viewer;
import pw.phylame.util.StringUtils;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.text.ParseException;

/**
 * The text editor component.
 */
public class TextEditor extends JScrollPane implements Editable {
    private static Imabw app = Imabw.getInstance();

    private static Object[] POPUP_MENU_MODEL = {
            Imabw.EDIT_CUT, Imabw.EDIT_COPY, Imabw.EDIT_PASTE, null, Imabw.GOTO_POSITION
    };

    TextEditor(EditorTab owner, String text) {
        this.owner = owner;
        viewer = app.getActiveViewer();

        undoAction = viewer.getMenuAction(Imabw.EDIT_UNDO);
        redoAction = viewer.getMenuAction(Imabw.EDIT_REDO);

        undoManager = new UndoManager();

        textArea = new JTextArea(text);
        initialize();
        setViewportView(textArea);
    }

    private void initialize() {
        contextMenu = new JPopupMenu();
        IToolkit.addMenuItems(contextMenu, POPUP_MENU_MODEL, viewer.getMenuActions(), viewer);

        textArea.setBorder(BorderFactory.createEmptyBorder());

        EditorStyle config = EditorStyle.getInstance();

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
            updateActions();
            updateIndicator();
        });

        textArea.setComponentPopupMenu(null);
        // set caret to the position where mouse is at when press mouse right key
        //      and show context menu
        textArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getClickCount() == 1) {
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
            }
        });

        textArea.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                viewer.notifyActivated(TextEditor.this);
                updateActions();
                updateIndicator();
            }
        });

        // add some useful shortcut editor actions
        mapKeyboardAction(new ShowContextMenuAction());
        mapKeyboardAction(new GotoPrevLineAction());
        mapKeyboardAction(new GotoNextLineAction());
        mapKeyboardAction(new CutCurrentLineAction());
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

    private void updateIndicator() {
        StatusIndicator indicator = viewer.getStatusIndicator();
        indicator.setRuler(getCurrentRow() + 1, getCurrentColumn() + 1, getSelectionCount());
        indicator.setWords(textArea.getDocument().getLength());
        indicator.setReadonly(isReadonly());
    }

    private void updateActions() {
        // disable all common edit actions
        viewer.updateEditActions(false);

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

                viewer.setActionEnable(Imabw.EDIT_TO_LOWER, true);
                viewer.setActionEnable(Imabw.EDIT_TO_UPPER, true);
                viewer.setActionEnable(Imabw.EDIT_TO_CAPITALIZED, true);
                viewer.setActionEnable(Imabw.EDIT_TO_TITLED, true);
            }

            viewer.setActionEnable(Imabw.EDIT_PASTE, canPaste());
            viewer.setActionEnable(Imabw.EDIT_DELETE, true);

            viewer.setActionEnable(Imabw.EDIT_JOIN_LINES, true);

            viewer.setActionEnable(Imabw.REPLACE_TEXT, true);
        }
    }

    private void updateUndoRedoActions() {
        redoAction.setEnabled(undoManager.canRedo());
        redoAction.putValue(Action.NAME, undoManager.getRedoPresentationName());

        undoAction.setEnabled(undoManager.canUndo());
        undoAction.putValue(Action.NAME, undoManager.getUndoOrRedoPresentationName());
    }

    public int getCurrentRow() {
        int row = -1;
        try {
            row = textArea.getLineOfOffset(textArea.getCaretPosition());
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
        return row;
    }

    public int getCurrentColumn() {
        int column = -1;
        try {
            int row = textArea.getLineOfOffset(textArea.getCaretPosition());
            column = textArea.getCaretPosition() - textArea.getLineStartOffset(row);
        } catch (BadLocationException ex) {
            ex.printStackTrace();
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
        updateActions();
    }

    public void undo() {
        if (undoManager.canUndo()) {
            prohibitNotify = true;
            undoManager.undo();
            prohibitNotify = false;
            app.getActiveTask().chapterTextModified(owner.getChapter(), false);
            app.getActiveViewer().getNavigateTree().textModified(owner.getChapter());
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
                    ex.printStackTrace();
                }
            }
        }
    }

    public void selectAll() {
        textArea.selectAll();
    }

    @Override
    public void find() {

    }

    @Override
    public void findNext() {

    }

    @Override
    public void findPrevious() {

    }

    @Override
    public void gotoPosition() {
        String str = DialogFactory.inputText(viewer, app.getText("Editor.GotoLine.Title"),
                app.getText("Editor.GotoLine.InputTip"),
                (getCurrentRow() + 1) + ":" + (getCurrentColumn() + 1), false, false);
        if (str == null) {
            return;
        }
        String[] parts = str.split(":", 2);
        int line, column = 0;
        try {
            line = Integer.valueOf(parts[0]) - 1;
        } catch (NumberFormatException e) {
            app.debug("invalid line number: " + parts[0]);
            return;
        }

        if (parts.length == 2) {
            try {
                column = Integer.valueOf(parts[1]) - 1;
            } catch (NumberFormatException e) {
                app.debug("invalid column number: " + parts[1]);
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
            app.debug("cannot go to: " + e.offsetRequested(), e);
        }
    }

    public void joinLines() {
        // todo: join lines
    }

    /**
     * Converts text in selection to lower case.
     */
    public void toLower() {
        int start = textArea.getSelectionStart(), end = textArea.getSelectionEnd();
        String text = textArea.getSelectedText();
        textArea.replaceSelection(text.toLowerCase());
        textArea.setSelectionStart(start);
        textArea.setSelectionEnd(end);
    }

    /**
     * Converts text in selection to upper case.
     */
    public void toUpper() {
        int start = textArea.getSelectionStart(), end = textArea.getSelectionEnd();
        String text = textArea.getSelectedText();
        textArea.replaceSelection(text.toUpperCase());
        textArea.setSelectionStart(start);
        textArea.setSelectionEnd(end);
    }

    /**
     * Converts text in selection to capitalized.
     * <p>
     * Capitalized is that the first character is upper case.
     */
    public void toCapitalized() {
        int start = textArea.getSelectionStart(), end = textArea.getSelectionEnd();
        String text = textArea.getSelectedText();
        textArea.replaceSelection(StringUtils.toCapital(text));
        textArea.setSelectionStart(start);
        textArea.setSelectionEnd(end);
    }

    /**
     * Converts text in selection to titled.
     * <p>
     * Titled is that the first character of each words is upper case.
     */
    public void toTitled() {
        int start = textArea.getSelectionStart(), end = textArea.getSelectionEnd();
        String text = textArea.getSelectedText();
        textArea.replaceSelection(StringUtils.toTitle(text));
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
                ex.printStackTrace();
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
                ex.printStackTrace();
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
                ex.printStackTrace();
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
                    text = textArea.getText(start, end-start);
                    textArea.replaceRange(null, start, end);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
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

    // prohibit notify chapter modified when text changed
    private boolean prohibitNotify = false;
}
