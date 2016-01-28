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

package pw.phylame.imabw.app.ui.com;

import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

import pw.phylame.gaf.ixin.IAction;
import pw.phylame.gaf.ixin.IxinUtilities;
import pw.phylame.imabw.app.Imabw;
import pw.phylame.imabw.app.Worker;
import pw.phylame.imabw.app.model.OpenResult;
import pw.phylame.imabw.app.ui.dialog.CommonDialog;
import pw.phylame.jem.core.Jem;
import pw.phylame.jem.util.FileFactory;
import pw.phylame.jem.util.FileObject;
import pw.phylame.jem.util.TextFactory;
import pw.phylame.jem.util.TextObject;
import pw.phylame.jem.formats.util.text.TextUtils;

import static pw.phylame.imabw.app.ui.dialog.DialogFactory.*;

public abstract class ItemTable extends MappingTable<String, Object> {
    public static final Imabw app = Imabw.sharedInstance();
    public static final Worker worker = Worker.sharedInstance();

    public static final String EXPORT = "itemTable.exportItem";

    protected int typeColumn = 1;

    public ItemTable(int columns) {
        super(columns);
        commands.add(EXPORT);
        valueColumn = 2;
    }

    public void resetAll(Map<String, Object> map, Set<String> ignoredKeys) {
        resetAll(map, ignoredKeys, String::compareTo);
        if (getKeys().isEmpty()) {
            setActionEnable(EXPORT, false);
        }
    }

    @Override
    protected void createComponents(String title, String comment) {
        super.createComponents(title, comment);
        table.getTableHeader().setReorderingAllowed(false);
    }

    @Override
    protected String nameOfColumn(int column) {
        if (column == keyColumn) {
            return app.getText("com.table.field.key");
        } else if (column == typeColumn) {
            return app.getText("com.table.field.type");
        } else if (column == valueColumn) {
            return app.getText("com.table.field.value");
        } else {
            return null;
        }
    }

    @Override
    protected Object valueOfCell(String key, int column) {
        if (column == keyColumn) {
            return key;
        } else if (column == typeColumn) {
            return worker.readableItemType(valueFor(key));
        } else if (column == valueColumn) {
            return worker.readableItemValue(valueFor(key));
        } else {
            return null;
        }
    }

    @Override
    protected boolean isEditable(int row, int column) {
        return column == keyColumn;
    }

    // last is customize name
    protected abstract String[] supportedKeys();

    protected abstract String nameOfKey(String key);

    protected String typeOfKey(String key) {
        return "str";
    }

    protected abstract Object defaultForKey(String key);

    protected String[] getTypes() {
        return Jem.supportedTypes();
    }

    protected String getTypeName(String type) {
        return worker.readableTypeName(type);
    }

    @Override
    protected void createItem() {
        NewItemDialog dialog;
        String title = app.getText("itemTable.appendItem.title");
        Window window = getWindowForComponent(this);
        if (window instanceof Dialog) {
            dialog = new NewItemDialog((Dialog) window, title);
        } else {
            dialog = new NewItemDialog((Frame) window, title);
        }
        dialog.makeShow(false);
    }

    protected abstract String getModifyTitle(String key);

    @Override
    protected Object modifyValue(String key, Object oldValue) {
        Object value = null;
        String title = getModifyTitle(key);
        switch (Jem.typeOfVariant(oldValue)) {
            case "str":
                value = inputText(this, title,
                        app.getText("itemTable.modifyItem.inputText"),
                        (String) oldValue, true, true);
                break;
            case "datetime":
                value = selectDate(this, title,
                        app.getText("itemTable.modifyItem.selectDate"),
                        (Date) oldValue);
                break;
            case "file": {
                OpenResult od = worker.selectOpenFile(this,
                        app.getText("itemTable.modifyItem.selectFile"),
                        null, null, null, true, false);
                if (od != null) {
                    try {
                        value = FileFactory.fromFile(od.getFile(), null);
                    } catch (IOException e) {
                        throw new AssertionError("BUG: unexpected IOException here.");
                    }
                }
            }
            break;
            case "text": {
                String text = longInput(this, title,
                        app.getText("itemTable.modifyItem.inputText"),
                        TextUtils.fetchText((TextObject) oldValue, ""), true, false);
                if (text != null) {
                    value = TextFactory.fromString(text);
                }
            }
            break;
            case "locale":
                value = selectLocale(this, title,
                        app.getText("itemTable.modifyItem.selectLocale"),
                        (Locale) oldValue);
                break;
            case "int":
                long n;
                if (oldValue instanceof Integer) {
                    n = (Integer) oldValue;
                } else {
                    n = (Long) oldValue;
                }
                value = inputInteger(this, title,
                        app.getText("itemTable.modifyItem.inputInteger"),
                        n, true);
                break;
            case "real":
                value = inputNumber(this, title,
                        app.getText("itemTable.modifyItem.inputNumber"),
                        (Number) oldValue, true);
                break;
            case "bool":
                value = !(boolean) oldValue;
                break;
            default:
                break;
        }
        return value;
    }

    @Override
    protected void itemSelected(int[] rows) {
        super.itemSelected(rows);
        boolean enable = false;
        if (rows.length == 1) {
            switch (Jem.typeOfVariant(valueAt(rows[0]))) {
                case "file":
                case "text":
                    enable = true;
                    break;
            }
        }
        setActionEnable(EXPORT, enable);
    }

    @Override
    protected boolean commandPerformed(String command) {
        switch (command) {
            case EXPORT:
                exportItem();
                break;
            default:
                return super.commandPerformed(command);
        }
        return true;
    }

    private void exportItem() {
        String key = keyAt(table.getSelectedRow());
        String title = app.getText("itemTable.exportItem.title", nameOfKey(key));

        OpenResult od = worker.selectSaveFile(this, title, null, null, null, true);
        if (od == null) {
            return;
        }

        File file = od.getFile();
        Object value = valueFor(key);
        try {
            if (value instanceof FileObject) {
                worker.storeToFile((FileObject) value, file);
            } else if (value instanceof TextObject) {
                worker.storeToFile((TextObject) value, file, null);
            }
            localizedInformation(this, title, "itemTable.exportItem.result", file);
        } catch (IOException e) {
            String str = app.getText("itemTable.exportItem.error", file, e.getMessage());
            viewException(this, title, str, e);
        }
    }

    private class NewItemDialog extends CommonDialog {
        private NamedComboBox<String> cbKeys, cbTypes;
        private JButton btnOk;
        private final String[] fields = supportedKeys(), types = getTypes();

        private NewItemDialog(Dialog owner, String title) {
            super(owner, title, true);
        }

        private NewItemDialog(Frame owner, String title) {
            super(owner, title, true);
        }

        @Override
        protected void createComponents(JPanel userPane) {
            cbKeys = new NamedComboBox<>(fields, ItemTable.this::nameOfKey);
            cbKeys.addActionListener(e -> {
                int index = cbKeys.getSelectedIndex();
                cbTypes.activateItem(typeOfKey(fields[index]));
                if (index == cbKeys.getItemCount() - 1) { // customize
                    cbKeys.setEditable(true);
                    cbKeys.requestFocus();
                    cbTypes.setEnabled(true);
                } else if (index != -1) {                   // default
                    cbKeys.setEditable(false);
                    cbTypes.setEnabled(false);
                    btnOk.setEnabled(true);
                }
            });
            JTextField tf = (JTextField) cbKeys.getEditor().getEditorComponent();
            tf.addCaretListener(e -> {
                String key = tf.getText().trim();
                btnOk.setEnabled(key.length() > 0 && !getMap().containsKey(key));
            });

            cbTypes = new NamedComboBox<>(types, ItemTable.this::getTypeName);
            JLabel lbComment = new JLabel();
            cbTypes.addActionListener(e ->
                            lbComment.setText(app.getText("common.type.desc." + cbTypes.currentItem()))
            );

            JLabel lbKey = IxinUtilities.localizedLabel("itemTable.appendItem.key", app, cbKeys);
            JLabel lbType = IxinUtilities.localizedLabel("itemTable.appendItem.type", app, cbTypes);

            GroupLayout layout = new GroupLayout(userPane);
            userPane.setLayout(layout);

            GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
            hGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(lbKey).addComponent(lbType));
            hGroup.addGap(5);
            hGroup.addGroup(layout.createParallelGroup()
                    .addComponent(cbKeys).addComponent(cbTypes).addComponent(lbComment));
            layout.setHorizontalGroup(hGroup);

            GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
            vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(lbKey).addComponent(cbKeys));
            vGroup.addGap(5);
            vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(lbType).addComponent(cbTypes));
            vGroup.addGap(5);
            vGroup.addGroup(layout.createParallelGroup().addComponent(lbComment));
            layout.setVerticalGroup(vGroup);

            btnOk = new JButton(new IAction(BUTTON_OK) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onOk();
                }
            });
            defaultButton = btnOk;
            controlsPane = createControlsPane(SwingConstants.RIGHT, btnOk,
                    createCloseButton(BUTTON_CANCEL));

            setMinimumSize(new Dimension(320, 110));
            cbKeys.setSelectedIndex(0);
        }

        private void onOk() {
            dispose();
            int index = cbKeys.getSelectedIndex();
            String key;
            Object value;
            if (index == -1 || index == cbKeys.getItemCount() - 1) {  //customized
                key = ((String) cbKeys.getEditor().getItem()).trim();
                value = Jem.defaultOfType(cbTypes.currentItem());
            } else {
                key = fields[index];
                value = defaultForKey(key);
                if (value == null) {
                    value = "";
                }
            }
            appendItem(key, value);
        }
    }
}
