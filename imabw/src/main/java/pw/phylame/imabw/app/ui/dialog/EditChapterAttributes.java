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

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pw.phylame.gaf.ixin.IAction;
import pw.phylame.gaf.ixin.IxinUtilities;
import pw.phylame.imabw.app.Imabw;
import pw.phylame.imabw.app.Worker;
import pw.phylame.imabw.app.config.JemConfig;
import pw.phylame.imabw.app.model.OpenResult;
import pw.phylame.imabw.app.ui.UISnap;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.core.Jem;
import pw.phylame.jem.formats.util.text.TextUtils;
import pw.phylame.jem.util.FileFactory;
import pw.phylame.jem.util.FileObject;
import pw.phylame.jem.util.TextFactory;
import pw.phylame.jem.util.TextObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;
import java.awt.Frame;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Date;
import java.util.Locale;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import static pw.phylame.imabw.app.ui.dialog.DialogFactory.*;

class EditChapterAttributes extends CommonDialog {
    private static final Log LOG = LogFactory.getLog(EditChapterAttributes.class);
    private static final Imabw app = Imabw.sharedInstance();
    private static final Worker worker = Worker.sharedInstance();

    private static final String V_DIVIDER_LOCATION = "attributes.vDivider.location";
    private static final String H_DIVIDER_LOCATION = "attributes.hDivider.location";
    private static final String DIALOG_SIZE = "attributes.size";

    private enum ItemCommand {
        Add("add"), Remove("remove"), Modify("modify"), Export("export");

        ItemCommand(String name) {
            this.name = name;
        }

        private String name;
    }

    private static final ItemCommand[] ITEM_COMMANDS = {
            ItemCommand.Add, ItemCommand.Remove,
            ItemCommand.Modify, ItemCommand.Export};

    public static final String DATE_FORMAT = "yy-M-d";

    public static final String[] chapterKeys = {Chapter.WORDS};

    public static final String[] bookKeys = {
            Chapter.AUTHOR, Chapter.DATE, Chapter.GENRE, Chapter.LANGUAGE,
            Chapter.PUBLISHER, Chapter.PUBLISHER, Chapter.RIGHTS, Chapter.STATE,
            Chapter.SUBJECT, "source", Chapter.VENDOR, Chapter.WORDS
    };

    public static final ArrayList<String> ignoredKeys = new ArrayList<>(
            Arrays.asList(Chapter.TITLE, Chapter.COVER, Chapter.INTRO));

    public static final ArrayList<String> supportedTypes = new ArrayList<>(
            Arrays.asList("str", "int", "datetime", "text", "real", "file", "bool"));

    public static final ArrayList<String> choiceKeys = new ArrayList<>(
            Arrays.asList(Chapter.STATE, Chapter.LANGUAGE, Chapter.GENRE));

    private Chapter chapter;
    private HashMap<String, Object> attributes = new HashMap<>();

    private JLabel lbCoverImage, lbCoverDetails;
    private JButton btnSaveCover, btnRemoveCover;

    private JTable tbAttributes;
    private AttributeTableModel tableModel;
    private HashMap<String, Action> attributeActions = new HashMap<>();

    private JTextArea taIntro;

    private JSplitPane hSplitPane, vSplitPane;

    private JButton btnSave;

    private boolean modified = false;

    public EditChapterAttributes(Frame owner, String title, boolean modal) {
        super(owner, title, modal);
    }

    public EditChapterAttributes(Dialog owner, String title, boolean modal) {
        super(owner, title, modal);
    }

    void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    @Override
    protected void createComponents(JPanel userPane) {
        UISnap snap = UISnap.sharedInstance();

        vSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, createTablePane(),
                createIntroPane());
//        vSplitPane.setBorder(BorderFactory.createEmptyBorder());
        vSplitPane.setDividerSize(7);
        vSplitPane.setDividerLocation(snap.getInteger(V_DIVIDER_LOCATION, 254));

        hSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createCoverPane(),
                vSplitPane);
        hSplitPane.setDividerSize(7);
        hSplitPane.setDividerLocation(snap.getInteger(H_DIVIDER_LOCATION, 256));

        userPane.add(hSplitPane, BorderLayout.CENTER);

        controlsPane = createButtonPane();

        setPreferredSize(snap.getDimension(DIALOG_SIZE, new Dimension(878, 514)));
    }

    @Override
    public Object makeShow(boolean resizable) {
        initialize(resizable);
        resetAttributes();
        setVisible(true);
        return getResult();
    }

    private JPanel createCoverPane() {
        lbCoverImage = new JLabel();
        lbCoverImage.setVerticalAlignment(JLabel.CENTER);
        lbCoverImage.setHorizontalAlignment(JLabel.CENTER);

        lbCoverDetails = new JLabel();
        lbCoverDetails.setHorizontalAlignment(JLabel.CENTER);
        lbCoverDetails.setBorder(BorderFactory.createEmptyBorder(1, 0, 2, 0));

        JButton btnOpenCover = new JButton(
                new IAction("dialog.editAttributes.openCover") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        openCover();
                    }
                });
        btnSaveCover = new JButton(
                new IAction("dialog.editAttributes.saveCover") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        saveCover();
                    }
                });
        btnRemoveCover = new JButton(
                new IAction("dialog.editAttributes.removeCover") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        removeCover();
                    }
                });

        updateCover(null, null);

        JPanel coverPane = new JPanel(new BorderLayout());
        coverPane.setPreferredSize(new Dimension(260, 500));
        coverPane.add(new JScrollPane(lbCoverImage), BorderLayout.CENTER);
        JPanel tp = new JPanel(new BorderLayout());
        tp.add(lbCoverDetails, BorderLayout.CENTER);
        tp.add(new JSeparator(), BorderLayout.PAGE_END);
        coverPane.add(tp, BorderLayout.PAGE_END);

        JPanel buttonPane = new JPanel(new FlowLayout());
        buttonPane.add(btnOpenCover);
        buttonPane.add(btnSaveCover);
        buttonPane.add(btnRemoveCover);

        JPanel pane = new JPanel(new BorderLayout());
        pane.setBorder(BorderFactory.createTitledBorder(
                app.getText("dialog.editAttributes.coverTitle")));
        pane.add(coverPane, BorderLayout.CENTER);
        pane.add(buttonPane, BorderLayout.PAGE_END);

        return pane;
    }

    private void openCover() {
        String title = app.getText("dialog.editAttributes.openCover.title");
        OpenResult od = worker.selectOpenImage(this, title);
        if (od == null) {
            return;
        }
        File file = od.getFile();
        try {
            FileObject fb = FileFactory.fromFile(file, null);
            ImageIcon icon = loadCover(fb, title, true);
            if (icon != null) {
                updateCover(icon, fb.getMime());
                attributes.put(Chapter.COVER, fb);
                fireAttributesModified();
            }
        } catch (IOException e) {
            app.error("cannot create file object: " + file, e);
        }
    }

    private void saveCover() {
        String title = app.getText("dialog.editAttributes.saveCover.title");
        OpenResult od = worker.selectSaveImage(this, title);
        if (od == null) {
            return;
        }
        File file = od.getFile();
        ImageIcon cover = (ImageIcon) lbCoverImage.getIcon();
        BufferedImage img = (BufferedImage) cover.getImage();
        try {
            ImageIO.write(img, od.getFormat(), file);
            localizedInformation(this, title,
                    "dialog.editAttributes.saveCover.result", file.getAbsolutePath());
        } catch (IOException e) {
            viewException(this, title, app.getText("dialog.editAttributes.saveCover.error",
                    file.getAbsolutePath(), e.getMessage()), e);
        }
    }

    private void removeCover() {
        attributes.remove(Chapter.COVER);
        updateCover(null, null);
        fireAttributesModified();
    }

    private ImageIcon scaleImageIcon(ImageIcon imageIcon, Dimension preferredSize) {
        if (imageIcon.getIconWidth() > preferredSize.getWidth() ||
                imageIcon.getIconHeight() > preferredSize.getHeight()) {
            double rate1 = imageIcon.getIconHeight() / (double) imageIcon.getIconWidth();
            double rate2 = preferredSize.getHeight() / preferredSize.getWidth();
            int width, height;
            if (rate1 >= rate2) {
                height = (int) preferredSize.getHeight();
                width = (int) (height / rate1);
            } else {
                width = (int) preferredSize.getWidth();
                height = (int) (rate1 * width);
            }
            return new ImageIcon(imageIcon.getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT));
        } else {
            return imageIcon;
        }
    }

    private void updateCover(ImageIcon cover, String mime) {
        if (cover == null) {
            lbCoverImage.setIcon(null);
            lbCoverImage.setText(app.getText("dialog.editAttributes.altCoverText"));
            lbCoverDetails.setVisible(false);
            btnSaveCover.setEnabled(false);
            btnRemoveCover.setEnabled(false);
        } else {
            Dimension size = new Dimension((int) (getWidth() * 0.33), (int) (getHeight() * 0.75));
            lbCoverImage.setIcon(scaleImageIcon(cover, size));
            lbCoverImage.setText(null);
            lbCoverDetails.setText(app.getText("dialog.editAttributes.coverDetails",
                    cover.getIconWidth(), cover.getIconHeight(), mime));
            lbCoverDetails.setVisible(true);
            btnSaveCover.setEnabled(true);
            btnRemoveCover.setEnabled(true);
        }
    }

    private ImageIcon loadCover(FileObject fb, String title, boolean notifyUser) {
        ImageIcon icon = null;
        InputStream in = null;
        try {
            in = fb.openStream();
            BufferedImage img = ImageIO.read(in);
            if (img != null) {
                icon = new ImageIcon(img);
            } else {
                localizedError(this, title, "dialog.editAttributes.invalidCover", fb.getName());
            }
            fb.reset();
        } catch (IOException e) {
            if (notifyUser) {
                String str = app.getText("dialog.editAttributes.openCover.error",
                        fb.getName(), e.getMessage());
                viewException(this, title, str, e);
            } else {
                app.error("cannot load cover image of " + chapter.stringAttribute(Chapter.TITLE), e);
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOG.debug("cannot close cover stream: " + fb, e);
                }
            }
        }
        return icon;
    }

    private void attributeEvent(ItemCommand command) {
        switch (command) {
            case Remove:
                int[] rows = tbAttributes.getSelectedRows();
                assert rows != null && rows.length > 0;
                int row = rows[0];
                tableModel.removeRows(rows);
                if (row == tbAttributes.getRowCount()) {       // focus to the next row
                    --row;
                }
                if (row > -1) {
                    tbAttributes.setRowSelectionInterval(row, row);
                } else {    // no more row
                    attributeActions.get(ItemCommand.Remove.name).setEnabled(false);
                }
                break;
            case Modify:
                modifyAttribute();
                break;
            case Add:
                addAttribute();
                break;
            case Export:
                exportAttribute();
                break;
        }
    }

    private void addAttribute() {
        NewAttributeDialog dialog = new NewAttributeDialog();
        dialog.setVisible(true);
    }

    private void exportAttribute() {
        String name = tableModel.getKeyAt(tbAttributes.getSelectedRow());
        String title = app.getText("dialog.editAttributes.exportItem.title",
                transAttributeName(name));

        OpenResult od = worker.selectSaveFile(this, title, null, null, null, true);
        if (od == null) {
            return;
        }

        File file = od.getFile();
        String success = app.getText("dialog.editAttributes.exportItem.result",
                file.getAbsolutePath());
        Object value = attributes.get(name);
        try {
            if (value instanceof FileObject) {
                InputStream in = null;
                FileObject fb = (FileObject) value;
                try {
                    in = fb.openStream();
                    FileUtils.copyInputStreamToFile(in, file);
                    fb.reset();
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            LOG.debug("cannot close file object: " + fb, e);
                        }
                    }
                }
            } else if (value instanceof TextObject) {
                FileUtils.write(file, worker.contentOfText((TextObject) value, ""));
            } else if (value instanceof byte[]) {
                FileUtils.writeByteArrayToFile(file, (byte[]) value);
            }
            showInformation(this, title, success);
        } catch (IOException e) {
            String str = app.getText("dialog.editAttributes.exportItem.error",
                    file.getPath(), e.getMessage());
            viewException(this, title, str, e);
        }
    }

    private String chooseGenre(String title, String initGenre) {
        String tipText = app.getText("dialog.editAttributes.inputGenre");
        return (String) chooseItem(this, title, tipText,
                JemConfig.sharedInstance().getGenres().split(";"),
                initGenre.isEmpty() ? null : initGenre, true);
    }

    private String chooseState(String title, String initState) {
        String tipText = app.getText("dialog.editAttributes.inputState");
        return (String) chooseItem(this, title, tipText,
                JemConfig.sharedInstance().getStates().split(";"),
                initState.isEmpty() ? null : initState, true);
    }

    private void modifyAttribute() {
        int row = tbAttributes.getSelectedRow();
        String name = tableModel.getKeyAt(row);
        Object v = null, oldValue = attributes.get(name);

        String title = app.getText("dialog.editAttributes.modifyItem.title",
                transAttributeName(name));

        Class<?> clazz = oldValue.getClass();

        if (clazz == String.class) {
            switch (name) {
                case Chapter.STATE:
                    v = chooseState(title, oldValue.toString());
                    break;
                case Chapter.LANGUAGE:
                    v = selectLocale(this, title,
                            app.getText("dialog.editAttributes.inputLanguage"),
                            oldValue.toString());
                    break;
                case Chapter.GENRE:
                    v = chooseGenre(title, oldValue.toString());
                    break;
                default:
                    break;
            }
        } else if (clazz == Date.class) {
            v = selectDate(this, title,
                    app.getText("dialog.editAttributes.inputDate"),
                    (Date) oldValue);
        } else if (FileObject.class.isAssignableFrom(clazz)) {
            OpenResult od = worker.selectOpenFile(this,
                    app.getText("dialog.editAttributes.inputFile"),
                    null, null, null, true, false);
            if (od != null) {
                try {
                    v = FileFactory.fromFile(od.getFile(), null);
                } catch (IOException e) {
                    app.error("cannot create file object: " + od.getFile(), e);
                }
            }
        } else if (TextObject.class.isAssignableFrom(clazz)) {
            String text = longInput(this, title,
                    app.getText("dialog.editAttributes.inputText"),
                    worker.contentOfText((TextObject) oldValue, ""), true, false);
            if (text != null) {
                v = TextFactory.fromString(text);
            }
        } else if (clazz == Integer.class) {
            v = inputInteger(this, title,
                    app.getText("dialog.editAttributes.inputInteger"),
                    (int) oldValue, true);
        } else if (Number.class.isAssignableFrom(clazz)) {
            v = inputNumber(this, title,
                    app.getText("dialog.editAttributes.inputNumber"),
                    (Number) oldValue, true);
        } else if (clazz == Boolean.class) {
            v = !(boolean) oldValue;
        } else {
            LOG.debug("unsupported type: " + clazz);
        }

        if (v != null) {
            attributes.put(name, v);
            tableModel.fireTableCellUpdated(row, 3);
            fireAttributesModified();
        }
    }

    private JPanel createTablePane() {
        for (ItemCommand command : ITEM_COMMANDS) {
            Action action = new IAction("dialog.editAttributes." + command.name + "Item") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    attributeEvent(command);
                }
            };
            action.setEnabled(false);
            attributeActions.put(command.name, action);
        }

        tableModel = new AttributeTableModel();
        tbAttributes = new JTable(tableModel);
        tbAttributes.setRowHeight(tbAttributes.getRowHeight() + 2);
        tbAttributes.getTableHeader().setReorderingAllowed(false);
        tbAttributes.setIntercellSpacing(new Dimension(0, 1));
        tbAttributes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                // double-click and select the key field
                if (e.getClickCount() == 2 && tbAttributes.getSelectedColumn() != 0) {
                    attributeEvent(ItemCommand.Modify);
                }
            }
        });

        tbAttributes.getSelectionModel().addListSelectionListener(e -> {
            int count = tbAttributes.getSelectedRowCount();
            attributeActions.get(ItemCommand.Export.name).setEnabled(true);
            attributeActions.get(ItemCommand.Modify.name).setEnabled(count == 1);
            if (count == 1) {
                Object v = attributes.get(tableModel.getKeyAt(tbAttributes.getSelectedRow()));
                attributeActions.get(ItemCommand.Export.name).setEnabled(v instanceof FileObject ||
                        v instanceof TextObject || v instanceof byte[]);
            } else {
                attributeActions.get(ItemCommand.Export.name).setEnabled(false);
            }
        });

        JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
        toolBar.setRollover(true);
        toolBar.setFloatable(false);
        toolBar.setBorderPainted(false);
        Object[] model = new Object[ITEM_COMMANDS.length];
        Arrays.setAll(model, ix -> ITEM_COMMANDS[ix].name);
        IxinUtilities.addToolItems(toolBar, model, attributeActions, null);

        JPanel pane = new JPanel(new BorderLayout());
        pane.setBorder(BorderFactory.createTitledBorder(
                app.getText("dialog.editAttributes.tableTitle")));
        pane.add(new JScrollPane(tbAttributes), BorderLayout.CENTER);
        pane.add(toolBar, BorderLayout.LINE_END);
        return pane;
    }

    // translate readable attribute name
    private String transAttributeName(String name) {
        name = TextUtils.toCamelized(name).toString();
        return app.getOptionalText("dialog.editAttributes.itemName." + name,
                TextUtils.toCapitalized(name).toString());
    }

    private String getTypeName(String type) {
        type = TextUtils.toCamelized(type).toString();
        return app.getOptionalText("dialog.editAttributes.itemType." + type, type);
    }

    // translate readable attribute type
    private String transAttributeType(String name) {
        Object o = attributes.get(name);
        String type;
        if (o == null) {
            type = "str";
        } else {
            type = Jem.variantType(o);
        }
        return getTypeName(type);
    }

    // translate readable attribute value
    private String transAttributeValue(String name) {
        Object o = attributes.get(name);
        if (o == null) {
            return "";
        }
        if (o instanceof Date) {
            return TextUtils.formatDate((Date) o, DATE_FORMAT);
        } else if (o instanceof TextObject) {
            String str = worker.contentOfText((TextObject) o, "");
            return str.substring(0, Math.min(str.length(), 40));
        } else if (name.equals(Chapter.LANGUAGE) && o instanceof String) {
            String str = ((String) o).replace('_', '-');
            return Locale.forLanguageTag(str).getDisplayName();
        }
        return String.valueOf(o);
    }

    private JComponent createIntroPane() {
        taIntro = new JTextArea(6, 0);
        taIntro.setLineWrap(true);
        taIntro.setWrapStyleWord(true);
        taIntro.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                fireAttributesModified();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                fireAttributesModified();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                fireAttributesModified();
            }
        });
        JScrollPane scrollPane = new JScrollPane(taIntro);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                app.getText("dialog.editAttributes.intro.title")));
        return scrollPane;
    }

    private JPanel createButtonPane() {
        JButton btnReset = new JButton(
                new IAction("dialog.editAttributes.buttonReset") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        resetAttributes();
                    }
                });

        btnSave = new JButton(
                new IAction("dialog.editAttributes.buttonSave") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        onOk();
                    }
                });
        btnSave.setEnabled(false);

        JButton btnCancel = new JButton(
                new IAction("dialog.editAttributes.buttonCancel") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        onCancel();
                    }
                });

        defaultButton = btnCancel;
        return createControlsPane(SwingConstants.RIGHT, btnReset, btnSave, btnCancel);
    }

    private void resetAttributes() {
        attributes.clear();
        for (String name : chapter.attributeNames()) {
            attributes.put(name, chapter.getAttribute(name));
        }

        // cover image
        ImageIcon cover = null;
        String mime = null;
        Object o = chapter.getAttribute(Chapter.COVER);
        if (o instanceof FileObject) {
            FileObject fb = (FileObject) o;
            cover = loadCover(fb, null, false);
            mime = fb.getMime();
        }
        updateCover(cover, mime);

        // intro text
        o = chapter.getAttribute(Chapter.INTRO);
        if (o instanceof TextObject) {
            taIntro.setText(worker.contentOfText((TextObject) o, ""));
            taIntro.setCaretPosition(0);
        }

        // attributes
        tableModel.update();
        if (tableModel.getRowCount() > 0) {
            tbAttributes.setRowSelectionInterval(0, 0);
        }
        attributeActions.get(ItemCommand.Add.name).setEnabled(true);

        modified = false;
        btnSave.setEnabled(false);

        tbAttributes.requestFocus();
    }

    private void fireAttributesModified() {
        modified = true;
        btnSave.setEnabled(true);
    }

    private void updateToChapter() {
        attributes.put(Chapter.INTRO, TextFactory.fromString(taIntro.getText()));
        app.getForm().getContentsTree().updateChapterAttributes(chapter, attributes, true,
                app.getText("undo.message.editAttributes"));
        app.localizedMessage("dialog.editAttributes.result", chapter);
    }

    private void onOk() {
        destroy();
        if (isModified()) {
            updateToChapter();
        }
    }

    @Override
    protected void onCancel() {
        if (modified) {
            int option = showOptions(this, getTitle(),
                    app.getText("dialog.editAttributes.askUpdate.tip", chapter),
                    MessageDialog.IconStyle.Question,
                    -1, 2,
                    "dialog.editAttributes.askUpdate.discard",
                    Box.createHorizontalGlue(),
                    "dialog.editAttributes.askUpdate.ok",
                    "dialog.editAttributes.askUpdate.cancel");
            switch (option) {
                case 2:
                    onOk();
                    return;
                case 0: // discard
                    break;
                default:
                    return;
            }
        }
        modified = false;
        destroy();
    }

    private void destroy() {
        UISnap snap = UISnap.sharedInstance();
        snap.setDimension(DIALOG_SIZE, getSize());
        snap.setInteger(H_DIVIDER_LOCATION, hSplitPane.getDividerLocation());
        snap.setInteger(V_DIVIDER_LOCATION, vSplitPane.getDividerLocation());
        dispose();
    }

    boolean isModified() {
        return modified;
    }

    private class AttributeTableModel extends AbstractTableModel {
        private ArrayList<String> keys = new ArrayList<>();

        void update() {
            keys.clear();
            Collections.addAll(keys, chapter.attributeNames());
            keys.removeAll(ignoredKeys);
            Collections.sort(keys);
            fireTableDataChanged();
        }

        void addKey(String name) {
            keys.add(name);
            int count = keys.size();
            fireTableRowsInserted(count, count);
            tbAttributes.setRowSelectionInterval(count - 1, count - 1);
            tbAttributes.scrollRectToVisible(tbAttributes.getCellRect(count - 1, 0, true));
            fireAttributesModified();
        }

        String getKeyAt(int row) {
            return keys.get(row);
        }

        void removeRows(int[] rows) {
            String[] nm = keys.toArray(new String[keys.size()]);
            for (int row : rows) {
                String name = nm[row];
                keys.remove(name);
                attributes.remove(name);
            }
            if (rows.length > 0) {
                fireTableDataChanged();
                fireAttributesModified();
            }
        }

        @Override
        public int getRowCount() {
            return keys.size();
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return app.getText("dialog.editAttributes.fieldName.key");
                case 1:
                    return app.getText("dialog.editAttributes.fieldName.name");
                case 2:
                    return app.getText("dialog.editAttributes.fieldName.type");
                default:
                    return app.getText("dialog.editAttributes.fieldName.value");
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            String name = getKeyAt(rowIndex);
            Object v = attributes.get(name);
            return columnIndex == 0 ||
                    (columnIndex == 3 && v instanceof String && !choiceKeys.contains(name));
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            String name = keys.get(rowIndex);
            switch (columnIndex) {
                case 0:                 // key
                    return name;
                case 1:                 // name
                    return transAttributeName(name);
                case 2:                 // type
                    return transAttributeType(name);
                default:                // value
                    return transAttributeValue(name);
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            String key = getKeyAt(rowIndex);
            Object oldValue = attributes.get(key);
            switch (columnIndex) {
                case 0:         // key
                {
                    String newKey = (String) aValue;
                    if (newKey.equals(key)) {
                        return;
                    }
                    keys.set(rowIndex, newKey);
                    attributes.remove(key);
                    attributes.put(newKey, oldValue);
                    // to update the name
                    fireTableRowsUpdated(rowIndex, rowIndex);
                    fireAttributesModified();
                }
                break;
                case 3:         // value
                {
                    if (!aValue.equals(oldValue)) {
                        attributes.put(key, aValue);
                        fireAttributesModified();
                    }
                }
                break;
            }
        }
    }

    private class NewAttributeDialog extends CommonDialog {
        private ArrayList<String> keys, fieldNames, typeNames;

        private JComboBox<String> cbFields, cbTypes;
        private JButton btnOk;

        NewAttributeDialog() {
            super(EditChapterAttributes.this, app.getText("dialog.editAttributes.addItem.title"), true);

            keys = new ArrayList<>();
            if (chapter instanceof Book) {
                Collections.addAll(keys, bookKeys);
            } else {
                Collections.addAll(keys, chapterKeys);
            }
            keys.removeAll(attributes.keySet());
            fieldNames = keys.stream().map(EditChapterAttributes.this::transAttributeName)
                    .collect(Collectors.toCollection(ArrayList::new));
            fieldNames.add(app.getText("dialog.editAttributes.addItem.customize"));

            typeNames = supportedTypes.stream().map(EditChapterAttributes.this::getTypeName)
                    .collect(Collectors.toCollection(ArrayList::new));

            initialize(false);
        }

        @Override
        protected void createComponents(JPanel topPane) {
            cbFields = new JComboBox<>(fieldNames.toArray(new String[fieldNames.size()]));
            cbFields.addActionListener(e -> {
                int index = cbFields.getSelectedIndex();
                if (index == cbFields.getItemCount() - 1) { // customize
                    cbFields.setEditable(true);
                    cbFields.requestFocus();
                    cbTypes.setEnabled(true);
                } else if (index != -1) {                   // default
                    cbFields.setEditable(false);
                    cbTypes.setEnabled(false);
                    btnOk.setEnabled(true);
                }
            });
            final JTextField tf = (JTextField) cbFields.getEditor().getEditorComponent();
            tf.addCaretListener(e -> {
                String key = tf.getText().trim();
                btnOk.setEnabled(key.length() > 0 && !attributes.containsKey(key));
            });

            cbTypes = new JComboBox<>(typeNames.toArray(new String[typeNames.size()]));

            JPanel pane = new JPanel(new GridLayout(2, 1, 0, 5));
            pane.add(IxinUtilities.localizedLabel("dialog.editAttributes.addItem.name", app, cbFields));

            pane.add(IxinUtilities.localizedLabel("dialog.editAttributes.addItem.type", app, cbTypes));

            JPanel panel = new JPanel(new GridLayout(2, 1, 0, 5));
            panel.add(cbFields);
            panel.add(cbTypes);

            JPanel contentPane = new JPanel(new BorderLayout());
            contentPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
            contentPane.add(pane, BorderLayout.LINE_START);
            contentPane.add(panel, BorderLayout.CENTER);

            btnOk = new JButton(new IAction(BUTTON_OK) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onOk();
                }
            });

            controlsPane = createControlsPane(SwingConstants.RIGHT, btnOk, createCloseButton(BUTTON_CANCEL));
            defaultButton = btnOk;

            topPane.add(contentPane, BorderLayout.CENTER);

            topPane.setPreferredSize(new Dimension(300, 80));

            cbFields.setSelectedIndex(0);
        }

        private void onOk() {
            dispose();
            int index = cbFields.getSelectedIndex();
            String key;
            Object value;
            if (index == -1 || index == cbFields.getItemCount() - 1) {  // customized
                key = ((String) cbFields.getEditor().getItem()).trim();
                switch (supportedTypes.get(cbTypes.getSelectedIndex())) {
                    case "int":
                        value = 0;
                        break;
                    case "datetime":
                        value = new Date();
                        break;
                    case "text":
                        value = TextFactory.fromString("");
                        break;
                    case "real":
                        value = 0.0;
                        break;
                    case "file":
                        value = FileFactory.emptyFile();
                        break;
                    case "bool":
                        value = false;
                        break;
                    default:
                        value = "";
                }
            } else {
                key = keys.get(index);
                value = worker.getDefaultAttribute(key);
            }
            attributes.put(key, value);
            tableModel.addKey(key);
        }
    }
}
