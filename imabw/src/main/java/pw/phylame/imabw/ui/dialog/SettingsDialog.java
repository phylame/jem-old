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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import pw.phylame.pat.ixin.IAction;
import pw.phylame.pat.ixin.IToolkit;
import pw.phylame.imabw.Config;
import pw.phylame.imabw.Imabw;
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
    private JCheckBox         cDecorateTitle;

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
            setSize((int) (getWidth() * 1.4), (int) (getHeight() * 1.2));
            setLocationRelativeTo(getOwner());
        }
    }

    private String[] loadSupportedLanguages() {
        InputStream in = SettingsDialog.class.getResourceAsStream("/res/i18n/all.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        ArrayList<String> lang = new ArrayList<>();
        String line;
        try {
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.length() != 0 && ! line.startsWith("#")) {
                    lang.add(line.trim());
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return lang.toArray(new String[0]);
    }

    private void initLanguages() {
        final String[] languages = loadSupportedLanguages();

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
        Object[] parts =  IToolkit.parseTextMnemonic(
                app.getText("Dialog.Settings.General.Title"));

        tabbedPane.setTitleAt(0, (String) parts[0]);
        tabbedPane.setMnemonicAt(0, (int) parts[1]);

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
        lbEditorFontDemo.setFont(font);
        lbEditorFontDemo.setText(Config.toString(font));
        // todo update editors
    }

    public void initEditor() {
        Object[] parts =  IToolkit.parseTextMnemonic(
                app.getText("Dialog.Settings.Editor.Title"));

        tabbedPane.setTitleAt(1, (String) parts[0]);
        tabbedPane.setMnemonicAt(1, (int) parts[1]);

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

        Action action = new IAction(null, "Dialog.Settings.Editor.Style.LabelLineWarp", app) {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean enable = cEditorStyleLW.isSelected();
                app.getConfig().setEditorLineWarp(enable);
                // todo update editors
            }
        };
        cEditorStyleLW.setAction(action);

        action = new IAction(null, "Dialog.Settings.Editor.Style.LabelWordWarp", app) {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean enable = cEditorStyleWW.isSelected();
                app.getConfig().setEditorWordWarp(enable);
                // todo update editors
            }
        };
        cEditorStyleWW.setAction(action);

        action = new IAction(null, "Dialog.Settings.Editor.Style.LabelLineNumber", app) {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean enable = cEditorStyleLN.isSelected();
                app.getConfig().setEditorShowLineNumber(enable);
                // todo update editors
            }
        };
        cEditorStyleLN.setAction(action);

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

        Action action = new IAction(null, "Dialog.Settings.Face.Window.ShowToolbar", app) {
            @Override
            public void actionPerformed(ActionEvent e) {
                app.onCommand(Imabw.SHOW_TOOLBAR);
                cLockToolbar.setEnabled(cShowToolbar.isSelected());
            }
        };
        cShowToolbar.setAction(action);

        action = new IAction(null, "Dialog.Settings.Face.Window.LockToolbar", app) {
            @Override
            public void actionPerformed(ActionEvent e) {
                app.onCommand(Imabw.LOCK_TOOLBAR);
            }
        };
        cLockToolbar.setAction(action);

        action = new IAction(null, "Dialog.Settings.Face.Window.ShowSidebar", app) {
            @Override
            public void actionPerformed(ActionEvent e) {
                app.onCommand(Imabw.SHOW_SIDEBAR);
            }
        };
        cShowSidebar.setAction(action);

        action = new IAction(null, "Dialog.Settings.Face.Window.ShowStatusbar", app) {
            @Override
            public void actionPerformed(ActionEvent e) {
                app.onCommand(Imabw.SHOW_STATUSBAR);
            }
        };
        cShowStatusbar.setAction(action);

        action = new IAction(null, "Dialog.Settings.Face.Window.DecorateTitle", app) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame.setDefaultLookAndFeelDecorated(cDecorateTitle.isSelected());
                JDialog.setDefaultLookAndFeelDecorated(cDecorateTitle.isSelected());
                app.getConfig().setDecoratedFrame(cDecorateTitle.isSelected());
            }
        };
        cDecorateTitle.setAction(action);
    }

    private void initFace() {
        Object[] parts =  IToolkit.parseTextMnemonic(
                app.getText("Dialog.Settings.Face.Title"));

        tabbedPane.setTitleAt(2, (String) parts[0]);
        tabbedPane.setMnemonicAt(2, (int) parts[1]);

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
        cDecorateTitle.setSelected(app.getConfig().isDecoratedFrame());
    }

    private void initJem() {
        Object[] parts =  IToolkit.parseTextMnemonic(
                app.getText("Dialog.Settings.Jem.Title"));

        tabbedPane.setTitleAt(3, (String) parts[0]);
        tabbedPane.setMnemonicAt(3, (int) parts[1]);

        tabbedPane.removeTabAt(3);
    }

    private void updateSettings() {
        updateGeneral();
        updateEditor();
        updateFace();
    }

    private void initButtons() {
        Action resetAction = new IAction(null, "Dialog.Settings.ButtonReset", app) {
            @Override
            public void actionPerformed(ActionEvent e) {
                app.getConfig().reset();
                updateSettings();
            }
        };
        buttonReset.setAction(resetAction);

        Action closeAction = new IAction(null, "Dialog.Settings.ButtonClose", app) {
            @Override
            public void actionPerformed(ActionEvent e) {
                onClose();
            }
        };
        buttonClose.setAction(closeAction);

        getRootPane().setDefaultButton(buttonClose);

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onClose();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(closeAction,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

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
