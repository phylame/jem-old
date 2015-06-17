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

/**
 * General application model.
 */
public abstract class Application implements Runnable, I18nSupport {

    /** Unique application instance */
    protected static Application instance = null;

    /** Application name */
    private String name;

    /** Application version */
    private String version;

    /** Command line arguments */
    private String[] arguments;

    /** Installed translator */
    private Translator translator = null;

    /**
     * User home directory for application.
     * Location: ${login_user_home}/.${app_name}
     */
    private String home;

    protected Application(String name, String version, String[] args) {
        if (instance != null) {         // already created
            throw new RuntimeException("Application has been created");
        }
        instance = this;

        if (name == null) {
            throw new NullPointerException("name");
        }
        this.name = name;

        if (version == null) {
            throw new NullPointerException("version");
        }
        this.version = version;

        this.arguments = args;

        home = System.getProperty("user.home") + "/." + name.toLowerCase();
    }

    public static Application getApplication() {
        return instance;
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

    public String getHome() {
        return home;
    }

    public void installTranslator(Translator translator) {
        if (translator == null) {
            throw new NullPointerException("translator");
        }
        this.translator = translator;
    }

    private void ensureTranslatorInstalled() {
        if (translator == null) {
            throw new RuntimeException("No translator initialized, " +
                    "call installTranslator firstly.");
        }
    }

    @Override
    public String getText(String key) {
        ensureTranslatorInstalled();
        return translator.getText(key);
    }

    @Override
    public String getText(String key, Object ... args) {
        ensureTranslatorInstalled();
        return translator.getText(key, args);
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
