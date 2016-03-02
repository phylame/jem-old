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
import pw.phylame.imabw.app.model.OpenResult;
import pw.phylame.imabw.app.ui.UISnap;
import pw.phylame.imabw.app.ui.com.ItemTable;
import pw.phylame.imabw.app.util.BookUtils;
import pw.phylame.jem.core.Jem;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.formats.util.text.TextUtils;
import pw.phylame.jem.util.FileFactory;
import pw.phylame.jem.util.FileObject;
import pw.phylame.jem.util.TextFactory;
import pw.phylame.jem.util.TextObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.Frame;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import static pw.phylame.imabw.app.ui.dialog.DialogFactory.*;

class ChapterAttributes extends CommonDialog {
    private static final Imabw app = Imabw.sharedInstance();
    private static final Worker worker = Worker.sharedInstance();

    private static final String V_DIVIDER_LOCATION = "attributes.vDivider.location";
    private static final String H_DIVIDER_LOCATION = "attributes.hDivider.location";
    private static final String DIALOG_SIZE = "attributes.size";

    public static final String[] chapterKeys = {Chapter.WORDS, "customize"};

    public static final String[] bookKeys = {
            Chapter.AUTHOR, Chapter.DATE, Chapter.GENRE, Chapter.LANGUAGE,
            Chapter.PUBLISHER, Chapter.RIGHTS, Chapter.STATE,
            Chapter.SUBJECT, "source", Chapter.VENDOR, Chapter.WORDS, "customize"
    };

    public static final Set<String> ignoredKeys = new HashSet<>();

    static {
        Collections.addAll(ignoredKeys, Chapter.TITLE, Chapter.COVER, Chapter.INTRO);
    }

    private Chapter chapter;
    private Map<String, Object> attributes = new HashMap<>();

    private JLabel lbCoverImage, lbCoverDetails;
    private JButton btnSaveCover, btnRemoveCover;

    private AttributesTable table;

    private JTextArea taIntro;

    private JSplitPane hSplitPane, vSplitPane;

    private JButton btnSave;

    private boolean modified = false;

    public ChapterAttributes(Frame owner, String title, boolean modal) {
        super(owner, title, modal);
    }

    public ChapterAttributes(Dialog owner, String title, boolean modal) {
        super(owner, title, modal);
    }

    void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    @Override
    protected void createComponents(JPanel userPane) {
        controlsPane = createButtonPane();

        UISnap snap = UISnap.sharedInstance();

        vSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                createTablePane(), createIntroPane());
        vSplitPane.setDividerSize(7);
        vSplitPane.setDividerLocation(snap.getInteger(V_DIVIDER_LOCATION, 254));

        hSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                createCoverPane(), vSplitPane);
        hSplitPane.setDividerSize(7);
        hSplitPane.setDividerLocation(snap.getInteger(H_DIVIDER_LOCATION, 256));

        userPane.add(hSplitPane, BorderLayout.CENTER);

        setPreferredSize(snap.getDimension(DIALOG_SIZE, new Dimension(878, 514)));
    }

    @Override
    public Object makeShow(boolean resizable) {
        initialize(resizable);
        reset();
        setVisible(true);
        return getResult();
    }

    private JPanel createCoverPane() {
        lbCoverImage = new JLabel();
        lbCoverImage.setVerticalAlignment(JLabel.CENTER);
        lbCoverImage.setHorizontalAlignment(JLabel.CENTER);

        lbCoverDetails = new JLabel(null, null, JLabel.CENTER);
        lbCoverDetails.setBorder(BorderFactory.createEmptyBorder(1, 0, 2, 0));

        JButton btnOpen = new JButton(new IAction("attributes.openCover") {
            @Override
            public void actionPerformed(ActionEvent e) {
                openCover();
            }
        });
        btnSaveCover = new JButton(new IAction("attributes.saveCover") {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveCover();
            }
        });
        btnRemoveCover = new JButton(new IAction("attributes.removeCover") {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeCover();
            }
        });

        updateCover(null, null);

        JPanel detailsPane = new JPanel(new BorderLayout());
        detailsPane.add(lbCoverDetails, BorderLayout.CENTER);
        detailsPane.add(new JSeparator(), BorderLayout.PAGE_END);

        JPanel coverPane = new JPanel(new BorderLayout());
        coverPane.setPreferredSize(new Dimension(260, 500));
        coverPane.add(new JScrollPane(lbCoverImage), BorderLayout.CENTER);
        coverPane.add(detailsPane, BorderLayout.PAGE_END);

        JPanel buttonPane = new JPanel(new FlowLayout());
        buttonPane.add(btnOpen);
        buttonPane.add(btnSaveCover);
        buttonPane.add(btnRemoveCover);

        JPanel pane = new JPanel(new BorderLayout());
        IxinUtilities.localizedTitleBorder(pane, "attributes.cover.title", app);
        pane.add(coverPane, BorderLayout.CENTER);
        pane.add(buttonPane, BorderLayout.PAGE_END);

        return pane;
    }

    private void openCover() {
        String title = app.getText("attributes.openCover.title");
        OpenResult od = worker.selectOpenImage(this, title);
        if (od == null) {
            return;
        }
        FileObject fb;
        try {
            fb = FileFactory.forFile(od.getFile(), null);
        } catch (IOException e) {
            throw new AssertionError("BUG: unexpected IOException here");
        }
        ImageIcon cover = loadCover(fb, title, true);
        if (cover == null) {
            return;
        }
        updateCover(cover, fb.getMime());
        attributes.put(Chapter.COVER, fb);
        fireModified();
    }

    private void saveCover() {
        String title = app.getText("attributes.saveCover.title");
        OpenResult od = worker.selectSaveImage(this, title);
        if (od == null) {
            return;
        }
        File file = od.getFile();
        FileObject fb = BookUtils.getVariant(attributes, Chapter.COVER, null, FileObject.class);
        ImageIcon cover = loadCover(fb, title, true);
        BufferedImage image = (BufferedImage) cover.getImage();
        try {
            ImageIO.write(image, od.getFormat(), file);
            localizedInformation(this, title,
                    "attributes.saveCover.result", file.getPath());
        } catch (IOException e) {
            localizedException(this, title, e,
                    "attributes.saveCover.error", file.getPath(), e.getLocalizedMessage());
        }
    }

    private void removeCover() {
        attributes.remove(Chapter.COVER);
        updateCover(null, null);
        fireModified();
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
            lbCoverImage.setText(app.getText("attributes.cover.alt"));
            lbCoverDetails.setVisible(false);
            btnSaveCover.setEnabled(false);
            btnRemoveCover.setEnabled(false);
        } else {
            Dimension size = new Dimension((int) (getWidth() * 0.33), (int) (getHeight() * 0.75));
            lbCoverImage.setIcon(scaleImageIcon(cover, size));
            lbCoverImage.setText(null);
            lbCoverDetails.setText(app.getText("attributes.cover.details",
                    cover.getIconWidth(), cover.getIconHeight(), mime));
            lbCoverDetails.setVisible(true);
            btnSaveCover.setEnabled(true);
            btnRemoveCover.setEnabled(true);
        }
    }

    private ImageIcon loadCover(FileObject fb, String title, boolean notifyUser) {
        ImageIcon cover = null;

        try (InputStream in = fb.openStream()) {
            BufferedImage image = ImageIO.read(in);
            if (image != null) {
                cover = new ImageIcon(image);
            } else {
                localizedError(this, title,
                        "attributes.invalidCover", fb.getName());
            }
        } catch (IOException e) {
            if (notifyUser) {
                localizedException(this, title, e,
                        "attributes.openCover.error", fb.getName(), e.getLocalizedMessage());
            } else {
                app.error(e, "cannot load cover image of " + chapter.getTitle());
            }
        }
        return cover;
    }

    private JPanel createTablePane() {
        table = new AttributesTable();
        return table;
    }

    private JComponent createIntroPane() {
        taIntro = new JTextArea(6, 0);
        taIntro.setLineWrap(true);
        taIntro.setWrapStyleWord(true);
        taIntro.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                fireModified();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                fireModified();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                fireModified();
            }
        });
        JScrollPane scrollPane = new JScrollPane(taIntro);
        IxinUtilities.localizedTitleBorder(scrollPane, "attributes.intro.title", app);
        return scrollPane;
    }

    private JPanel createButtonPane() {
        JButton btnReset = new JButton(new IAction("attributes.buttonReset") {
            @Override
            public void actionPerformed(ActionEvent e) {
                reset();
            }
        });

        btnSave = new JButton(new IAction("attributes.buttonSave") {
            @Override
            public void actionPerformed(ActionEvent e) {
                onOk();
            }
        });
        btnSave.setEnabled(false);

        defaultButton = createCloseButton("attributes.buttonCancel");
        return createControlsPane(SwingConstants.RIGHT, btnReset, btnSave, defaultButton);
    }

    private void reset() {
        attributes = BookUtils.dumpAttributes(chapter);
        table.resetAll(attributes, ignoredKeys);

        // cover image
        ImageIcon img = null;
        String mime = null;
        FileObject cover = chapter.getCover();
        if (cover != null) {
            img = loadCover(cover, null, false);
            mime = cover.getMime();
        }
        updateCover(img, mime);

        // intro text
        TextObject intro = chapter.getIntro();
        if (intro != null) {
            taIntro.setText(TextUtils.fetchText(intro, ""));
            taIntro.setCaretPosition(0);
        }

        modified = false;
        btnSave.setEnabled(false);
    }

    private void fireModified() {
        modified = true;
        btnSave.setEnabled(true);
    }

    private void syncToChapter() {
        attributes.put(Chapter.INTRO, TextFactory.forString(taIntro.getText()));
        app.getForm().getContentsTree().updateChapterAttributes(chapter, attributes, true,
                app.getText("undo.message.editAttributes"));
        app.localizedMessage("attributes.result", chapter);
    }

    private void onOk() {
        destroy();
        if (modified) {
            syncToChapter();
        }
    }

    @Override
    protected void onCancel() {
        if (modified) {
            int option = localizedAsking(this, getTitle(),
                    MessageDialog.IconStyle.Question,
                    "attributes.askQuit", chapter);
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
        UISnap snap = UISnap.sharedInstance();
        snap.setDimension(DIALOG_SIZE, getSize());
        snap.setInteger(H_DIVIDER_LOCATION, hSplitPane.getDividerLocation());
        snap.setInteger(V_DIVIDER_LOCATION, vSplitPane.getDividerLocation());
        dispose();
    }

    private class AttributesTable extends ItemTable {
        public final HashSet<String> selectableNames = new HashSet<>();
        private final int nameColumn = 1;

        AttributesTable() {
            super(4);
            typeColumn = 2;
            valueColumn = 3;
            Collections.addAll(selectableNames, Chapter.STATE, Chapter.GENRE);
            createComponents(app.getText("attributes.table.title"), null);
        }

        @Override
        protected String nameOfColumn(int column) {
            if (column == nameColumn) {
                return app.getText("common.table.field.name");
            } else {
                return super.nameOfColumn(column);
            }
        }

        @Override
        protected Object valueOfCell(String key, int column) {
            if (column == nameColumn) {
                return nameOfKey(key);
            } else {
                return super.valueOfCell(key, column);
            }
        }

        @Override
        protected String[] supportedKeys() {
            ArrayList<String> names = new ArrayList<>();
            Collections.addAll(names, (chapter instanceof Book) ? bookKeys : chapterKeys);
            names.removeAll(super.getKeys());
            return names.toArray(new String[names.size()]);
        }

        @Override
        protected String nameOfKey(String key) {
            return BookUtils.nameOfAttribute(key);
        }

        @Override
        protected String typeOfKey(String key) {
            return Jem.typeOfAttribute(key);
        }

        @Override
        protected Object defaultForKey(String key) {
            return BookUtils.defaultOfAttribute(key);
        }

        @Override
        protected String getModifyTitle(String key) {
            return app.getText("attributes.modify.title", nameOfKey(key));
        }

        @Override
        protected Object modifyValue(String key, Object oldValue) {
            switch (key) {
                case Chapter.GENRE:
                    return BookUtils.chooseGenre(ChapterAttributes.this,
                            getModifyTitle(key), (String) oldValue);
                case Chapter.STATE:
                    return BookUtils.chooseState(ChapterAttributes.this,
                            getModifyTitle(key), (String) oldValue);
                default:
                    return super.modifyValue(key, oldValue);
            }
        }

        @Override
        public void onModified() {
            fireModified();
        }
    }
}
