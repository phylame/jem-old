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
import pw.phylame.ixin.frame.IFrame;

import java.util.Map;
import java.util.Locale;
import java.text.MessageFormat;

/**
 * The application descriptor.
 */
public abstract class IApplication {

    /** The unique {@code IApplication} instance */
    protected static IApplication instance = null;

    /** Application name */
    private String name;

    /** The system arguments */
    private String[] args;

    /** Language resource */
    private java.util.ResourceBundle bundle = null;

    /** Mapped settings table */
    protected Map<String, Object> settings = new java.util.HashMap<>();

    /** The constructor */
    protected IApplication(String name, String[] args) {
        instance = this;
        setName(name);
        this.args = args;
        loadSettings();
    }

    protected void loadSettings() {

    }

    protected void loadBundle(String path) throws java.util.MissingResourceException {
        Locale locale;
        Object o = settings.get("locale");
        if (o instanceof Locale) {
            locale = (Locale) o;
        } else {
            locale = Locale.getDefault();
        }
        loadBundle(path, locale);
    }

    /**
     * Load language resource bundle.
     * @param path path of bundle file
     * @param locale used locale
     */
    protected void loadBundle(String path, Locale locale) throws java.util.MissingResourceException {
        bundle = java.util.ResourceBundle.getBundle(path, locale);
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
     * Gets application name.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets application name.
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets system arguments.
     * @return array of argument string
     */
    public String[] getArguments() {
        return args;
    }

    /**
     * Get translation string by its name.
     * @exception RuntimeException if language bundle not initialized.
     */
    public String getText(String key, Object... args) {
        if (bundle == null) {
            throw new RuntimeException("Language bundle not initialized.");
        }
        return MessageFormat.format(bundle.getString(key), args);
    }

    /**
     * Return mapped settings.
     * <p>The settings is a key-value pair, the string key and value.</p>
     */
    public Map<String, Object> getSettings() {
        return settings;
    }

    /**
     * Returns setting item with specified key.
     * @param key item key
     * @return item value or <code>null</code> if <code>null</code> not found
     */
    public Object getSetting(String key) {
        return settings.get(key);
    }

    /**
     * Start application.
     */
    public abstract void start();

    /**
     * Return the main frame of application.
     */
    public abstract IFrame getViewer();

    /**
     * Execute a command identified with {@code cmd}.
     */
    public abstract void onCommand(Object cmd);

    /**
     * Exit application
     * @param status exit status
     */
    public void exit(int status) {
        System.exit(status);
    }
}
