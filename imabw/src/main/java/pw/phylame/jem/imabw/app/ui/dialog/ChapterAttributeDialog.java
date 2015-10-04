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

package pw.phylame.jem.imabw.app.ui.dialog;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pw.phylame.gaf.ixin.IAction;
import pw.phylame.gaf.ixin.IToolkit;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.core.Jem;
import pw.phylame.jem.imabw.app.Imabw;
import pw.phylame.jem.imabw.app.Worker;
import pw.phylame.jem.imabw.app.config.GUISnap;
import pw.phylame.jem.imabw.app.config.JemConfig;
import pw.phylame.jem.imabw.app.data.OpenResult;
import pw.phylame.jem.util.FileFactory;
import pw.phylame.jem.util.FileObject;
import pw.phylame.jem.util.TextFactory;
import pw.phylame.jem.util.TextObject;
import pw.phylame.util.DateUtils;
import pw.phylame.util.StringUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

public class ChapterAttributeDialog extends DialogFactory.CommonDialog {
    private static Log   LOG = LogFactory.getLog(ChapterAttributeDialog.class);
    private static Imabw app = Imabw.getInstance();

    private static final String[] ATTRIBUTE_ACTION_KEYS =
            {"Add", "Remove", "Modify", "Export"};

    public static final HashMap<String, Object> defaultAttributes = new HashMap<>();

    public static final ArrayList<String> chapterKeys = new ArrayList<>();

    public static final ArrayList<String> bookKeys = new ArrayList<>();

    public static final ArrayList<String> ignoredKeys = new ArrayList<>(
            Arrays.asList(Chapter.TITLE, Chapter.COVER, Chapter.INTRO));

    public static final ArrayList<String> supportedTypes = new ArrayList<>(
            Arrays.asList("str", "int", "datetime", "text", "real", "file"));

    public static final ArrayList<String> choiceKeys = new ArrayList<>(
            Arrays.asList(Chapter.STATE, Chapter.LANGUAGE, Chapter.GENRE));

    static {
        JemConfig jemConfig = JemConfig.getInstance();
        defaultAttributes.put(Chapter.AUTHOR, jemConfig.getAttributeValue(Chapter.AUTHOR));
        defaultAttributes.put(Chapter.DATE, new Date());
        defaultAttributes.put(Chapter.GENRE, jemConfig.getAttributeValue(Chapter.GENRE));
        defaultAttributes.put(Chapter.LANGUAGE, Locale.getDefault().toLanguageTag());
        defaultAttributes.put(Chapter.PUBLISHER, jemConfig.getAttributeValue(Chapter.PUBLISHER));
        defaultAttributes.put(Chapter.RIGHTS, jemConfig.getAttributeValue(Chapter.RIGHTS));
        defaultAttributes.put(Chapter.STATE, jemConfig.getAttributeValue(Chapter.STATE));
        defaultAttributes.put(Chapter.SUBJECT, jemConfig.getAttributeValue(Chapter.SUBJECT));
        defaultAttributes.put("source", jemConfig.getAttributeValue("source"));
        defaultAttributes.put(Chapter.VENDOR, jemConfig.getAttributeValue(Chapter.VENDOR));
        defaultAttributes.put(Chapter.WORDS, 0);

        bookKeys.addAll(defaultAttributes.keySet());
        Collections.addAll(chapterKeys, Chapter.WORDS);
    }

    private Chapter chapter;
    private Map<String, Object> attributes = new HashMap<>();

    private JPanel coverPane;
    private JLabel lbCoverImage, lbCoverDetails;
    private JButton btnSaveCover, btnRemoveCover;

    private JTable              tbAttributes;
    private AttributeTableModel tableModel;
    private Map<String, Action> attributeActions = new HashMap<>();

    private JTextArea taIntro;

    private JSplitPane coverAndRight, tableAndIntro;

    private JButton btnSave;

    private boolean modified = false;

    ChapterAttributeDialog(Frame owner, Chapter chapter) {
        super(owner, app.getText("Dialog.ChapterProperties.Title",
                chapter.stringAttribute(Chapter.TITLE)), true);
        this.chapter = chapter;
        init(true);
        resetAttributes();
    }

    ChapterAttributeDialog(Dialog owner, Chapter chapter) {
        super(owner, app.getText("Dialog.ChapterProperties.Title",
                chapter.stringAttribute(Chapter.TITLE)), false);
        this.chapter = chapter;
        init(true);
        resetAttributes();
    }

    @Override
    protected void createComponents(JPanel topPane) {
        GUISnap guiSnap = GUISnap.getInstance();

        Dimension dividers = guiSnap.getAttributeDividerLocation();

        tableAndIntro = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                createTablePane(), createIntroPane());
        tableAndIntro.setBorder(BorderFactory.createEmptyBorder());
        tableAndIntro.setDividerSize(7);
        tableAndIntro.setDividerLocation((int) dividers.getHeight());

        coverAndRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                createCoverPane(), tableAndIntro);
        coverAndRight.setDividerSize(7);
        coverAndRight.setDividerLocation((int) dividers.getWidth());

        topPane.add(coverAndRight, BorderLayout.CENTER);

        buttonPane = createButtonPane();

        setPreferredSize(guiSnap.getAttributeDialogSize());
    }

    private JPanel createCoverPane() {
        lbCoverImage = new JLabel();
        lbCoverImage.setVerticalAlignment(JLabel.CENTER);
        lbCoverImage.setHorizontalAlignment(JLabel.CENTER);

        lbCoverDetails = new JLabel();
        lbCoverDetails.setHorizontalAlignment(JLabel.CENTER);
        lbCoverDetails.setBorder(BorderFactory.createEmptyBorder(1, 0, 2, 0));

        JButton btnOpenCover = new JButton(new IAction("Dialog.ChapterProperties.Cover.ButtonOpen") {
            @Override
            public void actionPerformed(ActionEvent e) {
                openCover();
            }
        });
        btnSaveCover = new JButton(new IAction("Dialog.ChapterProperties.Cover.ButtonSave") {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveCover();
            }
        });
        btnRemoveCover = new JButton(new IAction("Dialog.ChapterProperties.Cover.ButtonRemove") {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeCover();
            }
        });

        updateCover(null, null);

        coverPane = new JPanel(new BorderLayout());
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
                app.getText("Dialog.ChapterProperties.Cover.Title")));
        pane.add(coverPane, BorderLayout.CENTER);
        pane.add(buttonPane, BorderLayout.PAGE_END);

        return pane;
    }

    private void openCover() {
        String title = app.getText("Dialog.ChapterProperties.Cover.OpenDialog.Title");
        OpenResult od = Worker.getInstance().selectOpenImage(this, title);
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
            app.debug("cannot create file object: " + file, e);
        }
    }

    private void saveCover() {
        String title = app.getText("Dialog.ChapterProperties.Cover.SaveDialog.Title");
        OpenResult od = Worker.getInstance().selectSaveImage(this, title);
        if (od == null) {
            return;
        }
        File file = od.getFile();
        ImageIcon cover = (ImageIcon) lbCoverImage.getIcon();
        BufferedImage img = (BufferedImage) cover.getImage();
        try {
            ImageIO.write(img, od.getFormat(), file);
            DialogFactory.showMessage(this, title,
                    app.getText("Dialog.ChapterProperties.Cover.SaveDialog.Finished", file.getAbsolutePath()));
        } catch (IOException e) {
            DialogFactory.viewException(this, title,
                    app.getText("Dialog.ChapterProperties.Cover.SaveDialog.Failed",
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
            lbCoverImage.setText(app.getText("Dialog.ChapterProperties.Cover.AltText"));
            lbCoverDetails.setVisible(false);
            btnSaveCover.setEnabled(false);
            btnRemoveCover.setEnabled(false);
        } else {
            Dimension size = new Dimension((int)(getWidth() * 0.33), (int)(getHeight() * 0.75));
            lbCoverImage.setIcon(scaleImageIcon(cover, size));
            lbCoverImage.setText(null);
            lbCoverDetails.setText(app.getText("Dialog.ChapterProperties.Cover.Details",
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
                DialogFactory.showError(this, title,
                        app.getText("Dialog.ChapterProperties.Cover.OpenDialog.NotImage", fb.getName()));
            }
            fb.reset();
        } catch (IOException e) {
            if (notifyUser) {
                String str = app.getText("Dialog.ChapterProperties.Cover.OpenDialog.Error",
                        fb.getName(), e.getMessage());
                DialogFactory.viewException(this, title, str, e);
            } else {
                app.debug("cannot load cover image of " + chapter.stringAttribute(Chapter.TITLE), e);
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOG.debug("cannot close cover stream: "+fb, e);
                }
            }
        }
        return icon;
    }

    private void attributeEvent(String command) {
        switch (command) {
            case "Remove":
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
                    attributeActions.get("Remove").setEnabled(false);
                }
                break;
            case "Modify":
                modifyAttribute();
                break;
            case "Add":
                addAttribute();
                break;
            case "Export":
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
        String title = app.getText("Dialog.ChapterProperties.Attributes.Export.Title",
                transAttributeName(name));

        OpenResult od = Worker.getInstance().selectSaveFile(this, title, null, null, null, true);
        if (od == null) {
            return;
        }

        File file = od.getFile();
        String success = app.getText("Dialog.ChapterProperties.Attributes.Export.Finished",
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
                FileUtils.write(file, ((TextObject) value).getText());
            } else if (value instanceof byte[]) {
                FileUtils.writeByteArrayToFile(file, (byte[]) value);
            }
            DialogFactory.showMessage(this, title, success);
        } catch (IOException e) {
            String str = app.getText("Dialog.ChapterProperties.Attributes.Export.Error",
                    file.getPath(), e.getMessage());
            DialogFactory.viewException(this, title, str, e);
        }
    }

    private static String[] availableGenres() {
        String str = app.getText("Dialog.ChapterProperties.Attributes.Genres");
        return str.split(";");
    }

    private String chooseGenre(String title, String initGenre) {
        String tipText = app.getText("Dialog.ChapterProperties.Attributes.Modify.InputGenre");
        return (String)DialogFactory.chooseItem(this, title, tipText, availableGenres(),
                initGenre.isEmpty() ? null : initGenre, true);
    }

    private static String[] availableStates() {
        String str = app.getText("Dialog.ChapterProperties.Attributes.States");
        return str.split(";");
    }

    private String chooseState(String title, String initState) {
        String tipText = app.getText("Dialog.ChapterProperties.Attributes.Modify.InputState");
        return (String)DialogFactory.chooseItem(this, title, tipText, availableStates(),
                initState.isEmpty() ? null : initState, true);
    }

    private void modifyAttribute() {
        int row = tbAttributes.getSelectedRow();
        String name = tableModel.getKeyAt(row);
        Object v = null, oldValue = attributes.get(name);

        String title = app.getText("Dialog.ChapterProperties.Attributes.Modify.Title",
                transAttributeName(name));

        Class<?> clazz = oldValue.getClass();

        if (clazz == String.class) {
            switch (name) {
                case Chapter.STATE:
                    v = chooseState(title, oldValue.toString());
                    break;
                case Chapter.LANGUAGE:
                    v = DialogFactory.selectLocale(this, title,
                            app.getText("Dialog.ChapterProperties.Attributes.Modify.InputLanguage"),
                            oldValue.toString());
                    break;
                case Chapter.GENRE:
                    v = chooseGenre(title, oldValue.toString());
                    break;
                default:
                    break;
            }
        } else if (clazz == Date.class) {
            v = DialogFactory.selectDate(this, title,
                    app.getText("Dialog.ChapterProperties.Attributes.Modify.InputDate"),
                    (Date)oldValue);
        } else if (FileObject.class.isAssignableFrom(clazz)) {
            OpenResult od = Worker.getInstance().selectOpenFile(this,
                    app.getText("Dialog.ChapterProperties.Attributes.Modify.InputFile"),
                    false, null, null, null, true);
            if (od != null) {
                try {
                    v = FileFactory.fromFile(od.getFile(), null);
                } catch (IOException e) {
                    app.debug("cannot create file object: "+od.getFile(), e);
                }
            }
        } else if (TextObject.class.isAssignableFrom(clazz)) {
            String text = DialogFactory.longInput(this, title,
                    app.getText("Dialog.ChapterProperties.Attributes.Modify.InputText"),
                    ((TextObject) oldValue).getText(), true, false);
            if (text != null) {
                v = TextFactory.fromString(text);
            }
        } else if (clazz == Integer.class) {
            v = DialogFactory.inputInteger(this, title,
                    app.getText("Dialog.ChapterProperties.Attributes.Modify.InputInteger"),
                    (int) oldValue, true);
        } else if (Number.class.isAssignableFrom(clazz)) {

            v = DialogFactory.inputNumber(this, title,
                    app.getText("Dialog.ChapterProperties.Attributes.Modify.InputNumber"),
                    (Number)oldValue, true);
        } else {
            LOG.debug("unsupported type: "+clazz);
        }

        if (v != null) {
            attributes.put(name, v);
            fireAttributesModified();
        }
    }

    private JPanel createTablePane() {
        for (final String key : ATTRIBUTE_ACTION_KEYS) {
            Action action = new IAction("Dialog.ChapterProperties.Attributes."+key) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    attributeEvent(key);
                }
            };
            action.setEnabled(false);
            attributeActions.put(key, action);
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
                    attributeEvent("Modify");
                }
            }
        });

        tbAttributes.getSelectionModel().addListSelectionListener(e -> {
                int count = tbAttributes.getSelectedRowCount();
                attributeActions.get("Remove").setEnabled(true);
                attributeActions.get("Modify").setEnabled(count == 1);
                if (count == 1) {
                    Object v = attributes.get(tableModel.getKeyAt(tbAttributes.getSelectedRow()));
                    attributeActions.get("Export").setEnabled(v instanceof FileObject ||
                            v instanceof TextObject || v instanceof byte[]);
                } else {
                    attributeActions.get("Export").setEnabled(false);
                }
        });

        JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
        toolBar.setRollover(true);
        toolBar.setFloatable(false);
        toolBar.setBorderPainted(false);
        IToolkit.addComponents(toolBar, ATTRIBUTE_ACTION_KEYS, attributeActions, null);

        JPanel pane = new JPanel(new BorderLayout());
        pane.setBorder(BorderFactory.createTitledBorder(
                app.getText("Dialog.ChapterProperties.Attributes.Title")));
        pane.add(new JScrollPane(tbAttributes), BorderLayout.CENTER);
        pane.add(toolBar, BorderLayout.LINE_END);
        return pane;
    }

    // translate readable attribute name
    private String transAttributeName(String name) {
        name = StringUtils.toCapital(name);
        try {
            return app.getText("Dialog.ChapterProperties.Attributes.Name." + name);
        } catch (MissingResourceException e) {
            app.debug("unknown attribute name: " + e.getKey());
            return name;
        }
    }

    private String getTypeName(String type) {
        type = StringUtils.toCapital(type);
        try {
            return app.getText("Dialog.ChapterProperties.Attributes.Type." + type);
        } catch (MissingResourceException e) {
            app.debug("unknown attribute type: " + e.getKey());
            return type;
        }
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

    private String toString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (byte b : bytes) {;
            String s = Integer.toHexString(b);
            sb.append(s.length() == 2 ? s : "0"+s);
            ++i;
            if (i != bytes.length) {
                sb.append(' ');
            }
        }
        return sb.toString();
    }

    // translate readable attribute value
    private String transAttributeValue(String name) {
        Object o = attributes.get(name);
        if (o == null) {
            return "";
        }
        if (o instanceof Date) {
            return DateUtils.formatDate((Date) o,
                    app.getText("Dialog.ChapterProperties.Attributes.DateFormat"));
        } else if (o instanceof TextObject) {
            String str = ((TextObject)o).getText();
            return str.substring(0, Math.min(str.length(), 40));
        } else if (name.equals(Chapter.LANGUAGE) && o instanceof String) {
            String str = ((String) o).replace('_', '-');
            return Locale.forLanguageTag(str).getDisplayName();
        } else if (o instanceof FileObject) {
            FileObject fb = (FileObject)o;
            return "URL: "+fb.getName()+"; MIME: "+fb.getMime();
        } else if (o instanceof byte[]) {
            return toString((byte[]) o);
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
                app.getText("Dialog.ChapterProperties.Intro.Title")));
        return scrollPane;
    }

    private JPanel createButtonPane() {
        JButton btnReset = new JButton(new IAction("Dialog.ChapterProperties.ButtonReset") {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetAttributes();
            }
        });

        btnSave = new JButton(new IAction("Dialog.ChapterProperties.ButtonSave") {
            @Override
            public void actionPerformed(ActionEvent e) {
                onOk();
            }
        });
        btnSave.setEnabled(false);

        JButton btnCancel = new JButton(new IAction("Dialog.ChapterProperties.ButtonCancel") {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        btnDefault = btnCancel;
        return makeButtonPane(SwingConstants.RIGHT, btnReset, btnSave, btnCancel);
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
            FileObject fb = (FileObject)o;
            cover = loadCover(fb, null, false);
            mime = fb.getMime();
        }
        updateCover(cover, mime);

        // intro text
        o = chapter.getAttribute(Chapter.INTRO);
        if (o instanceof TextObject) {
            taIntro.setText(((TextObject) o).getText());
            taIntro.setCaretPosition(0);
        }

        // attributes
        tableModel.update();
        if (tableModel.getRowCount() > 0) {
            tbAttributes.setRowSelectionInterval(0, 0);
        }
        attributeActions.get("Add").setEnabled(true);

        modified = false;
        btnSave.setEnabled(false);

        tbAttributes.requestFocus();
    }

    private void fireAttributesModified() {
        modified = true;
        btnSave.setEnabled(true);
    }

    private void updateToChapter() {
        chapter.clearAttributes();
        chapter.updateAttributes(attributes);
        String intro = taIntro.getText();
        if (!intro.isEmpty()) {
            chapter.setIntro(TextFactory.fromString(intro));
        }
        String msg = app.getText("Dialog.ChapterProperties.AttributesUpdated", chapter.getTitle());
        app.notifyMessage(msg);
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
            String[] options = {
                    app.getText("Dialog.ChapterProperties.AskUpdate.Ok"),
                    app.getText("Dialog.ChapterProperties.AskUpdate.Discard"),
                    app.getText("Dialog.ChapterProperties.AskUpdate.Cancel")};
            String title = chapter.stringAttribute(Chapter.TITLE);
            int rev = DialogFactory.showAsking(this,
                    app.getText("Dialog.ChapterProperties.Title", title),
                    app.getText("Dialog.ChapterProperties.AskUpdate", title),
                    options, options[0]);
            switch (rev) {
                case DialogFactory.YES_OPTION:
                    onOk();
                    return;
                case DialogFactory.CANCEL_OPTION:
                case -1:
                    return;
            }
        }
        modified = false;
        destroy();
    }

    private void destroy() {
        GUISnap guiSnap = GUISnap.getInstance();
        guiSnap.setAttributeDialogSize(getSize());
        Dimension dividers = new Dimension(coverAndRight.getDividerLocation(),
                tableAndIntro.getDividerLocation());
        guiSnap.setAttributeDividerLocation(dividers);
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
            tbAttributes.scrollRectToVisible(tbAttributes.getCellRect(count-1, 0, true));
            fireAttributesModified();
        }

        String getKeyAt(int row) {
            return keys.get(row);
        }

        void removeRows(int[] rows) {
            ArrayList<String> nm = new ArrayList<>(keys);
            for (int row : rows) {
                String name = nm.get(row);
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
                    return app.getText("Dialog.ChapterProperties.Attributes.Field.Key");
                case 1:
                    return app.getText("Dialog.ChapterProperties.Attributes.Field.Name");
                case 2:
                    return app.getText("Dialog.ChapterProperties.Attributes.Field.Type");
                default:
                    return app.getText("Dialog.ChapterProperties.Attributes.Field.Value");
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
                    String newKey = (String)aValue;
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

    private class NewAttributeDialog extends DialogFactory.CommonDialog {
        private ArrayList<String> keys, fieldNames, typeNames;

        private JComboBox<String>   cbFields, cbTypes;
        private JButton             btnOk;

        NewAttributeDialog() {
            super(ChapterAttributeDialog.this,
                    app.getText("Dialog.ChapterProperties.Attributes.Add.Title"), true);

            keys = new ArrayList<>();
            if (chapter instanceof Book) {
                keys.addAll(bookKeys);
            } else {
                keys.addAll(chapterKeys);
            }
            keys.removeAll(attributes.keySet());
            fieldNames = new ArrayList<>();
            for (String key : keys) {
                fieldNames.add(transAttributeName(key));
            }
            fieldNames.add(app.getText("Dialog.ChapterProperties.Attributes.Add.customize"));

            typeNames = new ArrayList<>();
            for (String type : supportedTypes) {
                typeNames.add(getTypeName(type));
            }

            init();
            btnDefault.requestFocus();
        }

        @Override
        protected void createComponents(JPanel topPane) {
            cbFields = new JComboBox<>(fieldNames.toArray(new String[0]));
            cbFields.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
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
                }
            });
            final JTextField tf = (JTextField)cbFields.getEditor().getEditorComponent();
            tf.addCaretListener(new CaretListener() {
                @Override
                public void caretUpdate(CaretEvent e) {
                    String key = tf.getText().trim();
                    btnOk.setEnabled(key.length() > 0 && !attributes.containsKey(key));
                }
            });

            cbTypes  = new JComboBox<>(typeNames.toArray(new String[0]));

            JPanel pane = new JPanel(new GridLayout(2, 1, 0, 5));
            JLabel label = IToolkit.labelWithMnemonic("Dialog.ChapterProperties.Attributes.Add.Name", app);
            label.setLabelFor(cbFields);
            pane.add(label);

            label = IToolkit.labelWithMnemonic("Dialog.ChapterProperties.Attributes.Add.Type", app);
            label.setLabelFor(cbTypes);
            pane.add(label);

            JPanel panel = new JPanel(new GridLayout(2, 1, 0, 5));
            panel.add(cbFields);
            panel.add(cbTypes);

            JPanel contentPane = new JPanel(new BorderLayout());
            contentPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
            contentPane.add(pane, BorderLayout.LINE_START);
            contentPane.add(panel, BorderLayout.CENTER);

            btnOk = new JButton(new IAction("Dialog.ChapterProperties.Attributes.Add.ButtonOk") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onOk();
                }
            });
            JButton btnCancel = new JButton(new IAction("Dialog.ChapterProperties.Attributes.Add.ButtonCancel") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onCancel();
                }
            });
            buttonPane = makeButtonPane(SwingConstants.RIGHT, btnOk, btnCancel);
            btnDefault = btnOk;

            topPane.add(contentPane, BorderLayout.CENTER);

            topPane.setPreferredSize(new Dimension(300, 97));

            cbFields.setSelectedIndex(0);
        }

        @Override
        protected void onCancel() {
            dispose();
        }

        private void onOk() {
            dispose();
            int index = cbFields.getSelectedIndex();
            String key;
            Object value;
            if (index == -1 || index == cbFields.getItemCount() - 1) {  // customized
                key = ((String)cbFields.getEditor().getItem()).trim();
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
                        value = FileFactory.emptyFile("a.txt", null);
                        break;
                    default:
                        value = "";
                }
            } else {
                key = keys.get(index);
                value = defaultAttributes.get(key);
            }
            attributes.put(key, value);
            tableModel.addKey(key);
        }
    }
}
