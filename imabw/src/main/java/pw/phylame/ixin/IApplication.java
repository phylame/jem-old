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

package pw.phylame.ixin;

import javax.swing.UIManager;
import javax.swing.SwingUtilities;

import pw.phylame.gaf.Application;
import pw.phylame.ixin.frame.IFrame;

/**
 * The GUI application model.
 */
public abstract class IApplication extends Application {

    /** The constructor */
    protected IApplication(String name, String[] args) {
        super(name, args);
    }

    public void setTheme(String name) {
        setTheme(name, IFrame.isDefaultLookAndFeelDecorated());
    }

    /**
     * Set SWING L&F.
     * @param name class name of L&F class or brief name.
     */
    public void setTheme(String name, boolean decorated) {
        /* decorated title bar */
        javax.swing.JFrame.setDefaultLookAndFeelDecorated(decorated);
        javax.swing.JDialog.setDefaultLookAndFeelDecorated(decorated);

        String theme = IToolkit.getLookAndFeel(name);
        try {
            UIManager.setLookAndFeel(theme);
            IFrame frame = getViewer();
            if (frame != null) {
                SwingUtilities.updateComponentTreeUI(frame);
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                javax.swing.UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }

    public void setGeneralFonts(java.awt.Font font) {
        UIManager.put("ToolTip.font", font);

        UIManager.put("TextField.font", font);
        UIManager.put("PasswordField.font", font);
        UIManager.put("FormattedTextField.font", font);
        UIManager.put("TextArea.font", font);
        UIManager.put("TextPane.font", font);
        UIManager.put("EditorPane.font", font);

        UIManager.put("Spinner.font", font);
        UIManager.put("ComboBox.font", font);
        UIManager.put("CheckBox.font", font);
        UIManager.put("Label.font", font);
        UIManager.put("List.font", font);
        UIManager.put("ProgressBar.font", font);
        UIManager.put("Tree.font", font);
        UIManager.put("Table.font", font);
        UIManager.put("TableHeader.font", font);

        UIManager.put("Button.font", font);
        UIManager.put("ToggleButton.font", font);
        UIManager.put("RadioButton.font", font);

        UIManager.put("DesktopIcon.font", font);
        UIManager.put("TitledBorder.font", font);

        UIManager.put("Menu.font", font);
        UIManager.put("MenuItem.font", font);
        UIManager.put("PopupMenu.font", font);
        UIManager.put("MenuBar.font", font);
        UIManager.put("CheckBoxMenuItem.font", font);
        UIManager.put("RadioButtonMenuItem.font", font);

        UIManager.put("ToolBar.font", font);

        UIManager.put("TabbedPane.font", font);
        UIManager.put("OptionPane.messageFont", font);
        UIManager.put("OptionPane.buttonFont", font);
    }

    public void setAAText(boolean enable) {
        System.setProperty("swing.aatext", String.valueOf(enable));
    }

    /**
     * Return the main frame of application.
     */
    public abstract IFrame getViewer();

    /**
     * Execute a command identified with {@code cmd}.
     */
    public abstract void onCommand(Object cmd);
}
