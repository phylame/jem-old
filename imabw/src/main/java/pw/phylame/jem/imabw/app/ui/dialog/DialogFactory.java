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

import java.awt.event.*;
import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.NumberFormatter;
import javax.swing.tree.TreePath;

import org.apache.commons.io.FilenameUtils;
import pw.phylame.gaf.ixin.IToolkit;
import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.imabw.app.Imabw;
import com.toedter.calendar.JCalendar;
import com.toedter.components.JLocaleChooser;
import pw.phylame.jem.imabw.app.Worker;
import pw.phylame.jem.imabw.app.data.FileHistory;
import pw.phylame.jem.imabw.app.data.OpenResult;

/**
 * Creates and shows some dialog.
 */
public final class DialogFactory {
    private static Imabw app = Imabw.getInstance();

    public static final int YES_OPTION    = JOptionPane.YES_OPTION;
    public static final int NO_OPTION     = JOptionPane.NO_OPTION;
    public static final int CANCEL_OPTION = JOptionPane.CANCEL_OPTION;

    private static JFileChooser fileChooser = new JFileChooser();

    static {
        String path = FileHistory.lastFile();
        if (path != null && !path.isEmpty()) {
            setCurrentDirectory(new File(path).getAbsoluteFile());
        }
    }

    public static void lafUpdated() {
        SwingUtilities.updateComponentTreeUI(fileChooser);
    }

    public static boolean showConfirm(Component parent, String title, Object message) {
        int rev = JOptionPane.showConfirmDialog(parent, message, title,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return rev == JOptionPane.YES_OPTION;
    }

    public static void showText(Component parent, String title, String text) {
        JOptionPane.showMessageDialog(parent, text, title, JOptionPane.PLAIN_MESSAGE);
    }

    public static void showMessage(Component parent, String title, Object message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showWarning(Component parent, String title, Object message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
    }

    public static void showError(Component parent, String title, Object message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public static int showAsking(Component parent, String title, Object message,
                                 String[] options, String initOption) {
        return JOptionPane.showOptionDialog(parent, message, title,
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, initOption);
    }

    public static Date selectDate(Component parent, String title,
                                  String tipText, Date initDate) {
        JCalendar calendar = new JCalendar(initDate);
        Object[] message = new Object[]{tipText, calendar};
        int rev = JOptionPane.showConfirmDialog(parent, message, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        return rev == JOptionPane.OK_OPTION ? calendar.getDate() : null;
    }

    public static String selectLocale(Component parent, String title,
                                      String tipText, String initLocale) {
        JLocaleChooser localeChooser = new JLocaleChooser();
        if (initLocale != null) {
            localeChooser.setLocale(Locale.forLanguageTag(initLocale));
        }
        Object[] message = new Object[]{tipText, localeChooser};
        int rev = JOptionPane.showConfirmDialog(parent, message, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        return rev == JOptionPane.OK_OPTION ? localeChooser.getLocale().toLanguageTag() : null;
    }

    public static Object chooseItem(Component parent, String title, String tipText,
                                    Object[] items, Object initItem, boolean editable) {
        int i = 0, index = -1;
        for (Object item : items) {
            if (item.equals(initItem)) {
                index = i;
                break;
            }
            ++i;
        }

        JComboBox<Object> comboBox = new JComboBox<>(items);
        comboBox.setEditable(editable);
        if (index != -1) {
            comboBox.setSelectedIndex(index);
        } else if (initItem != null) {
            comboBox.addItem(initItem);
            comboBox.setSelectedIndex(comboBox.getItemCount()-1);
        } else {
            comboBox.setSelectedIndex(0);
        }

        Object[] comps = {tipText, comboBox};
        int rev = JOptionPane.showConfirmDialog(parent, comps, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        return rev == JOptionPane.OK_OPTION ? items[comboBox.getSelectedIndex()] : null;
    }

    public static Window getWindowForComponent(Component comp) {
        if (comp == null) {
            return JOptionPane.getRootFrame();
        } else if (comp instanceof Frame || comp instanceof Dialog) {
            return (Window)comp;
        } else {
            return getWindowForComponent(comp.getParent());
        }
    }

    public static Object inputContent(Component parent, String title, String tipText,
                                      Object initValue, boolean requireChange, boolean canEmpty,
                                      JFormattedTextField.AbstractFormatter formatter) {
        SimpleInputDialog dialog;
        Window window = getWindowForComponent(parent);
        if (window instanceof Frame) {
            dialog = new SimpleInputDialog((Frame) window, title, tipText,
                    initValue, requireChange, canEmpty, formatter);
        } else if (window instanceof Dialog) {
            dialog = new SimpleInputDialog((Dialog) window, title, tipText,
                    initValue, requireChange, canEmpty, formatter);
        } else {
            dialog = new SimpleInputDialog((Frame) null, title, tipText,
                    initValue, requireChange, canEmpty, formatter);
        }
        dialog.setVisible(true);
        return dialog.getValue();
    }

    public static String inputText(Component parent, String title, String tipText,
                                   String initText, boolean requireChange, boolean canEmpty) {
        return (String)inputContent(parent, title ,tipText, initText, requireChange, canEmpty, null);
    }

    public static Long inputInteger(Component parent, String title, String tipText,
                                   long initValue, boolean requireChange) {
        return (Long)inputContent(parent, title, tipText, initValue, requireChange, false,
                new NumberFormatter(NumberFormat.getIntegerInstance()));
    }

    public static Number inputNumber(Component parent, String title, String tipText,
                                     Number initValue, boolean requireChange) {
        return (Number)inputContent(parent, title, tipText, initValue, requireChange, false,
                new NumberFormatter(NumberFormat.getNumberInstance()));
    }

    public static String longInput(Component parent, String title, String tipText,
                                   String initText, boolean requireChange, boolean canEmpty) {
        LongInputDialog dialog;
        Window window = getWindowForComponent(parent);
        if (window instanceof Frame) {
            dialog = new LongInputDialog((Frame)window, title, tipText, initText, requireChange, canEmpty);
        } else if (window instanceof Dialog) {
            dialog = new LongInputDialog((Dialog)window, title, tipText, initText, requireChange, canEmpty);
        } else {
            dialog = new LongInputDialog((Frame)null, title, tipText, initText, requireChange, canEmpty);
        }
        dialog.setVisible(true);
        return dialog.getText();
    }

    public static void viewException(Component parent, String title, String tipText,
                                     Throwable t) {
        ExceptionTraceDialog dialog;
        Window window = getWindowForComponent(parent);
        if (window instanceof Frame) {
            dialog = new ExceptionTraceDialog((Frame)window, title, tipText, t);
        } else if (window instanceof Dialog) {
            dialog = new ExceptionTraceDialog((Dialog)window, title, tipText, t);
        } else {
            dialog = new ExceptionTraceDialog((Frame)null, title, tipText, t);
        }
        dialog.setVisible(true);
    }

    public static void prepareFileChooser(String title, int mode, boolean multiple,
                                          File initFile, File initDir, boolean acceptAll,
                                          FileFilter[] filters, FileFilter initFilter) {
        fileChooser.setDialogTitle(title);
        fileChooser.setFileSelectionMode(mode);
        fileChooser.setMultiSelectionEnabled(multiple);
        if (initDir != null) {
            fileChooser.setCurrentDirectory(initDir);
        }
        if (initFile != null) {
            fileChooser.setSelectedFile(initFile);
        }
        // filters
        fileChooser.resetChoosableFileFilters();
        fileChooser.setAcceptAllFileFilterUsed(acceptAll);
        if (filters != null) {
            for (FileFilter filter : filters) {
                fileChooser.addChoosableFileFilter(filter);
            }
        }
        if (initFilter != null) {
            fileChooser.setFileFilter(initFilter);
        }
    }

    public static void setApproveButtonName(String name, String tipText) {
        Object[] parts = IToolkit.parseTextMnemonic(name);
        fileChooser.setApproveButtonText((String) parts[0]);
        fileChooser.setApproveButtonMnemonic((int) parts[1]);
        fileChooser.setApproveButtonToolTipText(tipText);
    }

    // transKey: translation key
    public static void setApproveButtonName(String transKey) {
        String name = app.getText(transKey), tipText = app.getText(transKey + ".Tip");
        setApproveButtonName(name, tipText);
    }

    public static void setCurrentDirectory(File dir) {
        fileChooser.setCurrentDirectory(dir);
    }

    public static OpenResult openFile(Component parent, String title,
                                    boolean multiple, File initFile, File initDir,
                                    boolean acceptAll,
                                    FileFilter[] filters, FileFilter initFilter) {
        prepareFileChooser(title, JFileChooser.FILES_ONLY, multiple,
                initFile, initDir, acceptAll, filters, initFilter);

        setApproveButtonName("Dialog.OpenFile.ApproveButton");

        if (fileChooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        File[] files;
        if (multiple) {
            files = fileChooser.getSelectedFiles();
        } else {
            files = new File[]{fileChooser.getSelectedFile()};
        }
        return new OpenResult(files, fileChooser.getFileFilter());
    }

    public static File fileWithExtension() {
        File file = fileChooser.getSelectedFile();

        if (FilenameUtils.getExtension(file.getPath()).isEmpty()) {
            // append extension by choose extension filter
            FileFilter filter = fileChooser.getFileFilter();
            if (filter instanceof FileNameExtensionFilter) {
                String[] ext = ((FileNameExtensionFilter) filter).getExtensions();
                file = new File(file.getPath() + "." + ext[0]);
            }
        }

        return file;
    }

    public static OpenResult saveFile(Component parent, String title,
                                    File initFile, File initDir,
                                    boolean askOverwrite, boolean acceptAll,
                                    FileFilter[] filters, FileFilter initFilter) {
        prepareFileChooser(title, JFileChooser.FILES_ONLY, false,
                initFile, initDir, acceptAll, filters, initFilter);

        setApproveButtonName("Dialog.SaveFile.ApproveButton");

        while (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File file = fileWithExtension();

            if (file.exists()) {
                if (askOverwrite) {
                    if (!showConfirm(parent, title,
                            app.getText("Dialog.SaveFile.Overwrite", file.getPath()))) {
                        continue;
                    }
                }
            }

            return new OpenResult(new File[]{file}, fileChooser.getFileFilter());
        }
        // cancelled
        return null;
    }

    public static void fileDetails(Component parent, File file) {
        Date date = new Date(file.lastModified());
        String str = app.getText("Dialog.FileDetails.Content", file.getName(),
                file.getAbsoluteFile().getParent(),
                FilenameUtils.getExtension(file.getPath()).toUpperCase(),
                Worker.getInstance().formatFileSize(file.length()),
                new SimpleDateFormat("yyyy-M-d H:m:s").format(date));
        showText(parent, app.getText("Dialog.FileDetails.Title"), str);
    }

    public static void aboutApp(Frame parent) {
        new AboutAppDialog(parent).setVisible(true);
    }

    public static void openSettings(Frame parent) {
        new EditSettingsDialog(parent).setVisible(true);
    }

    public static boolean editChapterAttributes(Component parent, Chapter chapter) {
        ChapterAttributeDialog dialog;
        Window window = getWindowForComponent(parent);
        if (window instanceof Frame) {
            dialog = new ChapterAttributeDialog((Frame) window, chapter);
        } else if (window instanceof Dialog) {
            dialog = new ChapterAttributeDialog((Dialog) window, chapter);
        } else {
            dialog = new ChapterAttributeDialog((Frame) null, chapter);
        }
        dialog.setVisible(true);
        return dialog.isModified();
    }

    static WaitingDialog createWaitingDialog(Component parent, String title,
                                              String tipText, String waitingText) {
        WaitingDialog dialog;
        Window window = getWindowForComponent(parent);
        if (window instanceof Frame) {
            dialog = new WaitingDialog((Frame) window, title, tipText, waitingText);
        } else if (window instanceof Dialog) {
            dialog = new WaitingDialog((Dialog) window, title, tipText, waitingText);
        } else {
            dialog = new WaitingDialog((Frame) null, title, tipText, waitingText);
        }
        return dialog;
    }

    public static void openWaiting(Component parent, String title,
                                   String tipText, String waitingText,
                                   WaitingWork work, boolean cancelable) {
        WaitingDialog dialog = createWaitingDialog(parent, title, tipText, waitingText);
        dialog.setWork(work, cancelable, true);
        work.execute();
        dialog.setVisible(true);
    }

    public static void openProgress(Component parent, String title,
                                   String tipText, String waitingText,
                                   WaitingWork work, boolean cancelable) {
        WaitingDialog dialog = createWaitingDialog(parent, title, tipText, waitingText);
        dialog.setWork(work, cancelable, false);
        work.execute();
        dialog.setVisible(true);
    }

    public static abstract class CommonDialog extends JDialog  {
        public static final int     MARGIN = 5;
        protected JButton           btnDefault = null;
        protected JPanel            buttonPane = null;

        public CommonDialog(Dialog owner, String title, boolean modal) {
            super(owner, title, modal);
        }

        public CommonDialog(Frame owner, String title, boolean modal) {
            super(owner, title, modal);
        }

        protected void init() {
            init(false);
        }

        protected void init(boolean resizable) {
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    onCancel();
                }
            });

            JPanel topPane = new JPanel(new BorderLayout());
            topPane.setBorder(BorderFactory.createEmptyBorder(MARGIN, MARGIN, MARGIN, MARGIN));

            // press escape to cancel input
            topPane.registerKeyboardAction(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            onCancel();
                        }
                    },
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                    JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

            createComponents(topPane);

            if (buttonPane != null) {
                topPane.add(buttonPane, BorderLayout.PAGE_END);
            }

            if (btnDefault != null) {
                getRootPane().setDefaultButton(btnDefault);
            }

            setContentPane(topPane);
            pack();
            setResizable(resizable);

            setLocationRelativeTo(getOwner());
        }

        // create buttons and others
        protected abstract void createComponents(JPanel topPane);

        protected abstract void onCancel();

        protected JPanel makeButtonPane(int alignment, JButton... buttons) {
            JPanel pane = new JPanel();
            pane.setLayout(new BoxLayout(pane, BoxLayout.LINE_AXIS));
            pane.setBorder(BorderFactory.createEmptyBorder(MARGIN, 0, 0, 0));

            if (alignment != SwingConstants.LEFT) {
                pane.add(Box.createHorizontalGlue());
            }

            int i = 0;
            for (JButton button : buttons) {
                pane.add(button);
                if (++i != buttons.length) {    // not the last
                    pane.add(Box.createRigidArea(new Dimension(5, 0)));
                }
            }
            if (alignment != SwingConstants.RIGHT) {
                pane.add(Box.createHorizontalGlue());
            }
            return pane;
        }
    }
}

