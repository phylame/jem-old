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

package pw.phylame.imabw;

import java.awt.Font;
import java.io.File;
import java.util.Locale;

import pw.phylame.pat.gaf.Translator;
import pw.phylame.pat.ixin.IAction;
import pw.phylame.pat.ixin.IApplication;
import pw.phylame.imabw.ui.UIState;
import pw.phylame.imabw.ui.Viewer;

/**
 * The entry of Imabw.
 */
public class Imabw extends IApplication implements Constants {
    private Manager manager = null;

    private Worker worker = null;

    private Viewer viewer = null;

    public Imabw(String[] args) {
        super(APP_NAME, APP_VERSION, args);
        ensureHomeExisted();
        initApp();
    }

    public Config getConfig() {
        return Config.getInstance();
    }

    @Override
    protected void onDestroy() {
        Config config = getConfig();

        if (config.isChanged()) {
            config.sync();
        }

        viewer.destroy();
        UIState.getInstance().sync();
    }

    /** Initialize Imabw */
    private void initApp() {
        Config config = getConfig();

        Locale.setDefault(config.getAppLocale());
        loadLanguage();

        setAAText(config.isAntiAliased());
        Font font = config.getGlobalFont();
        if (font != null) {
            setGeneralFonts(font);
        }
        setTheme(config.getLafTheme(), config.isDecoratedFrame());
        IAction.smallIconSize = config.getSmallIconSize();
        IAction.largeIconSize = config.getLargeIconSize();
    }

    public void loadLanguage() {
        Translator translator = new Translator(I18N_PATH);
        installTranslator(translator);
    }

    /** Get the application instance */
    public static Imabw getInstance() {
        return (Imabw) getApplication();
    }

    @Override
    public void onCommand(String command) {
        // send to manager
        manager.onCommand(command);
    }

    public void onTreeAction(String actionID) {
        // send to manager
        manager.onTreeAction(actionID);
    }

    public void onTabAction(String actionID) {
        // send to manager
        manager.onTabAction(actionID);
    }

    @Override
    protected void onStart() {
        viewer = new Viewer();
        worker = new Worker();
        manager = new Manager(viewer, worker);
    }

    @Override
    public void run() {
        // notify manager to work
        manager.begin();
    }

    public Viewer getViewer() {
        return viewer;
    }

    public Worker getWorker() {
        return worker;
    }

    public Manager getManager() {
        return manager;
    }

    public static void main(final String[] args) {
        Imabw app = new Imabw(args);
        app.start();
    }
}
