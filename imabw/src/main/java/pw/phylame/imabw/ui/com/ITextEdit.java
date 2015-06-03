/*
 * Copyright 2014 Peng Wan <phylame@163.com>
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

package pw.phylame.imabw.ui.com;

import pw.pat.ixin.IAction;
import pw.pat.ixin.IToolkit;
import pw.pat.ixin.event.IStatusTipEvent;
import pw.pat.ixin.event.IStatusTipListener;
import pw.phylame.imabw.Imabw;

import javax.swing.*;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

/**
 * IxIn TextArea component.
 */
public class ITextEdit extends JScrollPane {
    /** Text editor */
    private JTextArea textArea = null;

    /** Undo manager */
    private UndoManager undoManager = null;

    public ITextEdit() {
        this(null, null, 0, 0, null);
    }

    public ITextEdit(IStatusTipListener tipListener) {
        this(null, null, 0, 0, tipListener);
    }

    public ITextEdit(String text, IStatusTipListener tipListener) {
        this(null, text, 0, 0, tipListener);
    }

    public ITextEdit(int rows, int columns, IStatusTipListener tipListener) {
        this(null, null, rows, columns, tipListener);
    }

    public ITextEdit(String text, int rows, int columns, IStatusTipListener tipListener) {
        this(null, text, rows, columns, tipListener);
    }

    public ITextEdit(Document doc, IStatusTipListener tipListener) {
        this(doc, null, 0, 0, tipListener);
    }

    public ITextEdit(Document doc, String text, int rows, int columns, IStatusTipListener tipListener) {
        super();
        textArea = new JTextArea(doc, text, rows, columns);
        undoManager = new UndoManager();

        /* add undo and redo action */
        IToolkit.addKeyboardAction(textArea, contextActions.get(UNDO), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        IToolkit.addKeyboardAction(textArea, contextActions.get(REDO), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        /* add context menu */
        addContextActions(this);

        setContextMenuTipListener(tipListener);

        /* register undo manager */
        textArea.getDocument().addUndoableEditListener(undoManager);

        /* set text area to scroll panel */
        setViewportView(textArea);
    }

    @Override
    public void requestFocus() {
        textArea.requestFocus();
    }

    public JTextArea getTextEditor() {
        return textArea;
    }

    // *******************
    // ** Edit operation
    // *******************
    public void addDocumentListener(DocumentListener listener) {
        textArea.getDocument().addDocumentListener(listener);
    }

    public void setText(String text) {
        textArea.setText(text);
    }

    public String getText() {
        return textArea.getText();
    }

    public boolean canUndo() {
        return undoManager.canUndo();
    }

    public void undo() {
        if (undoManager.canUndo()) {
            undoManager.undo();
        }
    }

    public boolean canRedo() {
        return undoManager.canRedo();
    }

    public void redo() {
        if (undoManager.canRedo()) {
            undoManager.redo();
        }
    }

    public boolean canCopy() {
        return getSelectionCount() != 0;
    }

    public void cut() {
        textArea.cut();
    }

    public void copy() {
        textArea.copy();
    }

    public boolean canPaste() {
        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable clipT = clip.getContents(null);
        return clipT.isDataFlavorSupported(DataFlavor.stringFlavor);
    }

    public void paste() {
        textArea.paste();
    }

    public void delete() {
        textArea.replaceSelection("");
    }

    public void selectAll() {
        textArea.selectAll();
    }


    // *********************
    // ** Caret operations
    // *********************
    public void addCaretListener(CaretListener listener) {
        textArea.addCaretListener(listener);
    }

    public int getSelectionCount() {
        return textArea.getSelectionEnd() - textArea.getSelectionStart();
    }

    public int getCurrentRow() {
        textArea.getLineCount();
        try {
            return textArea.getLineOfOffset(getCaretPosition());
        } catch (BadLocationException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int getCurrentColumn() {
        int column = -1;
        try {
            int row = textArea.getLineOfOffset(getCaretPosition());
            column = getCaretPosition() - textArea.getLineStartOffset(row);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        return column;
    }

    public int getLineCount() {
        return textArea.getLineCount();
    }

    public void gotoLine(int line) throws BadLocationException {
        setCaretPosition(textArea.getLineStartOffset(line));
    }

    public int getCaretPosition() {
        return textArea.getCaretPosition();
    }

    public void setCaretPosition(int pos) {
        textArea.setCaretPosition(pos);
    }

    public void read(Reader in, Object desc) throws IOException {
        textArea.read(in, desc);
    }

    public void write(Writer out) throws IOException {
        textArea.write(out);
    }

    /** Edit context menu */
    private static JPopupMenu contextMenu = null;

    /** Context menu actions */
    private static Map<String, IAction> contextActions = null;

    public static final String UNDO       = "undo";
    public static final String REDO       = "redo";
    public static final String CUT        = "cut";
    public static final String COPY       = "copy";
    public static final String PASTE      = "paste";
    public static final String DELETE     = "delete";
    public static final String SELECT_ALL = "select-all";

    private static Object[][] POPUP_MENU_ACTIONS = {
            {UNDO, "Editor.Menu.Undo"},
            {REDO, "Editor.Menu.Redo"},
            {CUT, "Editor.Menu.Cut"},
            {COPY, "Editor.Menu.Copy"},
            {PASTE, "Editor.Menu.Paste"},
            {DELETE, "Editor.Menu.Delete"},
            {SELECT_ALL, "Editor.Menu.SelectAll"},
    };

    private static final Object[] POPUP_MENU_MODEL = {
            UNDO, REDO,
            null,
            CUT, COPY, PASTE, DELETE,
            null,
            SELECT_ALL
    };

    // **********************************
    // ** Context menu tool tip listener
    // **********************************
    private static IStatusTipListener contextMenuTipListener = null;

    public static void setContextMenuTipListener(IStatusTipListener tipListener) {
        contextMenuTipListener = tipListener;
    }

    // ***********************
    // ** Current instance
    // ***********************
    private static ITextEdit currentInstance = null;

    /* create context menu */
    private static void createContextMenu() {
        if (contextMenu != null) {      // already created
            return;
        }

        /* send action to current editor */
        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentInstance == null) {
                    return;
                }
                switch (e.getActionCommand()) {
                    case UNDO:
                        currentInstance.undo();
                        break;
                    case REDO:
                        currentInstance.redo();
                        break;
                    case CUT:
                        currentInstance.cut();
                        break;
                    case COPY:
                        currentInstance.copy();
                        break;
                    case PASTE:
                        currentInstance.paste();
                        break;
                    case DELETE:
                        currentInstance.delete();
                        break;
                    case SELECT_ALL:
                        currentInstance.selectAll();
                        break;
                }
            }
        };

        IStatusTipListener tipListener = new IStatusTipListener() {
            @Override
            public void showingTip(IStatusTipEvent e) {
                if (contextMenuTipListener != null) {
                    contextMenuTipListener.showingTip(e);
                }
            }

            @Override
            public void closingTip(IStatusTipEvent e) {
                if (contextMenuTipListener != null) {
                    contextMenuTipListener.closingTip(e);
                }
            }
        };

        contextActions = IToolkit.createActions(POPUP_MENU_ACTIONS, actionListener, Imabw.getInstance());
        contextMenu = new JPopupMenu();
        IToolkit.addMenuItems(contextMenu, POPUP_MENU_MODEL, contextActions, tipListener);
    }

    /** Add context menu to {@code textEdit} */
    private static void addContextActions(final ITextEdit textEdit) {
        JTextArea textArea = textEdit.getTextEditor();
        /* context menu key */
        textArea.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateContextMenu(textEdit);
                try {
                    Rectangle rect = textEdit.getTextEditor().modelToView(textEdit.getTextEditor().getCaretPosition());
                    contextMenu.show(textEdit.getTextEditor(), (int) rect.getX(), (int) rect.getY());
                } catch (BadLocationException exp) {
                    exp.printStackTrace();
                }
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_CONTEXT_MENU, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        IAction action = getEditAction(UNDO);
        textArea.registerKeyboardAction(action, action.getAccelerator(), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        action = getEditAction(REDO);
        textArea.registerKeyboardAction(action, action.getAccelerator(), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        /* meta mouse */
        textArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (!e.isMetaDown()) {
                    return;
                }
                updateContextMenu(textEdit);
                contextMenu.show(textEdit.getTextEditor(), e.getX(), e.getY());
            }
        });
    }

    /** Set context menu status by {@code textEdit} */
    public static void updateContextMenu(ITextEdit textEdit) {
        currentInstance = textEdit;
        if (textEdit == null) {
            for (IAction action : contextActions.values()) {
                action.setEnabled(false);
            }
            return;
        }
        contextActions.get(UNDO).setEnabled(textEdit.canUndo());
        contextActions.get(REDO).setEnabled(textEdit.canRedo());
        contextActions.get(CUT).setEnabled(textEdit.canCopy());
        contextActions.get(COPY).setEnabled(textEdit.canCopy());
        contextActions.get(PASTE).setEnabled(textEdit.canPaste());
        contextActions.get(DELETE).setEnabled(textEdit.canCopy());
        contextActions.get(SELECT_ALL).setEnabled(true);
    }

    public static void updateContextMenu(ITextEdit textEdit, boolean hasSelection) {
        currentInstance = textEdit;
        contextActions.get(UNDO).setEnabled(textEdit.canUndo());
        contextActions.get(REDO).setEnabled(textEdit.canRedo());
        contextActions.get(CUT).setEnabled(hasSelection);
        contextActions.get(COPY).setEnabled(hasSelection);
        contextActions.get(PASTE).setEnabled(textEdit.canPaste());
        contextActions.get(DELETE).setEnabled(hasSelection);
        contextActions.get(SELECT_ALL).setEnabled(true);
    }

    public static Map<String, IAction> getContextActions() {
        return contextActions;
    }

    public static IAction getEditAction(String id) {
        IAction action = contextActions.get(id);
        if (currentInstance != null) {
            if (action != null) {
                switch ((String) action.getCommand()) {
                    case UNDO:
                        action.setEnabled(currentInstance.canUndo());
                        break;
                    case REDO:
                        action.setEnabled(currentInstance.canRedo());
                        break;
                    case PASTE:
                        action.setEnabled(currentInstance.canPaste());
                        break;
                    default:
                        action.setEnabled(currentInstance.canCopy());
                        break;
                }
            }
        }
        return action;
    }

    static {
        createContextMenu();
    }
}
