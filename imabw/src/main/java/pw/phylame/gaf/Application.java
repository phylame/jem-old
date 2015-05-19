/*
 * Copyright 2014-2015 Peng Wan <phylame@163.com>
 *
 * This file is part of GAF.
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

package pw.phylame.gaf;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.text.MessageFormat;

/**
 * General application model.
 */
public abstract class Application implements Runnable {

    /** Unique application instance */
    protected static Application instance = null;

    /** User home directory */
    private static String USER_HOME = null;

    /** Application name */
    private String name;

    /** Application version */
    private String version;

    /** Command line arguments */
    private String[] arguments;

    /** Global language bundle */
    private ResourceBundle languageBundle = null;

    protected Application(String name, String[] args) {
        instance = this;
        this.name = name;
        this.arguments = args;
        USER_HOME = System.getProperty("user.home") + "/." + name;
    }

    public static Application getApplication() {
        return instance;
    }

    public static String getUserHome() {
        return USER_HOME;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String[] getArguments() {
        return arguments;
    }

    public void setLocale(Locale locale) {
        Locale.setDefault(locale);
    }

    protected void loadLanguage(String path) throws MissingResourceException {
        languageBundle = ResourceBundle.getBundle(path);
    }

    protected void loadLanguage(String path, Locale locale) throws MissingResourceException {
        languageBundle = ResourceBundle.getBundle(path, locale);
    }

    public String getText(String key) {
        if (languageBundle == null) {
            throw new RuntimeException("No language bundle initialized, calls loadLanguage firstly");
        }
        return languageBundle.getString(key);
    }

    public String getText(String key, Object... args) {
        if (languageBundle == null) {
            throw new RuntimeException("No language bundle initialized, calls loadLanguage firstly");
        }
        return MessageFormat.format(languageBundle.getString(key), args);
    }

    protected void onStart() {

    }

    public void start() {
        run();
    }

    protected void onDestroy() {

    }

    public void exit(int status) {
        onDestroy();
        System.exit(status);
    }

    public void exit() {
        exit(0);
    }
}
