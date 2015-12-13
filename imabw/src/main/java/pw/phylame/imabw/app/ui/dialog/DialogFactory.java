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

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

import com.toedter.calendar.JCalendar;
import com.toedter.components.JLocaleChooser;
import org.apache.commons.io.FilenameUtils;
import pw.phylame.gaf.ixin.IAction;
import pw.phylame.gaf.ixin.ICommonDialog;
import pw.phylame.gaf.ixin.IQuietAction;
import pw.phylame.gaf.ixin.IxinUtilities;
import pw.phylame.imabw.app.Imabw;
import pw.phylame.imabw.app.Worker;
import pw.phylame.imabw.app.model.OpenResult;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.formats.util.text.TextUtils;

/**
 * Factory for creating dialogs.
 */
public final class DialogFactory {
    private static final Imabw app = Imabw.sharedInstance();

    public static JFileChooser fileChooser = new JFileChooser();

    private DialogFactory() {
    }

    public static Window getWindowForComponent(Component comp) {
        if (comp == null) {
            return JOptionPane.getRootFrame();
        } else if (comp instanceof Frame || comp instanceof Dialog) {
            return (Window) comp;
        } else {
            return getWindowForComponent(comp.getParent());
        }
    }

    private static <D extends ICommonDialog, W extends Window> D createDialog(W owner,
                                                                              String title,
                                                                              boolean modal,
                                                                              Class<D> dialogClass,
                                                                              Class<W> ownerClass) {
        try {
            Constructor<D> constructor = dialogClass.getConstructor(ownerClass,
                    String.class,
                    boolean.class);
            return constructor.newInstance(owner, title, modal);
        } catch (NoSuchMethodException | InvocationTargetException |
                InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("not found constructor for " + dialogClass, e);
        }
    }

    public static <D extends ICommonDialog> D createDialog(Component parent,
                                                           String title,
                                                           boolean modal,
                                                           Class<D> clazz) {
        Window window = getWindowForComponent(parent);
        if (window instanceof Frame) {
            return createDialog((Frame) window, title, modal, clazz, Frame.class);
        } else if (window instanceof Dialog) {
            return createDialog((Dialog) window, title, modal, clazz, Dialog.class);
        } else {
            return createDialog(null, title, modal, clazz, Frame.class);
        }
    }

    public static boolean showConfirm(boolean[] dnaa,
                                      Component parent, String title, Object message) {
        MessageDialog dialog = createDialog(parent, title, true, MessageDialog.class);
        dialog.setIconStyle(MessageDialog.IconStyle.Question);
        dialog.setMessage(message);

        if (dnaa != null && dnaa.length > 0) {
            Action action = new IQuietAction("dialog.common.checkNotAskAgain");
            JCheckBox checkBox = new JCheckBox(action);
            checkBox.setOpaque(false);
            dialog.setOptions(-1,
                    2,
                    checkBox,
                    Box.createHorizontalGlue(),
                    CommonDialog.BUTTON_OK,
                    CommonDialog.BUTTON_CANCEL);
            boolean state = dialog.showModal() == 2;
            dnaa[0] = checkBox.isSelected();
            return state;
        } else {
            dialog.setRightAlignedOptions(
                    0,
                    CommonDialog.BUTTON_OK,
                    CommonDialog.BUTTON_CANCEL);
            return dialog.showModal() == 0;
        }
    }

    public static boolean showConfirm(Component parent, String title, Object message) {
        return showConfirm(null, parent, title, message);
    }

    public static boolean localizedConfirm(Component parent, String title,
                                           String message, Object... args) {
        return showConfirm(parent, title, app.getText(message, args));
    }

    public static void showText(Component parent, String title, Object message) {
        MessageDialog dialog = createDialog(parent, title, true, MessageDialog.class);
        dialog.setMessage(message);
        dialog.setDecorationStyle(JRootPane.PLAIN_DIALOG);
        dialog.addCloseButton(SwingConstants.CENTER);
        dialog.showModal();
    }

    public static void localizedText(Component parent, String title,
                                     String text, Object... args) {
        showText(parent, title, app.getText(text, args));
    }

    public static void showInformation(Component parent, String title, Object message) {
        MessageDialog dialog = createDialog(parent, title, true, MessageDialog.class);
        dialog.setIconStyle(MessageDialog.IconStyle.Information);
        dialog.setMessage(message);
        dialog.setDecorationStyle(JRootPane.INFORMATION_DIALOG);
        dialog.addCloseButton(SwingConstants.RIGHT);
        dialog.showModal();
    }

    public static void localizedInformation(Component parent, String title,
                                            String message, Object... args) {
        showInformation(parent, title, app.getText(message, args));
    }

    public static void showWarning(Component parent, String title, Object message) {
        MessageDialog dialog = createDialog(parent, title, true, MessageDialog.class);
        dialog.setIconStyle(MessageDialog.IconStyle.Warning);
        dialog.setMessage(message);
        dialog.setDecorationStyle(JRootPane.WARNING_DIALOG);
        dialog.addCloseButton(SwingConstants.RIGHT);
        dialog.showModal();
    }

    public static void localizedWarning(Component parent, String title,
                                        String message, Object... args) {
        showWarning(parent, title, app.getText(message, args));
    }

    public static void showError(Component parent, String title, Object message) {
        MessageDialog dialog = createDialog(parent, title, true, MessageDialog.class);
        dialog.setIconStyle(MessageDialog.IconStyle.Alert);
        dialog.setMessage(message);
        dialog.setDecorationStyle(JRootPane.ERROR_DIALOG);
        dialog.addCloseButton(SwingConstants.RIGHT);
        dialog.showModal();
    }

    public static void localizedError(Component parent, String title,
                                      String message, Object... args) {
        showError(parent, title, app.getText(message, args));
    }

    // return 0: discard, 1: save, other: cancel
    public static int askSaving(Component parent, String title, Object message) {
        int option = showOptions(parent, title, message,
                MessageDialog.IconStyle.Save,
                -1, 2,
                "dialog.askSaving.buttonDiscard",
                Box.createHorizontalGlue(),
                "dialog.askSaving.buttonSave",
                "dialog.askSaving.buttonCancel");
        return option < 1 ? option : option - 1;
    }

    public static int showOptions(Component parent, String title, Object message,
                                  MessageDialog.IconStyle style,
                                  int alignment, int defaultOption, Object... options) {
        MessageDialog dialog = createDialog(parent, title, true, MessageDialog.class);
        dialog.setIconStyle(style);
        dialog.setMessage(message);
        dialog.setDecorationStyle(JRootPane.QUESTION_DIALOG);
        dialog.setOptions(alignment, defaultOption, options);
        return dialog.showModal();
    }

    public static Date selectDate(Component parent, String title,
                                  String tipText, Date initDate) {
        JCalendar calendar = new JCalendar(initDate);
        Object[] message = new Object[]{tipText, calendar};
        int choice = showOptions(parent, title, message, null, SwingConstants.RIGHT,
                0, CommonDialog.BUTTON_OK, CommonDialog.BUTTON_CANCEL);
        return choice == 0 ? calendar.getDate() : null;
    }

    public static Locale selectLocale(Component parent, String title,
                                      String tipText, Locale initLocale) {
        JLocaleChooser localeChooser = new JLocaleChooser();
        if (initLocale != null) {
            localeChooser.setLocale(initLocale);
        }
        Object[] message = new Object[]{tipText, localeChooser};

        int choice = showOptions(parent, title, message, null, SwingConstants.RIGHT,
                0, CommonDialog.BUTTON_OK, CommonDialog.BUTTON_CANCEL);
        return choice == 0 ? localeChooser.getLocale() : null;
    }

    public static String selectLocale(Component parent, String title, String tipText,
                                      String langTag) {
        Locale locale = selectLocale(parent, title, tipText, Locale.forLanguageTag(langTag));
        return locale != null ? locale.toLanguageTag() : null;
    }

    public static Object inputContent(Component parent, String title, String tipText,
                                      Object initValue, boolean requireChange, boolean canEmpty,
                                      JFormattedTextField.AbstractFormatter formatter) {
        SimpleInputDialog dialog = createDialog(parent, title, true, SimpleInputDialog.class);
        dialog.setTipText(tipText);
        dialog.setInitValue(initValue);
        dialog.setRequireChange(requireChange);
        dialog.setCanEmpty(canEmpty);
        dialog.setFormatter(formatter);
        return dialog.makeShow(false);
    }

    public static String inputText(Component parent, String title, String tipText,
                                   String initText, boolean requireChange, boolean canEmpty) {
        return (String) inputContent(parent, title, tipText, initText, requireChange, canEmpty, null);
    }

    public static Long inputInteger(Component parent, String title, String tipText,
                                    long initValue, boolean requireChange) {
        return (Long) inputContent(parent, title, tipText, initValue, requireChange, false,
                new NumberFormatter(NumberFormat.getIntegerInstance()));
    }

    public static Number inputNumber(Component parent, String title, String tipText,
                                     Number initValue, boolean requireChange) {
        return (Number) inputContent(parent, title, tipText, initValue, requireChange, false,
                new NumberFormatter(NumberFormat.getNumberInstance()));
    }

    public static String longInput(Component parent, String title, String tipText,
                                   String initText, boolean requireChange, boolean canEmpty) {
        LongInputDialog dialog = createDialog(parent, title, true, LongInputDialog.class);
        dialog.setTipText(tipText);
        dialog.setInitText(initText);
        dialog.setRequireChange(requireChange);
        dialog.setCanEmpty(canEmpty);
        return dialog.makeShow(false);
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
        Object[] parts = IxinUtilities.mnemonicOfText(name);
        fileChooser.setApproveButtonText((String) parts[0]);
        fileChooser.setApproveButtonMnemonic((int) parts[1]);
        fileChooser.setApproveButtonToolTipText(tipText);
    }

    // i18nKey: translation key
    public static void setApproveButtonName(String i18nKey) {
        String name = app.getText(i18nKey), tipText = app.getText(i18nKey + IAction.tipKeySuffix);
        setApproveButtonName(name, tipText);
    }

    public static void setCurrentDirectory(File dir) {
        if (dir != null) {
            fileChooser.setCurrentDirectory(dir);
        }
    }

    public static OpenResult openFile(Component parent, String title,
                                      boolean multiple, File initFile, File initDir,
                                      boolean acceptAll,
                                      FileFilter[] filters, FileFilter initFilter) {
        prepareFileChooser(title, JFileChooser.FILES_ONLY, multiple,
                initFile, initDir, acceptAll, filters, initFilter);

        setApproveButtonName("dialog.openFile.approveButton");

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

    private static File fileWithExtension() {
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

        setApproveButtonName("dialog.saveFile.approveButton");

        while (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File file = fileWithExtension();

            if (file.exists()) {
                if (askOverwrite) {
                    if (!showConfirm(parent, title,
                            app.getText("dialog.saveFile.askOverwrite", file.getPath()))) {
                        continue;
                    }
                }
            }

            return new OpenResult(new File[]{file}, fileChooser.getFileFilter());
        }
        // cancelled
        return null;
    }

    public static void viewException(Component parent, String title, String tipText,
                                     Throwable error) {
        ErrorTracer dialog = createDialog(parent, title, true, ErrorTracer.class);
        dialog.setTipText(tipText);
        dialog.setError(error);
        dialog.setVisible(true);
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
            comboBox.setSelectedIndex(comboBox.getItemCount() - 1);
        } else {
            comboBox.setSelectedIndex(0);
        }

        Object[] comps = {tipText, comboBox};
        int rev = JOptionPane.showConfirmDialog(parent, comps, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        return rev == JOptionPane.OK_OPTION ? items[comboBox.getSelectedIndex()] : null;
    }


    public static String displayableStringMap(String[] strings, String title) {
        StringBuilder builder = new StringBuilder("<html><table>");
        if (title != null) {
            builder.append("<caption><b color=blue>").append(title);
            builder.append("</b></caption>");
        }
        for (int i = 0; i < strings.length; i += 2) {
            builder.append("<tr>");
            builder.append("<td align=right>").append(strings[i]).append("</td>");
            builder.append(":<td>").append(strings[i + 1]).append("</td>");
            builder.append("</tr>");
        }
        return builder.append("</table></html>").toString();
    }

    public static void showDetails(Component parent, File file, Book book) {
        Worker worker = Worker.sharedInstance();

        Date date = new Date(file.lastModified());
        String[] strings = {
                app.getText("dialog.fileDetails.name"), file.getName(),
                app.getText("dialog.fileDetails.path"), file.getAbsoluteFile().getParent(),
                app.getText("dialog.fileDetails.size"), worker.formatFileSize(file.length()),
                app.getText("dialog.fileDetails.date"), TextUtils.formatDate(date, "yyyy-M-d H:m:s"),
                app.getText("dialog.fileDetails.format"), FilenameUtils.getExtension(file.getPath()).toUpperCase(),
        };
        String title;
        if (book != null) {
            title = app.getText("dialog.fileDetails.tipLabel", book.getTitle());
            String[] infos = worker.getFileInfo(book);
            if (infos != null) {
                String[] tmp = new String[strings.length + infos.length];
                System.arraycopy(strings, 0, tmp, 0, strings.length);
                System.arraycopy(infos, 0, tmp, strings.length, infos.length);
                strings = tmp;
            }
        } else {
            title = app.getText("dialog.fileDetails.title");
        }

        showText(parent, title, displayableStringMap(strings, null));
    }

    public static void editSettings(Frame parent) {
        new EditAppSettings(parent).setVisible(true);
    }

    public static void showAbout(Frame parent) {
        new AboutImabw(parent).setVisible(true);
    }

    public static void editAttributes(Component parent, Chapter chapter) {
        EditChapterAttributes dialog = createDialog(parent,
                app.getText("dialog.editAttributes.title", chapter.getTitle()),
                true, EditChapterAttributes.class);
        dialog.setChapter(chapter);
        dialog.makeShow(true);
    }

    public static void editExtensions(Component parent, Book book) {

    }

    public static void featureDeveloping(Component parent) {
        localizedInformation(parent, app.getText("app.name"), "dialog.feature.developing");
    }

    static WaitingDialog createWaitingDialog(Component parent, String title,
                                             String tipText, String waitingText) {
        WaitingDialog dialog = createDialog(parent, title, true, WaitingDialog.class);
        dialog.setTipText(tipText);
        dialog.setWaitingText(waitingText);
        return dialog;
    }

    public static void openWaiting(Component parent, String title,
                                   String tipText, String waitingText,
                                   WaitingWork work, boolean cancelable) {
        WaitingDialog dialog = createWaitingDialog(parent, title, tipText, waitingText);
        dialog.setWork(work, cancelable, true);
        work.execute();
        dialog.makeShow(false);
    }

    public static void openProgress(Component parent, String title,
                                    String tipText, String waitingText,
                                    WaitingWork work, boolean cancelable) {
        WaitingDialog dialog = createWaitingDialog(parent, title, tipText, waitingText);
        dialog.setWork(work, cancelable, false);
        work.execute();
        dialog.makeShow(false);
    }

    public static void browseURI(String uri) {
        if (uri == null) {
            return;
        }
        try {
            Desktop.getDesktop().browse(new URI(uri));
        } catch (Exception ex) {
            showError(null, app.getName(), ex.getMessage());
        }
    }
}
