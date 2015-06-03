/*
 * Copyright 2015 Peng Wan <phylame@163.com>
 *
 * This file is part of PAT Core.
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

package pw.pat.gaf;

import java.util.Locale;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.MissingResourceException;

/**
 * General application model.
 */
public abstract class Application implements Runnable, I18nSupport {

    /** Unique application instance */
    protected static Application instance = null;

    public static Application getApplication() {
        return instance;
    }

    /**
     * User home directory for application.
     * Location: ${login_user_home}/.${app_name}
     */
    private static String USER_HOME = null;

    public static String getUserHome() {
        return USER_HOME;
    }

    /** Application name */
    private String name;

    /** Application version */
    private String version;

    /** Command line arguments */
    private String[] arguments;

    /** Global language bundle */
    private ResourceBundle languageBundle = null;

    protected Application(String name, String version, String[] args) {
        if (instance != null) {         // already created
            throw new RuntimeException("Application has been created");
        }
        instance = this;
        this.name = name;
        this.version = version;
        this.arguments = args;
        USER_HOME = System.getProperty("user.home") + "/." + name.toLowerCase();
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String[] getArguments() {
        return arguments;
    }

    public Locale getLocale() {
        return Locale.getDefault();
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

    @Override
    public String getText(String key) {
        if (languageBundle == null) {
            throw new RuntimeException("No language bundle initialized, calls loadLanguage firstly");
        }
        return languageBundle.getString(key);
    }

    @Override
    public String getText(String key, Object... args) {
        if (languageBundle == null) {
            throw new RuntimeException("No language bundle initialized, calls loadLanguage firstly");
        }
        return MessageFormat.format(languageBundle.getString(key), args);
    }

    protected void onStart() {

    }

    public void start() {
        onStart();
        run();
    }

    protected void onDestroy() {

    }

    public void exit() {
        exit(0);
    }

    public void exit(int status) {
        onDestroy();
        System.exit(status);
    }
}
