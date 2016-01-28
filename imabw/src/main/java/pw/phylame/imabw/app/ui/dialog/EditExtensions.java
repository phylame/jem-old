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

package pw.phylame.imabw.app.ui.dialog;

import pw.phylame.gaf.ixin.IAction;
import pw.phylame.imabw.app.Imabw;
import pw.phylame.imabw.app.ui.UISnap;
import pw.phylame.imabw.app.util.BookUtils;
import pw.phylame.imabw.app.ui.com.ItemTable;

import static pw.phylame.imabw.app.ui.dialog.DialogFactory.*;

import pw.phylame.jem.core.Book;
import pw.phylame.jem.formats.util.FileInfo;
import pw.phylame.jem.formats.util.text.TextUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

class EditExtensions extends CommonDialog {
    private static final Imabw app = Imabw.sharedInstance();

    private static final String DIALOG_SIZE = "extensions.size";

    public static final HashSet<String> ignoredKeys = new HashSet<>();

    static {
        Collections.addAll(ignoredKeys, FileInfo.FILE_INFO);
    }

    private Book book;
    private Map<String, Object> extensions = new HashMap<>();
    private boolean modified = false;

    private ExtensionsTable table;
    private JButton btnSave;

    public EditExtensions(Dialog owner, String title, boolean modal) {
        super(owner, title, modal);
    }

    public EditExtensions(Frame owner, String title, boolean modal) {
        super(owner, title, modal);
    }

    void setBook(Book book) {
        this.book = book;
    }

    @Override
    protected void createComponents(JPanel userPane) {
        controlsPane = createButtonPane();
        table = new ExtensionsTable();
        userPane.add(table, BorderLayout.CENTER);

        setPreferredSize(UISnap.sharedInstance().getDimension(DIALOG_SIZE, new Dimension(514, 372)));
    }

    @Override
    protected void initialize(boolean resizable) {
        super.initialize(resizable);
        reset();
    }

    private JPanel createButtonPane() {
        JButton btnReset = new JButton(new IAction("d.extensions.buttonReset") {
            @Override
            public void actionPerformed(ActionEvent e) {
                reset();
            }
        });

        btnSave = new JButton(new IAction("d.extensions.buttonSave") {
            @Override
            public void actionPerformed(ActionEvent e) {
                onOk();
            }
        });
        btnSave.setEnabled(false);

        defaultButton = createCloseButton("d.extensions.buttonCancel");
        return createControlsPane(SwingConstants.RIGHT, btnReset, btnSave, defaultButton);
    }

    private void fireModified() {
        modified = true;
        btnSave.setEnabled(true);
    }

    private void reset() {
        extensions = BookUtils.dumpExtensions(book);
        table.resetAll(extensions, ignoredKeys);
        btnSave.setEnabled(false);
    }

    private void onOk() {
        destroy();
        if (modified) {
            syncToBook();
        }
    }

    @Override
    protected void onCancel() {
        if (modified) {
            int option = localizedAsking(this, getTitle(),
                    MessageDialog.IconStyle.Question,
                    "d.extensions.askQuit", book);
            switch (option) {
                case OPTION_OK:
                    onOk();
                    break;
                case OPTION_DISCARD:
                    break;
                default:
                    return;
            }
        }
        modified = false;
        destroy();
    }

    private void destroy() {
        dispose();
        UISnap.sharedInstance().setDimension(DIALOG_SIZE, getSize());
    }

    private void syncToBook() {
        for (Map.Entry<String, Object> entry : extensions.entrySet()) {
            book.setExtension(entry.getKey(), entry.getValue());
        }
//        app.getForm().getContentsTree().updateChapterAttributes(chapter, attributes, true,
//                app.getText("undo.message.editAttributes"));
        app.localizedMessage("d.extensions.result", book);
    }

    private class ExtensionsTable extends ItemTable {
        private ExtensionsTable() {
            super(3);
            createComponents(null, app.getText("d.extensions.comment"));
        }

        @Override
        protected String[] supportedKeys() {
            return new String[]{"customize"};
        }

        @Override
        protected String nameOfKey(String key) {
            return TextUtils.capitalized(key);
        }

        @Override
        protected Object defaultForKey(String key) {
            return "";
        }

        @Override
        protected String getModifyTitle(String key) {
            return app.getText("d.extensions.modify.title", nameOfKey(key));
        }

        @Override
        public void onModified() {
            fireModified();
        }
    }
}
