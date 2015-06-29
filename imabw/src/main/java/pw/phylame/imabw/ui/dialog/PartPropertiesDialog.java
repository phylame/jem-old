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

package pw.phylame.imabw.ui.dialog;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.imageio.ImageIO;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import java.io.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;

import pw.phylame.jem.util.FileFactory;
import pw.phylame.jem.util.FileObject;
import pw.phylame.jem.util.TextObject;
import pw.phylame.pat.ixin.IAction;
import pw.phylame.imabw.Imabw;
import pw.phylame.imabw.Worker;

import pw.phylame.imabw.ui.com.NewAttributePane;
import pw.phylame.imabw.ui.com.ITextEdit;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Jem;
import pw.phylame.jem.core.Part;
import pw.phylame.tools.DateUtils;
import pw.phylame.tools.StringUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PartPropertiesDialog extends JDialog {
    private static Log       LOG         = LogFactory.getLog(PartPropertiesDialog.class);
    private static Point     oldLocation = null;
    private static Dimension oldSize     = null;

    private static ArrayList<String> CommonNames  = new ArrayList<>(
            Arrays.asList(Book.AUTHOR, Book.DATE, Book.GENRE, Book.LANGUAGE, Book.PUBLISHER,
                    Book.RIGHTS, Book.STATE, Book.SUBJECT, "source", Book.VENDOR)
    );
    private static ArrayList<String> IgnoredNames = new ArrayList<>(
            Arrays.asList(Book.TITLE, Book.COVER, Book.INTRO));

    private static ArrayList<String> SupportedTypes = new ArrayList<>(
            Arrays.asList("str", "int", "datetime"));

    private static Imabw  app    = Imabw.getInstance();
    private static Worker worker = app.getWorker();

    private JPanel              contentPane;
    private JButton             buttonClose;
    private JButton             buttonOpen;
    private JButton             buttonRemove;
    private JButton             buttonSave;
    private JButton             buttonPlus;
    private JButton             buttonMinus;
    private JButton             buttonModify;
    private AttributeTableModel tableModel;
    private JTable              propertyTable;
    private JPanel              introPane;
    private JPanel              coverPane;
    private JLabel              labelCover;
    private JLabel              coverInfo;
    private JPanel              attributesPane;
    private JButton             buttonExport;
    private ITextEdit           introEdit;

    private Part part;
    private boolean modified = false;

    public PartPropertiesDialog(Frame owner, String title, Part part) {
        super(owner, title, true);
        this.part = part;
        initComp();
    }

    public PartPropertiesDialog(Dialog owner, String title, Part part) {
        super(owner, title, true);
        this.part = part;
        initComp();
    }

    private void initComp() {
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonClose);

        // cover
        createCoverPane();

        // attributes
        createAttributesPane();

        // intro
        createIntroPane();

        Action closeAction = new IAction("Dialog.Properties.ButtonClose", app) {
            @Override
            public void actionPerformed(ActionEvent e) {
                onClose();
            }
        };
        buttonClose.setAction(closeAction);

        // call onClose() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onClose();
            }
        });

        // call onClose() on ESCAPE
        contentPane.registerKeyboardAction(closeAction,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        if (oldSize != null) {
            setSize(oldSize);
            setLocation(oldLocation);
        } else {
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            setSize((int) (d.getWidth() * 0.7), (int) (d.getWidth() * 0.4)); // 16x9
            setLocationRelativeTo(getOwner());
        }
    }

    private void createCoverPane() {
        ((TitledBorder) coverPane.getBorder()).setTitle(app.getText("Dialog.Properties.Cover"));
        Action action = new IAction("Dialog.Properties.ButtonOpen", app) {
            @Override
            public void actionPerformed(ActionEvent e) {
                openCover();
            }
        };
        buttonOpen.setAction(action);

        action = new IAction("Dialog.Properties.ButtonSave", app) {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveCover();
            }
        };
        buttonSave.setAction(action);

        action = new IAction("Dialog.Properties.ButtonRemove", app) {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeCover();
            }
        };
        buttonRemove.setAction(action);

        Object o = part.getAttribute(Book.COVER);
        if (o instanceof FileObject) {
            FileObject fb = (FileObject) o;
            ImageIcon cover = loadCover(fb);
            updateCover(cover, fb.getMime());
        } else {
            updateCover(null, null);
        }
    }

    private void createAttributesPane() {
        ((TitledBorder) attributesPane.getBorder()).setTitle(app.getText(
                "Dialog.Properties.Attributes"));

        tableModel = new AttributeTableModel();
        propertyTable.setModel(tableModel);
        propertyTable.setRowHeight(23);
        propertyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        ActionListener addAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addAttribute();
            }
        };
        ActionListener removeAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeAttribute();
            }
        };
        ActionListener modifyAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modifyAttribute();
            }
        };
        ActionListener exportAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportAttribute();
            }
        };
        buttonPlus.setToolTipText(app.getText("Dialog.Properties.ButtonPlus.Tip"));
        buttonPlus.addActionListener(addAction);

        buttonMinus.setToolTipText(app.getText("Dialog.Properties.ButtonNimus.Tip"));
        buttonMinus.addActionListener(removeAction);

        buttonModify.setToolTipText(app.getText("Dialog.Properties.ButtonModify.Tip"));
        buttonModify.addActionListener(modifyAction);

        buttonExport.setToolTipText(app.getText("Dialog.Properties.ButtonExport.Tip"));
        buttonExport.addActionListener(exportAction);

        if (tableModel.getRowCount() > 0) {
            propertyTable.setRowSelectionInterval(0, 0);
        } else {        // no item
            buttonMinus.setEnabled(false);
            buttonModify.setEnabled(false);
            buttonExport.setEnabled(false);
        }
        propertyTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    modifyAttribute();
                }
            }
        });
        propertyTable.registerKeyboardAction(removeAction,
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        propertyTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int row = propertyTable.getSelectedRow();
                if (row == -1) {
                    return;
                }
                String name = tableModel.getName(row);
                Object o = part.getAttribute(name);
                if (o instanceof FileObject) {
                    buttonExport.setEnabled(true);
                } else {
                    buttonExport.setEnabled(false);
                }
            }
        });
    }

    private void createIntroPane() {
        Object o = part.getAttribute(Book.INTRO);
        // load intro in this section to prohibit undo manager works when using setText
        String intro = "";
        if (o instanceof TextObject) {      // valid intro
            try {
                intro = ((TextObject) o).getText();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        introEdit = new ITextEdit(intro, 6, 0, null);
        introEdit.getTextEditor().setWrapStyleWord(true);
        introEdit.getTextEditor().setLineWrap(true);
        introEdit.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                modified = true;
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                modified = true;
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });
        ((TitledBorder) introPane.getBorder()).setTitle(app.getText("Dialog.Properties.Intro"));
        introPane.add(introEdit, BorderLayout.CENTER);
    }

    private void updateCover(ImageIcon cover, String mime) {
        if (cover == null) {
            labelCover.setText(app.getText("Dialog.Properties.NoCover"));
            labelCover.setIcon(null);
            setCoverInfo(-1, -1, null);
            buttonSave.setEnabled(false);
            buttonRemove.setEnabled(false);
        } else {
            labelCover.setText(null);
            labelCover.setIcon(cover);
            setCoverInfo(cover.getIconWidth(), cover.getIconHeight(), mime);
            buttonSave.setEnabled(true);
            buttonRemove.setEnabled(true);
        }
    }

    private ImageIcon loadCover(FileObject fb) {
        ImageIcon icon = null;
        try {
            BufferedImage img = ImageIO.read(fb.openInputStream());
            icon = new ImageIcon(img);
        } catch (Exception e) {
            LOG.debug("cannot load cover image of "+part.getTitle(), e);
        }
        return icon;
    }

    private void setCoverInfo(int width, int height, String mime) {
        if (width < 0) {
            coverInfo.setVisible(false);
        } else {
            coverInfo.setVisible(true);
            coverInfo.setText(app.getText("Dialog.Properties.CoverDetails", width, height, mime));
        }
    }

    private void openCover() {
        File file = worker.selectOpenImage(this, app.getText("Dialog.Properties.OpenCover"));
        if (file == null) {
            return;
        }
        FileObject fb = null;
        try {
            fb = FileFactory.fromFile(file, null);
        } catch (IOException e) {
            e.printStackTrace();    // nothing
        }
        if (fb == null) {
            return;
        }
        ImageIcon cover = loadCover(fb);
        if (cover == null) {
            worker.showError(this, app.getText("Dialog.Properties.OpenCover"),
                    app.getText("Dialog.Properties.InvalidCover", file.getPath()));
            return;
        }
        updateCover(cover, fb.getMime());
        part.setAttribute(Book.COVER, fb);
        modified = true;
    }

    private void saveCover() {
        File file = worker.selectSaveImage(this, app.getText("Dialog.Properties.SaveCover"));
        if (file == null) {
            return;
        }
        ImageIcon cover = (ImageIcon) labelCover.getIcon();
        BufferedImage img = (BufferedImage) cover.getImage();
        try {
            ImageIO.write(img, worker.getSelectedFormat(), file);
            worker.showError(this, app.getText("Dialog.Properties.SaveCover"),
                    app.getText("Dialog.Properties.SaveCover.Success", file));
        } catch (IOException e) {
            LOG.debug("cannot save cover of "+part.getTitle(), e);
            worker.showError(this, app.getText("Dialog.Properties.SaveCover"),
                    app.getText("Dialog.Properties.SaveCover.Error"));
        }
    }

    private void removeCover() {
        part.setAttribute(Book.COVER, null);
        updateCover(null, null);
        modified = true;
    }

    // translate readable attribute name
    private String transAttributeName(String name) {
        name = StringUtils.toCapital(name);
        try {
            return app.getText("Dialog.Properties.Attributes.Name." + name);
        } catch (MissingResourceException e) {
            System.out.println("unknown attribute name: "+e.getKey());
            return name;
        }
    }

    private String getTypeName(String type) {
        type = StringUtils.toCapital(type);
        try {
            return app.getText("Dialog.Properties.Attributes.Type." + type);
        } catch (MissingResourceException e) {
            System.out.println("Unknown attribute type: "+e.getKey());
            return type;
        }
    }

    // translate readable attribute type
    private String transAttributeType(String name) {
        Object o = part.getAttribute(name);
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
        Object o = part.getAttribute(name);
        if (o == null) {
            return "";
        }
        if (o instanceof Date) {
            return DateUtils.formatDate((Date)o,
                    app.getText("Dialog.Properties.Attributes.DateFormat"));
        } else if (o instanceof TextObject) {
            try {
                String str = ((TextObject)o).getText();
                return str.substring(0, Math.min(str.length(), 40));
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }
        } else if (name.equals(Book.LANGUAGE) && o instanceof String) {
            String str = ((String) o).replace('_', '-');
            return Locale.forLanguageTag(str).getDisplayName();
        } else if (o instanceof byte[]) {
            String str = Arrays.toString((byte[]) o);
            return str.substring(1, str.length()-1);
        }
        return String.valueOf(o);
    }

    private void addAttribute() {
        ArrayList<String> keys = new ArrayList<>();
        if (part instanceof Book) {
            keys.addAll(new ArrayList<>(CommonNames));
        }
        keys.removeAll(part.attributeNames());
        ArrayList<String> names = new ArrayList<>();
        for (String key: keys) {
            names.add(transAttributeName(key));
        }
        names.add(app.getText("Dialog.Properties.Attributes.Add.CustomerName"));

        ArrayList<String> types = new ArrayList<>();
        for (String type: SupportedTypes) {
            types.add(getTypeName(type));
        }

        NewAttributePane pane = new NewAttributePane(names, types);
        String title = app.getText("Dialog.Properties.Attributes.Add.Title");
        int r = JOptionPane.showOptionDialog(this, pane.getPane(), title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, null, null);
        if (r != JOptionPane.OK_OPTION) {
            return;
        }
        String key, type;
        if (pane.isCustomized()) {
            key = pane.getInput();
            if (key.length() == 0) {
                worker.showError(this, title,
                        app.getText("Dialog.Properties.Attributes.Add.NoInputName"));
                return;
            }
            type = SupportedTypes.get(pane.getType());
        } else {
            key = keys.get(pane.getName());
            if (key.equals(Book.DATE)) {
                type = "datetime";
            } else {
                type = "str";
            }
        }

        Object value;
        switch (type) {
            case "str":
                if (key.equals(Book.LANGUAGE)) {
                    value = "zh_CN";
                } else {
                    value = "";
                }
                break;
            case "int":
                value = 0;
                break;
            case "datetime":
                value = new Date();
                break;
            default:
                value = null;
                break;
        }
        if (! part.hasAttribute(key)) {
            part.setAttribute(key, value);
            tableModel.addName(key);
            int rows = tableModel.getRowCount();
            propertyTable.setRowSelectionInterval(rows-1, rows-1);

            if (rows == 1) {        // firstly added
                buttonMinus.setEnabled(true);
                buttonModify.setEnabled(true);
            }
        }
    }

    private void removeAttribute() {
        int row = propertyTable.getSelectedRow();
        if (row == -1) {    // no selection
            return;
        }

        String name = tableModel.getName(row);
        if (! worker.showConfirm(this,
                app.getText("Dialog.Properties.Attributes.Remove.Title"),
                app.getText("Dialog.Properties.Attributes.Remove.Tip",
                        transAttributeName(name)))) {
            return;
        }
        part.removeAttribute(name);
        tableModel.removeRow(row);
        modified = true;

        int rows = tableModel.getRowCount();
        if (rows == 0) {
            buttonMinus.setEnabled(false);
            buttonModify.setEnabled(false);
            buttonExport.setEnabled(false);
        } else {
            if (row == rows) {
                propertyTable.setRowSelectionInterval(rows - 1, rows - 1);
            } else {
                propertyTable.setRowSelectionInterval(row, row);
            }
        }
    }

    private void modifyAttribute() {
        int row = propertyTable.getSelectedRow();
        if (row == -1) {    // no selection
            return;
        }

        String name = tableModel.getName(row);
        Object o = part.getAttribute(name), newValue;
        String title = app.getText("Dialog.Properties.Attributes.Modify",
                transAttributeName(name));
        if (o instanceof Date) {
            newValue = worker.selectDate(this, title,
                    app.getText("Dialog.SelectDate.Tip"), (Date) o);
            if (newValue == null) {
                return;
            }
        } else if (o instanceof FileObject) {
            File file = worker.selectSupportedFile(this,
                    app.getText("Dialog.Properties.Attributes.Modify.SelectFile",
                            transAttributeName(name)));
            if (file == null) {
                return;
            }
            try {
                newValue = FileFactory.fromFile(file, null);
            } catch (IOException e) {       // nothing
                e.printStackTrace();
                return;
            }
        } else if (o instanceof TextObject) {   // long text
            String text;
            try {
                text = ((TextObject)o).getText();
            } catch (IOException e) {
                e.printStackTrace();
                text = "";
            }
            text = worker.longInput(this, title, app.getText("Dialog.InputText.Tip"), text);
            if (text == null) {
                return;
            }
            newValue = new TextObject(text);
        } else if (o instanceof String) {
            if (name.equals(Book.LANGUAGE)) {   // language
                String lang = (String) o;
                newValue = worker.selectLanguage(this, title,
                        app.getText("Dialog.SelectLanguage.Tip"),
                        lang.length() != 0 ? lang : null);
                if (newValue == null) {
                    return;
                }
            } else {        // other
                newValue = worker.inputLoop(this, title,
                        app.getText("Dialog.InputText.Tip"),
                        app.getText("Dialog.InputText.NoInput"), (String)o);
                if (newValue == null) {
                    return;
                }
            }
        } else if (o instanceof Number) {
            String str = worker.inputLoop(this, title,
                    app.getText("Dialog.InputNumber.Tip"),
                    app.getText("Dialog.InputText.NoInput"), String.valueOf(o));
            if (str == null) {
                return;
            }
            try {
                newValue = NumberFormat.getInstance().parse(str);
            } catch (ParseException e) {
                worker.showError(this, title,
                        app.getText("Dialog.Properties.Attributes.Modify.InvalidNumber", str));
                return;
            }
        } else {
            return;
        }
        part.setAttribute(name, newValue);
        tableModel.setValueAt(newValue, row, 2);
        modified = true;
    }

    private void exportAttribute() {
        int row = propertyTable.getSelectedRow();
        if (row == -1) {    // no selection
            return;
        }

        String name = tableModel.getName(row);
        Object o = part.getAttribute(name);
        String title = app.getText("Dialog.Properties.Attributes.Export",
                transAttributeName(name));
        File file = worker.selectSaveFile(this, title, null, null, true, null);
        if (file == null) {
            return;
        }
        FileObject fb = (FileObject) o;
        OutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(file));
            fb.copyTo(out);
            worker.showMessage(this, title,
                    app.getText("Dialog.Properties.Attributes.Export.Success",
                            file.getPath()));
        } catch (IOException e) {
            LOG.debug("cannot save attribute "+name+" to "+file.getPath(), e);
            worker.showMessage(this, title,
                    app.getText("Dialog.Properties.Attributes.Export.Failed",
                            file.getPath()));
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    LOG.debug("cannot close file "+file.getPath(), e);
                }
            }
        }
    }

    private void onClose() {
        ITextEdit.updateContextMenu(null);
        if (modified) {
            part.setAttribute(Book.INTRO, new TextObject(introEdit.getText()));
        }
        oldLocation = getLocation();
        oldSize = getSize();
        dispose();
    }

    public static boolean viewProperties(Frame owner, Part part) {
        PartPropertiesDialog dialog = new PartPropertiesDialog(owner,
                app.getText("Dialog.PartProperties.Title", part.getTitle()), part);
        dialog.setVisible(true);
        return dialog.modified;
    }

    public static boolean viewProperties(Dialog owner, Part part) {
        PartPropertiesDialog dialog = new PartPropertiesDialog(owner,
                app.getText("Dialog.PartProperties.Title", part.getTitle()), part);
        dialog.setVisible(true);
        return dialog.modified;
    }

    private class AttributeTableModel extends AbstractTableModel {
        private ArrayList<String> names = new ArrayList<>();

        public AttributeTableModel() {
            names.addAll(part.attributeNames());
            names.removeAll(IgnoredNames);
            Collections.sort(names);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
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
                    return app.getText("Dialog.Properties.Attributes.Name");
                case 1:
                    return app.getText("Dialog.Properties.Attributes.Type");
                default:
                    return app.getText("Dialog.Properties.Attributes.Value");
            }
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            String name = names.get(rowIndex);
            switch (columnIndex) {
                case 0:                 // name
                    return transAttributeName(name);
                case 1:                 // type
                    return transAttributeType(name);
                default:                // value
                    return transAttributeValue(name);
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            fireTableCellUpdated(rowIndex, columnIndex);
        }

        public void removeRow(int row) {
            names.remove(row);
            fireTableRowsDeleted(row, row);
        }

        public String getName(int row) {
            return names.get(row);
        }

        public void addName(String name) {
            names.add(name);
            fireTableRowsInserted(names.size()-1, names.size()-1);
        }
    }
}
