/*
 * Copyright 2015 Peng Wan <phylame@163.com>
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

package pw.phylame.imabw;

import static pw.phylame.imabw.Constants.*;
import pw.phylame.imabw.ui.Viewer;
import pw.phylame.imabw.ui.com.MainPane;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Jem;

import javax.swing.*;
import java.io.File;

/**
 * The manager.
 */
public class Manager {
    /** The application */
    private Application app = Application.getApplication();

    /** The viewer */
    private Viewer viewer;

    /** The worker */
    private Worker worker;

    private MainPane mainPane;

    private File source = null;
    private Book book = null;
    private boolean modified = false;

    public Manager(Viewer viewer, Worker worker) {
        this.viewer = viewer;
        this.worker = worker;
    }

    /** Begin manager works */
    public void begin() {
        viewer.setStatusText(app.getText("Frame.StatusBar.Ready"));
        showMainPane();
        newFile();
        viewer.setVisible(true);

        mainPane.focusToTreeWindow();
    }

    /** Stop manager works */
    public void stop() {
        app.exit(0);
    }

    private void showMainPane() {
        if (viewer.getPaneRender() instanceof MainPane) {
            return;
        }
        mainPane = new MainPane();
        viewer.setPaneRender(mainPane);
    }

    /** Get current name of book file */
    private String getSourceName() {
        String name;
        /* saved or open PMAB file */
        if (source != null) {
            name = source.getPath();
        } else {
            name = String.format("%s.%s", app.getText("Common.NewBookTitle"), Jem.PMAB_FORMAT);
        }
        return name;
    }

    /** Update frame title */
    private void updateViewerTitle() {
        if (modified) {
            viewer.setTitle(String.format("%s* - %s", getSourceName(), app.getText("App.Name")));
        } else {
            viewer.setTitle(String.format("%s - %s", getSourceName(), app.getText("App.Name")));
        }
    }

    /** Check book is modified and ask saving */
    private boolean maybeSave(String title) {
        if (!modified) {
            return true;
        }
        Object[] options = {app.getText("Dialog.ButtonSave"), app.getText("Dialog.ButtonDiscard"),
                app.getText("Dialog.ButtonCancel")};
        int ret = JOptionPane.showOptionDialog(viewer, app.getText("Dialog.Save.LabelSaveTip"),
                title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                options, options[0]);
        switch (ret) {
            case JOptionPane.YES_OPTION:
                saveFile();
                break;
            case JOptionPane.NO_OPTION:
                break;
            case JOptionPane.CANCEL_OPTION:
            case -1:
                return false;
        }
        return true;
    }

    private void newFile() {
        if (! maybeSave(app.getText("Dialog.New.Title"))) {
            return;
        }
        book = worker.newBook();
        modified = false;
        source = null;
        mainPane.setBook(book);
        updateViewerTitle();
    }

    private void openFile() {
        if (! maybeSave(app.getText("Dialog.Open.Title"))) {
            return;
        }
    }

    private void saveFile() {
        modified = false;
    }

    private void saveAsFile() {
        if (! maybeSave(app.getText("Dialog.SaveAs.Title"))) {
            return;
        }
    }

    public void onCommand(Object cmdID) {
        switch ((String) cmdID) {
            case NEW_FILE:
                newFile();
                break;
            case OPEN_FILE:
                openFile();
                break;
            case SAVE_FILE:
                saveFile();
                break;
            case SAVE_AS_FILE:
                saveAsFile();
                break;
            case EXIT_APP:
                stop();
                break;
            case SHOW_TOOLBAR:
                viewer.showOrHideToolBar();
                break;
            case SHOW_STATUSBAR:
                viewer.showOrHideStatusBar();
                break;
            case SHOW_SIDEBAR:
                viewer.showOrHideSideBar();
                break;
            case SHOW_ABOUT:
                pw.phylame.imabw.ui.dialog.AboutDialog.showAbout(viewer);
                break;
            default:
                System.out.println(cmdID);
                break;
        }
    }

    public void onTreeAction(Object actionID) {
        switch ((String) actionID) {
            case TREE_NEW:
                break;
            default:
                System.out.println(actionID);
                break;
        }
    }

    public void onTabAction(Object actionID) {
        System.out.println(actionID);
    }
}
