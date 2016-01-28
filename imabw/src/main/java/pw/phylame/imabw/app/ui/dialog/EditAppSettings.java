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
import pw.phylame.gaf.ixin.IxinUtilities;
import pw.phylame.imabw.app.Imabw;
import pw.phylame.imabw.app.Worker;
import pw.phylame.imabw.app.config.AppConfig;
import pw.phylame.imabw.app.config.EditorConfig;
import pw.phylame.imabw.app.config.JemConfig;
import pw.phylame.imabw.app.config.UIConfig;
import pw.phylame.imabw.app.ui.com.MappingTable;
import pw.phylame.imabw.app.util.BookUtils;
import pw.phylame.jem.util.TextFactory;
import say.swing.JFontChooser;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

import static pw.phylame.imabw.app.ui.dialog.DialogFactory.*;

/**
 * Dialog for editing imabw preferences.
 */
class EditAppSettings extends CommonDialog {
    private static final Imabw app = Imabw.sharedInstance();

    private JPanel settingsPane;
    private JButton buttonOk;

    private JButton btnGlobalFont;
    private JCheckBox chbDecorateWindow;
    private JButton btnEditorFont;
    private JButton btnEditorFgColor;
    private JButton btnEditorBgColor;
    private JButton btnEditorHlColor;
    private JCheckBox chbEditorWL;
    private JCheckBox chbEditorWW;
    private JCheckBox chbEditorLN;
    private JCheckBox chbPluginsEnable;
    private JComboBox<String> cbbLaf;
    private JComboBox<String> cbbLanguage;
    private JTextField tfGenres;
    private JButton btnEditGenres;
    private JTextField tfStates;
    private JButton btnEditStates;
    private JCheckBox chbDisableHistory;
    private JSlider sldHistoryLimits;
    private JButton btnNewDefault;
    private JButton btnRemoveDefault;
    private JLabel lbHistoryLimits;
    private JLabel lbGlobalFontViewer;
    private JCheckBox chbFontAA;
    private JCheckBox chbUseMnemonic;
    private JComboBox<String> cbbIcons;
    private JLabel lbEditorFontViewer;
    private JLabel lbFgViewer;
    private JLabel lbBgViewer;
    private JLabel lbHlViewer;
    private JComboBox<String> cbbTabPlacement;
    private JComboBox<String> cbbTabLayout;
    private JTable tbDefaults;
    private JButton btnEditMA;
    private JButton btnEditPA;
    private DefaultsModel tbmDefaults;

    public EditAppSettings(Frame owner) {
        super(owner, app.getText("dialog.settings.title"), true);
        resetComponents();
        initialize(true);
        setSize(Math.max(getWidth(), 632), Math.max(getHeight(), 317));
    }

    @Override
    protected void createComponents(JPanel topPane) {
        topPane.add(settingsPane, BorderLayout.CENTER);
    }

    private void createButtons() {
        JButton btnReset = new JButton(
                new IAction("dialog.settings.buttonReset") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        onReset();
                    }
                });

        buttonOk = new JButton(
                new IAction("dialog.settings.buttonOk") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        onOk();
                    }
                });
        buttonOk.setEnabled(false);
        defaultButton = buttonOk;

        JButton btnCancel = new JButton(
                new IAction("dialog.settings.buttonCancel") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        onCancel();
                    }
                });

        controlsPane = createControlsPane(SwingConstants.RIGHT, btnReset, buttonOk, btnCancel);
    }

    private boolean resetting = true;

    private void notifyModified() {
        if (!resetting) {
            buttonOk.setEnabled(true);
        }
    }

    private void onReset() {
        dumpedAppConfig.reset();
        dumpedUIConfig.reset();
        dumpedEditorConfig.reset();
        dumpedJemConfig.reset();
        resetComponents();
    }

    @Override
    protected void onCancel() {
        dispose();
    }

    private void sync() {
        AppConfig.sharedInstance().update(dumpedAppConfig);
        UIConfig.sharedInstance().update(dumpedUIConfig);
        EditorConfig.sharedInstance().update(dumpedEditorConfig);
        JemConfig jemConfig = JemConfig.sharedInstance();
        jemConfig.clear();
        jemConfig.update(dumpedJemConfig);
    }

    private void onOk() {
        dispose();
        sync();
        doApplyTasks();
    }

    private int applyTasks = 0;
    private static final int REFRESH_UI = 1;
    private static final int UPDATE_FONT = 1 << 1 | REFRESH_UI;
    private static final int UPDATE_LAT = 1 << 2 | REFRESH_UI;

    private boolean hasTask(int id) {
        return (applyTasks & id) != 0;
    }

    private void addTask(int id) {
        applyTasks |= id;
    }

    private void doApplyTasks() {
        if (applyTasks == 0) {
            return;
        }
        if (hasTask(UPDATE_LAT)) {
            String laf = dumpedUIConfig.getLafTheme();
            try {
                IxinUtilities.setLafTheme(laf);
            } catch (RuntimeException e) {
                localizedError(this, getTitle(), "settings.update.invalidLaf", laf);
            }
        }
        if (hasTask(UPDATE_FONT)) {
            IxinUtilities.setGlobalFont(dumpedUIConfig.getGlobalFont());
        }
        if (hasTask(REFRESH_UI)) {
            refreshUI();
        }
    }

    private void refreshUI() {
        SwingUtilities.updateComponentTreeUI(app.getForm());
        SwingUtilities.updateComponentTreeUI(fileChooser);
    }

    private void requireRestartTip() {
//        DialogFactory.localizedInformation(this, getTitle(), "settings.modify.requireRestart");
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        createButtons();
        createGeneralPage();
        createGUIPage();
        createEditorPage();
        createPluginPage();
        createJemPage();
    }

    private void createGeneralPage() {
        dumpedAppConfig = AppConfig.dumpedInstance();

        Locale[] locales = AppConfig.supportedLocales();
        cbbLanguage = new JComboBox<>();
        for (Locale locale : locales) {
            cbbLanguage.addItem(locale.getDisplayName());
        }
        cbbLanguage.addActionListener(e -> {
            Locale locale = locales[cbbLanguage.getSelectedIndex()];
            if (locale != dumpedAppConfig.getAppLocale()) {
                dumpedAppConfig.setAppLocale(locale);
                notifyModified();
                requireRestartTip();
            }
        });

        // chbDisableHistory
        chbDisableHistory = new JCheckBox();
        chbDisableHistory.addActionListener(e -> {
            boolean enable = !chbDisableHistory.isSelected();
            dumpedAppConfig.setHistoryEnable(enable);
            sldHistoryLimits.setEnabled(enable);
            notifyModified();
        });
        lbHistoryLimits = new JLabel();
        sldHistoryLimits = new JSlider(1, Imabw.MAX_HISTORY_LIMITS);
        sldHistoryLimits.addChangeListener(e -> {
            dumpedAppConfig.setHistoryLimits(sldHistoryLimits.getValue());
            lbHistoryLimits.setText(Integer.toString(sldHistoryLimits.getValue()));
            notifyModified();
        });
    }

    private Font editFont(Font initFont) {
        JFontChooser fontChooser = new JFontChooser();
        if (initFont != null) {
            fontChooser.setSelectedFont(initFont);
        }
        if (fontChooser.showDialog(this) != JFontChooser.OK_OPTION) {
            return null;
        }
        return fontChooser.getSelectedFont();
    }

    private void createGUIPage() {
        dumpedUIConfig = UIConfig.dumpedInstance();
        cbbLaf = new JComboBox<>();
        String[] lafs = UIConfig.supportedLafs();
        for (String laf : lafs) {
            cbbLaf.addItem(laf);
        }
        cbbLaf.addActionListener(e -> {
            String laf = lafs[cbbLaf.getSelectedIndex()];
            if (!laf.equals(dumpedUIConfig.getLafTheme())) {
                dumpedUIConfig.setLafTheme(laf);
                addTask(UPDATE_LAT);
                notifyModified();
            }
        });

        btnGlobalFont = new JButton();
        btnGlobalFont.addActionListener(e -> {
            Font font = editFont(dumpedUIConfig.getGlobalFont());
            if (font == null) {
                return;
            }
            dumpedUIConfig.setGlobalFont(font);
            lbGlobalFontViewer.setFont(font);
            lbGlobalFontViewer.setText(toString(font));
            notifyModified();
            addTask(UPDATE_FONT);
        });

        chbFontAA = new JCheckBox();
        chbFontAA.addActionListener(e -> {
            dumpedUIConfig.setAntiAliasing(chbFontAA.isSelected());
            notifyModified();
            addTask(REFRESH_UI);
        });

        cbbIcons = new JComboBox<>();
        String[] icons = UIConfig.supportedIcons();
        for (String icon : icons) {
            cbbIcons.addItem(icon);
        }
        cbbIcons.addActionListener(e -> {
            String icon = icons[cbbIcons.getSelectedIndex()];
            if (!icon.equals(dumpedUIConfig.getIconSet())) {
                dumpedUIConfig.setIconSet(icon);
                notifyModified();
                requireRestartTip();
            }
        });

        chbUseMnemonic = new JCheckBox();
        chbUseMnemonic.addActionListener(e -> {
            dumpedUIConfig.setMnemonicEnable(chbUseMnemonic.isSelected());
            notifyModified();
        });

        chbDecorateWindow = new JCheckBox();
        chbDecorateWindow.addActionListener(e -> {
            dumpedUIConfig.setWindowDecorated(chbDecorateWindow.isSelected());
            notifyModified();
        });
    }

    private Color editColor(Color initColor) {
        return JColorChooser.showDialog(this, app.getText("dialog.chooseColor.title"), initColor);
    }

    private void createEditorPage() {
        dumpedEditorConfig = EditorConfig.dumpedInstance();
        btnEditorFont = new JButton();
        btnEditorFont.addActionListener(e -> {
            Font font = editFont(dumpedEditorConfig.getFont());
            if (font == null) {
                return;
            }
            dumpedEditorConfig.setFont(font);
            lbEditorFontViewer.setFont(font);
            lbEditorFontViewer.setText(toString(font));
            notifyModified();
        });

        btnEditorFgColor = new JButton();
        btnEditorFgColor.addActionListener(e -> {
            Color color = editColor(dumpedEditorConfig.getForeground());
            if (color != null) {
                dumpedEditorConfig.setForeground(color);
                lbFgViewer.setBackground(color);
                notifyModified();
            }
        });

        btnEditorBgColor = new JButton();
        btnEditorBgColor.addActionListener(e -> {
            Color color = editColor(dumpedEditorConfig.getBackground());
            if (color != null) {
                dumpedEditorConfig.setBackground(color);
                lbFgViewer.setBackground(color);
                notifyModified();
            }
        });

        btnEditorHlColor = new JButton();
        btnEditorHlColor.addActionListener(e -> {
            Color color = editColor(dumpedEditorConfig.getHighlight());
            if (color != null) {
                dumpedEditorConfig.setHighlight(color);
                lbFgViewer.setBackground(color);
                notifyModified();
            }
        });

        String[] items = {"top", "left", "bottom", "right"};
        cbbTabPlacement = new JComboBox<>();
        for (String item : items) {
            cbbTabPlacement.addItem(app.getText("settings.editor.tab.placement." + item));
        }
        cbbTabPlacement.addActionListener(e -> {
            int index = cbbTabPlacement.getSelectedIndex() + 1;
            if (index != dumpedEditorConfig.getTabPlacement()) {
                dumpedEditorConfig.setTabPlacement(index);
                notifyModified();
            }
        });

        items = new String[]{"warp", "scroll"};
        cbbTabLayout = new JComboBox<>();
        for (String item : items) {
            cbbTabLayout.addItem(app.getText("settings.editor.tab.layout." + item));
        }
        cbbTabLayout.addActionListener(e -> {
            int index = cbbTabLayout.getSelectedIndex();
            if (index != dumpedEditorConfig.getTabLayout()) {
                dumpedEditorConfig.setTabLayout(index);
                notifyModified();
            }
        });

        chbEditorWL = new JCheckBox();
        chbEditorWL.addActionListener(e -> {
            dumpedEditorConfig.setLineWarp(chbEditorWL.isSelected());
            notifyModified();
        });

        chbEditorWW = new JCheckBox();
        chbEditorWW.addActionListener(e -> {
            dumpedEditorConfig.setWordWarp(chbEditorWW.isSelected());
            notifyModified();
        });

        chbEditorLN = new JCheckBox();
        chbEditorLN.addActionListener(e -> {
            dumpedEditorConfig.setShowLineNumber(chbEditorLN.isSelected());
            notifyModified();
        });
    }

    private void createPluginPage() {
        chbPluginsEnable = new JCheckBox();
        chbPluginsEnable.addActionListener(e -> {

        });
    }

    private String editValues(String what, String initValues) {
        String values = longInput(this,
                app.getText("settings.jem." + what + ".title"),
                app.getText("settings.jem." + what + ".tip"),
                initValues.replace(';', '\n'), true, false);
        if (values == null) {
            return null;
        }
        return String.join(";", TextFactory.splitLines(values, true));
    }

    private void createJemPage() {
        dumpedJemConfig = JemConfig.dumpedInstance();

        btnEditPA = new JButton(new IAction("settings.jem.editPA") {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        btnEditMA = new JButton(new IAction("settings.jem.editMA") {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        btnEditGenres = new JButton();
        btnEditGenres.addActionListener(e -> {
            String genres = editValues("editGenres", dumpedJemConfig.getGenres());
            if (genres != null) {
                dumpedJemConfig.setGenres(genres);
                notifyModified();
            }
        });
        btnEditStates = new JButton();
        btnEditStates.addActionListener(e -> {
            String states = editValues("editStates", dumpedJemConfig.getStates());
            if (states != null) {
                dumpedJemConfig.setStates(states);
                notifyModified();
            }
        });

        tbmDefaults = new DefaultsModel();
        tbDefaults = new JTable(tbmDefaults);
        tbDefaults.getTableHeader().setReorderingAllowed(false);

        btnNewDefault = new JButton();
        btnNewDefault.addActionListener(e -> {
            String name = inputText(this,
                    app.getText("settings.jem.newDefault.title"),
                    app.getText("settings.jem.newDefault.tip"), null, false, false);
            if (name != null) {
                tbmDefaults.newDefault(name);
            }
        });

        btnRemoveDefault = new JButton();
        btnRemoveDefault.addActionListener(e -> {
            int[] rows = tbDefaults.getSelectedRows();
            if (rows != null && rows.length > 0) {
                tbmDefaults.removeRow(rows);
            }
        });
    }

    private void resetComponents() {
        resetting = true;
        resetGeneralPage();
        resetGUIPage();
        resetEditorPage();
        resetPluginPage();
        resetJemPage();
        resetting = false;
    }

    private void resetGeneralPage() {
        cbbLanguage.setSelectedItem(dumpedAppConfig.getAppLocale().getDisplayName());
        chbDisableHistory.setSelected(!dumpedAppConfig.isHistoryEnable());
        sldHistoryLimits.setEnabled(!chbDisableHistory.isSelected());
        int limits = dumpedAppConfig.getHistoryLimits();
        sldHistoryLimits.setValue(limits);
        lbHistoryLimits.setText(Integer.toString(limits));
    }

    private String toString(Font font) {
        return UIConfig.getConverter(Font.class).toString(font);
    }

    private void resetGUIPage() {
        cbbLaf.setSelectedItem(dumpedUIConfig.getLafTheme());
        Font font = dumpedUIConfig.getGlobalFont();
        if (font != null) {
            lbGlobalFontViewer.setFont(font);
            lbGlobalFontViewer.setText(toString(font));
        } else {
            lbGlobalFontViewer.setText(app.getText("settings.gui.font.default"));
        }
        chbFontAA.setSelected(dumpedUIConfig.isAntiAliasing());
        cbbIcons.setSelectedItem(dumpedUIConfig.getIconSet());
        chbUseMnemonic.setSelected(dumpedUIConfig.isMnemonicEnable());
        chbDecorateWindow.setSelected(dumpedUIConfig.isWindowDecorated());
    }

    private void resetEditorPage() {
        Font font = dumpedEditorConfig.getFont();
        if (font != null) {
            lbEditorFontViewer.setFont(font);
            lbEditorFontViewer.setText(toString(font));
        } else {
            lbEditorFontViewer.setText(app.getText("settings.editor.font.default"));
        }
        lbFgViewer.setBackground(dumpedEditorConfig.getForeground());
        lbBgViewer.setBackground(dumpedEditorConfig.getBackground());
        lbHlViewer.setBackground(dumpedEditorConfig.getHighlight());
        cbbTabPlacement.setSelectedIndex(dumpedEditorConfig.getTabPlacement() - 1);
        cbbTabLayout.setSelectedIndex(dumpedEditorConfig.getTabLayout());
        chbEditorWL.setSelected(dumpedEditorConfig.isLineWarp());
        chbEditorWW.setSelected(dumpedEditorConfig.isWordWarp());
        chbEditorLN.setSelected(dumpedEditorConfig.isShowLineNumber());
    }

    private void resetPluginPage() {

    }

    private void resetJemPage() {
        tfGenres.setText(dumpedJemConfig.getGenres());
        tfStates.setText(dumpedJemConfig.getStates());
        tbmDefaults.reset();
    }

    private class DefaultsModel extends AbstractTableModel {
        Map<String, String> values;
        List<String> names = new ArrayList<>();
        Worker worker = Worker.sharedInstance();

        private void reset() {
            values = dumpedJemConfig.defaultValues();
            names.clear();
            names.addAll(values.keySet());
            Collections.sort(names);
            fireTableDataChanged();
        }

        private void newDefault(String name) {
            if (names.contains(name)) {
                return;
            }
            names.add(name);
            values.put(name, "");
            dumpedJemConfig.setAttribute(name, "");
            int count = names.size();
            fireTableRowsInserted(count, count);
            tbDefaults.setRowSelectionInterval(count - 1, count - 1);
            tbDefaults.scrollRectToVisible(tbDefaults.getCellRect(count - 1, 0, true));
            notifyModified();
        }

        private void removeRow(int[] rows) {
            String[] nm = names.toArray(new String[names.size()]);
            for (int row : rows) {
                String name = nm[row];
                names.remove(name);
                values.remove(name);
                dumpedJemConfig.removeAttribute(name);
            }
            fireTableDataChanged();
            notifyModified();
        }

        @Override
        public int getRowCount() {
            return names.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return app.getText("com.table.field.key");
                case 1:
                    return app.getText("common.table.field.name");
                default:
                    return app.getText("com.table.field.value");
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 2;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            String name = names.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return name;
                case 1:
                    return BookUtils.nameOfAttribute(name);
                default:
                    return worker.readableItemValue(values.get(name));
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex != 2) {
                throw new AssertionError("BUG: only value is editable");
            }
            String name = names.get(rowIndex);
            String oldValue = values.get(name);
            if (!aValue.equals(oldValue)) {
                values.put(name, aValue.toString());
                dumpedJemConfig.setAttribute(name, aValue.toString());
                notifyModified();
            }
        }
    }

    private class DefaultsTable extends MappingTable<String, String> {
        private final int KEY_COLUMN = 0;
        private final int NAME_COLUMN = 1;
        private final int VALUE_COLUMN = 2;

        private Worker worker = Worker.sharedInstance();

        private DefaultsTable() {
            super(3);
        }

        @Override
        protected String nameOfColumn(int column) {
            switch (column) {
                case KEY_COLUMN:
                    return app.getText("com.table.field.key");
                case NAME_COLUMN:
                    return app.getText("common.table.field.name");
                default:
                    return app.getText("com.table.field.value");
            }
        }

        @Override
        protected Object valueOfCell(String key, int column) {
            switch (column) {
                case KEY_COLUMN:
                    return key;
                case NAME_COLUMN:
                    return BookUtils.nameOfAttribute(key);
                default:
                    return valueFor(key);
            }
        }

        @Override
        protected void createItem() {

        }

        @Override
        protected String modifyValue(String key, Object oldValue) {
            return null;
        }
    }

    private AppConfig dumpedAppConfig;
    private UIConfig dumpedUIConfig;
    private EditorConfig dumpedEditorConfig;
    private JemConfig dumpedJemConfig;
}
