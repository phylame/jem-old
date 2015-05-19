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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pw.phylame.imabw.ui.Viewer;
import pw.phylame.ixin.IApplication;
import pw.phylame.ixin.frame.IFrame;

import javax.swing.*;
import java.awt.Font;

/**
 * The entry of Imabw.
 */
public class Imabw extends IApplication implements Constants {
    private static Log LOG = LogFactory.getLog(Imabw.class);

    /** The manager */
    private Manager manager = null;

    /** The worker */
    private Worker worker = null;

    /** The main frame */
    private Viewer viewer = null;

    private Config config;

    public Imabw(String[] args) {
        super(INNER_NAME, args);
        checkHome();
        config = new Config();
        if (config.settingCount() == 0) { // no config
            LOG.trace("no config found, create new one");
            config.reset();     // save new config when exiting imabw
        }
        initApp();
    }

    /** Check and create user home directory for Imabw */
    private void checkHome() {
        java.io.File homeDir = new java.io.File(IMABW_HOME);
        if (!homeDir.exists() && !homeDir.mkdirs()) {
            throw new RuntimeException("Cannot create Imabw home directory");
        }
    }

    public Config getConfig() {
        return config;
    }

    @Override
    protected void onDestroy() {
        if (config.isChanged()) {
            config.setComment("Configurations for Imabw v"+VERSION+", encoding: UTF-8");
            config.sync();
        }
    }

    /** Initialize Imabw */
    private void initApp() {
        setLocale(config.getAppLocale());
        loadLanguage();
        setAAText(config.isAntiAliased());
        Font font = config.getGlobalFont();
        if (font != null) {
            setGeneralFonts(font);
        }
        setTheme(config.getLafTheme(), config.isDecoratedFrame());
    }

    public void loadLanguage() {
        loadLanguage(I18N_PATH);
    }

    /** Get the application instance */
    public static Imabw getInstance() {
        return (Imabw) getApplication();
    }

    @Override
    public void onCommand(Object cmdID) {
        // send to manager
        manager.onCommand(cmdID);
    }

    public void onTreeAction(Object actionID) {
        // send to manager
        manager.onTreeAction(actionID);
    }

    public void onTabAction(Object actionID) {
        // send to manager
        manager.onTabAction(actionID);
    }

    @Override
    public void run() {
        if (viewer == null) {
            viewer = new Viewer();
        }
        if (worker == null) {
            worker = new Worker();
        }
        if (manager == null) {
            manager = new Manager(viewer, worker);
        }
        // notify manager to work
        manager.begin();
    }

    @Override
    public IFrame getViewer() {
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
        javax.swing.SwingUtilities.invokeLater(app);
    }
}
