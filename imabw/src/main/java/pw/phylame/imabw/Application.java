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

import pw.phylame.imabw.ui.Viewer;
import pw.phylame.ixin.IApplication;
import pw.phylame.ixin.frame.IFrame;

/**
 * The entry of Imabw.
 */
public class Application extends IApplication {

    /** The manager */
    private Manager manager = null;

    /** The worker */
    private Worker worker = null;

    /** The main frame */
    private Viewer viewer = null;

    public Application(String[] args) {
        super(args);
        checkHome();
        loadSettings();
        initApp();
    }

    /** Check and create user home directory for Imabw */
    private void checkHome() {
        java.io.File homeDir = new java.io.File(Constants.IMABW_HOME);
        if (! homeDir.exists() && ! homeDir.mkdirs()) {
            throw new RuntimeException("Cannot create Imabw home directory");
        }
    }

    /** Load configuration file */
    private void loadSettings() {
    }

    /** Initialize Imabw */
    private void initApp() {
        loadBundle(Constants.I18N_PATH);
        setTheme("system");
    }

    /** Get the application instance */
    public static Application getApplication() {
        return (Application) instance;
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
    public void start() {
        if (viewer == null) {
            viewer = new Viewer();
        }
        if (worker == null) {
            worker = new Worker();
        }
        if (manager == null) {
            manager = new Manager(viewer, worker);
        }
        // notify manager work
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

    public static void main(String[] args) {
        new Application(args).start();
    }
}