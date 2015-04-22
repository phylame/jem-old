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

    /** The system arguments */
    private String[] args = null;

    /** Language resource */
    private java.util.ResourceBundle bundle = null;

    /** Mapped settings table */
    protected Map<String, Object> settings = new java.util.HashMap<>();

    /** The constructor */
    protected IApplication(String[] args) {
        instance = this;
        this.args = args;
        settings.put("locale", Locale.getDefault());
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
    public void setTheme(String name) {
        String theme = pw.phylame.ixin.IToolkit.getLookAndFeel(name);
        try {
            javax.swing.UIManager.setLookAndFeel(theme);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                javax.swing.UnsupportedLookAndFeelException exp) {
            exp.printStackTrace();
        }
    }

    /**
     * Get system arguments.
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
