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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pw.phylame.imabw.ui.Viewer;
import pw.phylame.ixin.IApplication;
import pw.phylame.ixin.frame.IFrame;

import java.awt.Font;
import java.awt.Color;

import java.io.Reader;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Properties;

/**
 * The entry of Imabw.
 */
public class Application extends IApplication {
    private static Log LOG = LogFactory.getLog(Application.class);

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

    public boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /** Load configuration file */
    private void loadSettings() {
        Properties prop = new Properties();
        Reader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(Constants.SETTINGS_FILE), "UTF-8");
            prop.load(reader);
        } catch (IOException e) {
            LOG.debug("cannot load settings file: "+Constants.SETTINGS_FILE, e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOG.debug("cannot close settings file", e);
                }
            }
        }
        // locale
        String str = prop.getProperty("imabw.locale");
        if (! isEmpty(str)) {
            settings.put("locale", Locale.forLanguageTag(str.replace('_', '-')));
        } else {
            settings.put("locale", Locale.getDefault());
        }

        // L&F
        str = prop.getProperty("ui.theme");
        if (isEmpty(str)) {
            str = "system";
        }
        settings.put("ui.theme", str);
        // Title bar decorated
        str = prop.getProperty("ui.decorated");
        settings.put("ui.decorated", ! isEmpty(str) && Boolean.parseBoolean(str));
        // Toolbar
        str = prop.getProperty("ui.show_toolbar");
        settings.put("ui.show_toolbar", isEmpty(str) || Boolean.parseBoolean(str));
        // Lock toolbar
        str = prop.getProperty("ui.lock_toolbar");
        settings.put("ui.lock_toolbar", isEmpty(str) || Boolean.parseBoolean(str));
        // Sidebar
        str = prop.getProperty("ui.show_sidebar");
        settings.put("ui.show_sidebar", isEmpty(str) || Boolean.parseBoolean(str));
        // Statusbar
        str = prop.getProperty("ui.show_statusbar");
        settings.put("ui.show_statusbar", isEmpty(str) || Boolean.parseBoolean(str));
        // global font
        str = prop.getProperty("ui.font.global");
        Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
        if (! isEmpty(str)) {
            font = Font.decode(str);
        }
        settings.put("ui.font.global", font);
        // Anti aliasing font
        str = prop.getProperty("ui.font.aatext");
        settings.put("ui.font.aatext", isEmpty(str) || Boolean.parseBoolean(str));

        // editor font
        str = prop.getProperty("editor.font");
        font = new Font(Font.SANS_SERIF, Font.PLAIN, 16);
        if (! isEmpty(str)) {
            font = Font.decode(str);
        }
        settings.put("editor.font", font);

        // editor color
        str = prop.getProperty("editor.background");
        Color color = Color.WHITE;
        if (! isEmpty(str)) {
            color = Color.decode(str);
        }
        settings.put("editor.background", color);
        str = prop.getProperty("editor.foreground");
        color = Color.BLACK;
        if (! isEmpty(str)) {
            color = Color.decode(str);
        }
        settings.put("editor.foreground", color);

        // PMAB output encoding
        str = prop.getProperty("pmab.encoding");
        settings.put("pmab.encoding", str != null ? str : System.getProperty("file.encoding"));
    }

    /**
     * Saves settings to file.
     */
    public void saveSettings() {

    }

    /** Initialize Imabw */
    private void initApp() {
        loadBundle(Constants.I18N_PATH);
        setTheme((String) getSetting("ui.theme"), (boolean) getSetting("ui.decorated"));
        setAAText((boolean) getSetting("ui.font.aatext"));
        setGeneralFonts((Font) getSetting("ui.font.global"));
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
