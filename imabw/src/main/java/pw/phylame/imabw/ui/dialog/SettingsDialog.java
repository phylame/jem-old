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
import java.util.HashSet;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import pw.phylame.imabw.Config;
import pw.phylame.imabw.Imabw;
import pw.phylame.imabw.ui.Viewer;
import say.swing.JFontChooser;

public class SettingsDialog extends JDialog {
    private JPanel            contentPane;
    private JButton           buttonClose;
    private JTabbedPane       tabbedPane;
    private JButton           buttonReset;
    private JComboBox<String> cbLanguages;
    private JComboBox<String> cbThemes;
    private JCheckBox         cShowToolbar;
    private JCheckBox         cShowStatusbar;
    private JCheckBox         cShowSidebar;
    private JRadioButton      rbPlain;
    private JRadioButton      rbBold;
    private JRadioButton      rbItalic;
    private JCheckBox         cEditorStyleLN;
    private JCheckBox         cLockToolbar;
    private JComboBox         comboBox6;
    private JComboBox         comboBox7;
    private JComboBox         comboBox8;
    private JTextField        textField1;
    private JButton           btnEditorColorFore;
    private JButton           btnEditorColorBack;
    private JButton           btnEditorColorHigh;
    private JCheckBox         cEditorStyleLW;
    private JCheckBox         cEditorStyleWW;
    private JLabel            lbEditorColorForeDemo;
    private JLabel            lbEditorColorBackDemo;
    private JLabel            lbEditorColorHighDemo;
    private JLabel            lbLanguage;
    private JPanel            jpGeneral;
    private JPanel            jpFace;
    private JPanel            jpJem;
    private JPanel            jpEditor;
    private JPanel            jpFaceTheme;
    private JLabel            lbFaceThemeStyle;
    private JButton           btnFaceGlobalFont;
    private JLabel            lbFaceGlobalFontDemo;
    private JLabel            lbFaceGlobalFont;
    private JPanel            jpFaceWindow;
    private JPanel            jpEditorFont;
    private JPanel            jpEditorColor;
    private JPanel            jpEditorStyle;
    private JLabel            lbEditorFont;
    private JLabel            lbEditorFontDemo;
    private JLabel            lbEditorColorFore;
    private JLabel            lbEditorColorBack;
    private JLabel            lbEditorColorHigh;
    private JButton           btnEditorFont;

    private static Point     oldLocation = null;
    private static Dimension oldSize     = null;

    private Imabw app = Imabw.getInstance();

    private SettingsDialog currentDialog;

    private HashSet<String> updateWorks = new HashSet<>();

    public SettingsDialog(Frame owner) {
        super(owner, true);
        currentDialog = this;
        init();
    }

    private void init() {
        setTitle(app.getText("Dialog.Settings.Title"));
        setContentPane(contentPane);

        initGeneral();
        initEditor();
        initFace();
        initJem();
        initButtons();

        updateSettings();
        updateWorks.clear();

        if (oldSize != null) {
            setSize(oldSize);
            setLocation(oldLocation);
        } else {
            pack();
            setSize((int) (getHeight() * 1.7), getHeight());
            setLocationRelativeTo(getOwner());
        }
    }

    private void initLanguages() {
        final String[] languages = {"en-US", "zh-CN"};

        for (String lang : languages) {
            Locale locale = Locale.forLanguageTag(lang);
            cbLanguages.addItem(locale.getDisplayName());
        }
        cbLanguages.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                int index = cbLanguages.getSelectedIndex();
                Locale locale = Locale.forLanguageTag(languages[index]);
                app.getConfig().setAppLocale(locale);
            }
        });
    }

    private void initGeneral() {
        tabbedPane.setTitleAt(0, app.getText("Dialog.Settings.General.Title"));

        lbLanguage.setText(app.getText("Dialog.Settings.General.LabelLanguage"));
        initLanguages();
    }

    private void updateGeneral() {
        cbLanguages.setSelectedItem(app.getConfig().getAppLocale().getDisplayName());
    }

    private void newEditorFont() {
        JFontChooser fontChooser = new JFontChooser();
        fontChooser.setSelectedFont(app.getConfig().getEditorFont());
        if (fontChooser.showDialog(currentDialog) != JFontChooser.OK_OPTION) {
            return;
        }
        Font font = fontChooser.getSelectedFont();
        app.getConfig().setEditorFont(font);
        // todo update editors
    }

    public void initEditor() {
        tabbedPane.setTitleAt(1, app.getText("Dialog.Settings.Editor.Title"));

        ((TitledBorder)jpEditorFont.getBorder()).setTitle(
                app.getText("Dialog.Settings.Editor.Font.Title"));
        lbEditorFont.setText(app.getText("Dialog.Settings.Editor.Font.LabelFont"));
        btnEditorFont.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newEditorFont();
            }
        });

        ((TitledBorder)jpEditorColor.getBorder()).setTitle(
                app.getText("Dialog.Settings.Editor.Color.Title"));
        lbEditorColorFore.setText(app.getText("Dialog.Settings.Editor.Color.LabelFore"));
        lbEditorColorForeDemo.setOpaque(true);
        lbEditorColorBack.setText(app.getText("Dialog.Settings.Editor.Color.LabelBack"));
        lbEditorColorBackDemo.setOpaque(true);
        lbEditorColorHigh.setText(app.getText("Dialog.Settings.Editor.Color.LabelHigh"));
        lbEditorColorHighDemo.setOpaque(true);

        ((TitledBorder) jpEditorStyle.getBorder()).setTitle(
                app.getText("Dialog.Settings.Editor.Style.Title"));
        cEditorStyleLW.setText(app.getText("Dialog.Settings.Editor.Style.LabelLineWarp"));
        cEditorStyleLW.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean enable = cEditorStyleLW.isSelected();
                app.getConfig().setEditorLineWarp(enable);
                // todo update editors
            }
        });
        cEditorStyleWW.setText(app.getText("Dialog.Settings.Editor.Style.LabelWordWarp"));
        cEditorStyleWW.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean enable = cEditorStyleWW.isSelected();
                app.getConfig().setEditorWordWarp(enable);
                // todo update editors
            }
        });
        cEditorStyleLN.setText(app.getText("Dialog.Settings.Editor.Style.LabelLineNumber"));
        cEditorStyleLN.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean enable = cEditorStyleLN.isSelected();
                app.getConfig().setEditorShowLineNumber(enable);
                // todo update editors
            }
        });

        btnEditorColorFore.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color color = JColorChooser.showDialog(currentDialog,
                        app.getText("Dialog.Settings.ChooseColor"),
                        app.getConfig().getEditorForeground());
                if (color != null) {
                    app.getConfig().setEditorForeground(color);
                    lbEditorColorForeDemo.setBackground(color);
                }
            }
        });

        btnEditorColorBack.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color color = JColorChooser.showDialog(currentDialog,
                        app.getText("Dialog.Settings.ChooseColor"),
                        app.getConfig().getEditorBackground());
                if (color != null) {
                    app.getConfig().setEditorBackground(color);
                    lbEditorColorBackDemo.setBackground(color);
                }
            }
        });

        btnEditorColorHigh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color color = JColorChooser.showDialog(currentDialog,
                        app.getText("Dialog.Settings.ChooseColor"),
                        app.getConfig().getEditorHighlight());
                if (color != null) {
                    app.getConfig().setEditorHighlight(color);
                    lbEditorColorHighDemo.setBackground(color);
                }
            }
        });
    }

    private void updateEditor() {
        Font font = app.getConfig().getEditorFont();
        lbEditorFontDemo.setFont(font);
        lbEditorFontDemo.setText(Config.toString(font));

        lbEditorColorForeDemo.setBackground(app.getConfig().getEditorForeground());
        lbEditorColorBackDemo.setBackground(app.getConfig().getEditorBackground());
        lbEditorColorHighDemo.setBackground(app.getConfig().getEditorHighlight());

        cEditorStyleLW.setSelected(app.getConfig().isEditorLineWarp());
        cEditorStyleWW.setSelected(app.getConfig().isEditorWordWarp());
        cEditorStyleLN.setSelected(app.getConfig().isEditorShowLineNumber());
    }

    private void newGlobalFontAction() {
        JFontChooser fontChooser = new JFontChooser();
        fontChooser.setSelectedFont(app.getConfig().getGlobalFont());
        if (fontChooser.showDialog(currentDialog) != JFontChooser.OK_OPTION) {
            return;
        }
        Font font = fontChooser.getSelectedFont();
        app.getConfig().setGlobalFont(font);
        app.setGeneralFonts(font);
        SwingUtilities.updateComponentTreeUI(currentDialog);
        SwingUtilities.updateComponentTreeUI(app.getViewer());
        updateFace();
    }

    private void initLafThemes() {
        final UIManager.LookAndFeelInfo[] feels = UIManager.getInstalledLookAndFeels();
        for (UIManager.LookAndFeelInfo laf: feels) {
            cbThemes.addItem(laf.getName());
        }
        cbThemes.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                int index = cbThemes.getSelectedIndex();
                UIManager.LookAndFeelInfo feel = feels[index];
                app.getConfig().setLafTheme(feel.getClassName());
                updateWorks.add("update_theme");
            }
        });
    }

    private void initWindow() {
        ((TitledBorder) jpFaceWindow.getBorder()).setTitle(
                app.getText("Dialog.Settings.Face.Window.Title"));
        cShowToolbar.setText(app.getText("Dialog.Settings.Face.Window.ShowToolbar"));
        cShowToolbar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                app.getViewer().showOrHideToolBar();
                app.getConfig().setShowToolbar(cShowToolbar.isSelected());
                cLockToolbar.setEnabled(cShowToolbar.isSelected());
            }
        });
        cLockToolbar.setText(app.getText("Dialog.Settings.Face.Window.LockToolbar"));
        cLockToolbar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean visible = cLockToolbar.isSelected();
                app.getViewer().setLockToolBar(visible);
                app.getConfig().setLockToolbar(visible);
            }
        });
        cShowSidebar.setText(app.getText("Dialog.Settings.Face.Window.ShowSidebar"));
        cShowSidebar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((Viewer)app.getViewer()).showOrHideSideBar();
                app.getConfig().setShowSidebar(cShowSidebar.isSelected());
            }
        });
        cShowStatusbar.setText(app.getText("Dialog.Settings.Face.Window.ShowStatusbar"));
        cShowStatusbar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                app.getViewer().showOrHideStatusBar();
                app.getConfig().settShowStatusbar(cShowStatusbar.isSelected());
            }
        });
    }

    private void initFace() {
        tabbedPane.setTitleAt(2, app.getText("Dialog.Settings.Face.Title"));

        // theme
        ((TitledBorder) jpFaceTheme.getBorder()).setTitle(
                app.getText("Dialog.Settings.Face.Theme.Title"));
        lbFaceThemeStyle.setText(app.getText("Dialog.Settings.Face.Theme.LabelStyle"));

        initLafThemes();

        lbFaceGlobalFont.setText(app.getText("Dialog.Settings.Face.Theme.LabelFont"));
        btnFaceGlobalFont.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newGlobalFontAction();
            }
        });

        initWindow();
    }

    private void updateFace() {
        String laf = app.getConfig().getLafTheme();
        int index = 0, i = 0;
        UIManager.LookAndFeelInfo[] feels = UIManager.getInstalledLookAndFeels();
        for (UIManager.LookAndFeelInfo feel: feels) {
            if (laf.equalsIgnoreCase(feel.getName()) || laf.equalsIgnoreCase(
                    feel.getClassName())) {
                index = i;
            }
            ++i;
        }
        cbThemes.setSelectedIndex(index);

        Font font = app.getConfig().getGlobalFont();
        lbFaceGlobalFontDemo.setFont(font);
        lbFaceGlobalFontDemo.setText(Config.toString(font));

        cShowToolbar.setSelected(app.getConfig().isShowToolbar());
        cLockToolbar.setEnabled(cShowToolbar.isSelected());
        cLockToolbar.setSelected(app.getConfig().isLockToolbar());
        cShowSidebar.setSelected(app.getConfig().isShowSidebar());
        cShowStatusbar.setSelected(app.getConfig().isShowStatusbar());
    }

    private void initJem() {
        tabbedPane.setTitleAt(3, app.getText("Dialog.Settings.Jem.Title"));
        tabbedPane.removeTabAt(3);
    }

    private void updateSettings() {
        updateGeneral();
        updateEditor();
        updateFace();
    }

    private void initButtons() {
        buttonReset.setText(app.getText("Dialog.ButtonReset"));
        buttonReset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                app.getConfig().reset();
                updateSettings();
            }
        });

        buttonClose.setText(app.getText("Dialog.ButtonClose"));
        buttonClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onClose();
            }
        });
        getRootPane().setDefaultButton(buttonClose);

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onClose();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onClose();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

    }

    private void dispatchWorks() {
        for (String key: updateWorks) {
            switch (key) {
                case "update_theme":
                    app.setTheme(app.getConfig().getLafTheme());
                    break;
            }
        }
    }

    private void onClose() {
        // add your code here
        oldLocation = getLocation();
        oldSize = getSize();
        dispatchWorks();
        dispose();
    }

    public static void editSettings(Frame owner) {
        SettingsDialog dialog = new SettingsDialog(owner);
        dialog.setVisible(true);
    }
}
