/*
 * Copyright 2015 Peng Wan <phylame@163.com>
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

package pw.phylame.jem.imabw.app.config;

import java.awt.Font;
import java.util.Date;
import java.util.Locale;
import java.io.IOException;

import pw.phylame.gaf.ixin.IToolkit;
import pw.phylame.gaf.ixin.ISettings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pw.phylame.jem.imabw.app.Constants;
import pw.phylame.jem.imabw.app.Imabw;

/**
 * Main configurations for Imabw.
 */
public class AppSettings extends ISettings {
    private static Log LOG = LogFactory.getLog(AppSettings.class);
    private static AppSettings instance = null;

    private static void mapLafName() {
        IToolkit.lafMap.put("jgoodies-3d", "com.jgoodies.looks.plastic.Plastic3DLookAndFeel");
        IToolkit.lafMap.put("jgoodies-xp", "com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
        IToolkit.lafMap.put("kunststoff", "com.incors.plaf.kunststoff.KunststoffLookAndFeel");
        IToolkit.lafMap.put("jtattoo-acryl", "com.jtattoo.plaf.acryl.AcrylLookAndFeel");
        IToolkit.lafMap.put("jtattoo-texture", "com.jtattoo.plaf.texture.TextureLookAndFeel");
    }

    public static AppSettings getInstance() {
        if (instance == null) {
            try {
                instance = new AppSettings();
            } catch (IOException e) {
                LOG.debug(e);
            }

            mapLafName();

            Imabw.getInstance().addExitHook(new Runnable() {
                @Override
                public void run() {
                    if (instance.isChanged()) {
                        try {
                            instance.sync();
                        } catch (IOException e) {
                            LOG.debug("cannot sync app settings", e);
                        }
                    }
                }
            });
        }
        return instance;
    }

    private AppSettings() throws IOException {
        super(true, Constants.SETTINGS_HOME + "settings");
    }

    @Override
    public void reset() {
        clear();

        setComment("Configurations for PW Imabw\nCreated: " + new Date());

        setAppLocale(getAppLocale());
        setDebugEnable(isDebugEnable());
        setPluginsEnable(isPluginsEnable());
        setHistoryLimits(getHistoryLimits());
        resetUI();
    }

    public Locale getAppLocale() {
        return getLocal("app.locale", Locale.getDefault());
    }

    public void setAppLocale(Locale locale) {
        setLocale("app.locale", locale, "Locale for Imabw");
    }

    public boolean isDebugEnable() {
        return getBoolean("app.debug", true);
    }

    public void setDebugEnable(boolean enable) {
        setBoolean("app.debug", enable, "Enable for print debug message");
    }

    public boolean isPluginsEnable() {
        return getBoolean("plugin.enable", true);
    }

    public void setPluginsEnable(boolean enable) {
        setBoolean("plugin.enable", enable, "To enable plugins");
    }

    public String getIconSet() {
        return getString("ui.icons", "default");
    }

    public void setIconSet(String iconSet) {
        setString("ui.icons", iconSet, "Icon set for IxIn framework");
    }

    public Font getGlobalFont() {
        Font font = getFont("ui.font", null);
        if (font == null) {
            String s = System.getProperty("ixin.font");
            if (s != null && !s.isEmpty()) {
                font = Font.decode(s);
            }
        }
        return font;
    }

    public void setGlobalFont(Font font) {
        if (font != null) {
            setFont("ui.font", font, "Global font for SWING");
        } else {
            setProperty("ui.font", "", "Global font for SWING");
        }
    }

    public boolean isAntiAliasing() {
        return getBoolean("ui.font.anti", true);
    }

    public void setAntiAliasing(boolean enable) {
        setBoolean("ui.font.anti", enable, "Enable anti aliasing font");
    }

    public String getLafTheme() {
        String laf = getString("ui.laf", null);
        if (laf == null) {
            laf = System.getProperty("ixin.laf");
            if (laf == null || laf.isEmpty()) {
                laf = "system";
            }
        }
        return laf;
    }

    public void setLafTheme(String theme) {
        setString("ui.laf", theme, "L&F theme for SWING");
    }

    public boolean isWindowDecorated() {
        return getBoolean("ui.laf.decorated", false);
    }

    public void setWindowDecorated(boolean decorated) {
        setBoolean("ui.laf.decorated", decorated, "Enable decorated frame or dialog by L&F");
    }

    private void resetUI() {
        setIconSet(getIconSet());
        setGlobalFont(getGlobalFont());
        setAntiAliasing(isAntiAliasing());
        setLafTheme(getLafTheme());
        setWindowDecorated(isWindowDecorated());
    }

    public int getHistoryLimits() {
        return getInteger("file.historyLimits", 18);
    }

    public void setHistoryLimits(int limits) {
        setInteger("file.historyLimits", limits, "Maximum number of history record");
    }
}
