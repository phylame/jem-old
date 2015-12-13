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

package pw.phylame.imabw.app.config;

import java.io.IOException;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;

import pw.phylame.gaf.ixin.ISettings;
import pw.phylame.gaf.ixin.IxinUtilities;
import pw.phylame.imabw.app.Constants;

/**
 * Imabw UI settings/
 */
public class UIConfig extends ISettings {
    private static UIConfig instance;

    public static UIConfig sharedInstance() {
        if (instance == null) {
            try {
                instance = new UIConfig();
            } catch (IOException e) {
                throw new RuntimeException("cannot load ui config", e);
            }
        }
        mapSupportedLafs();
        return instance;
    }

    public static UIConfig dumpedInstance() {
        try {
            UIConfig config = new UIConfig(0);    // no error occurred
            config.update(sharedInstance());
            return config;
        } catch (IOException e) {
            throw new AssertionError("BUG: IOException should't be raised here");
        }
    }

    private static void mapSupportedLafs() {
        final String[] lafMaps = {
                "JGoodies", "com.jgoodies.looks.plastic.PlasticLookAndFeel",
                "JGoodies 3D", "com.jgoodies.looks.plastic.Plastic3DLookAndFeel",
                "JGoodies XP", "com.jgoodies.looks.plastic.PlasticXPLookAndFeel",
                "Kunststoff", "com.incors.plaf.kunststoff.KunststoffLookAndFeel",
                "JTattoo Acryl", "com.jtattoo.plaf.acryl.AcrylLookAndFeel",
                "JTattoo Texture", "com.jtattoo.plaf.texture.TextureLookAndFeel",
        };
        for (int i = 0; i < lafMaps.length; i += 2) {
            IxinUtilities.mapLafPath(lafMaps[i], lafMaps[i + 1]);
        }
    }

    private UIConfig() throws IOException {
        super(true, Constants.SETTINGS_HOME + "ui", true);
    }

    private UIConfig(int unused) throws IOException {
        super(false, null, false);
    }

    @Override
    public void reset() {
        clear();
        setComment("UI components settings");

        setLafTheme(getDefaultLaf());
        setWindowDecorated(false);
        setGlobalFont(getDefaultFont());
        setAntiAliasing(true);
        setIconSet(getDefaultIcons());
        setMnemonicEnable(true);
    }

    public String getLafTheme() {
        return getString("ui.laf", getDefaultLaf());
    }

    public void setLafTheme(String name) {
        setString("ui.laf", name);
    }

    public boolean isWindowDecorated() {
        return getBoolean("ui.laf.decorated", false);
    }

    public void setWindowDecorated(boolean decorated) {
        setBoolean("ui.laf.decorated", decorated);
    }

    public Font getGlobalFont() {
        return getFont("ui.font", getDefaultFont());
    }

    public void setGlobalFont(Font font) {
        if (font != null) {
            setFont("ui.font", font);
        } else {
            setString("ui.font", "");
        }
    }

    public boolean isAntiAliasing() {
        return getBoolean("ui.font.anti", true);
    }

    public void setAntiAliasing(boolean enable) {
        setBoolean("ui.font.anti", enable);
    }

    public String getIconSet() {
        return getString("ui.icons", getDefaultIcons());
    }

    public void setIconSet(String iconSet) {
        setString("ui.icons", iconSet);
    }

    public boolean isMnemonicEnable() {
        return getBoolean("ui.mnemonic.enable", isSupportMnemonic());
    }

    public void setMnemonicEnable(boolean enable) {
        setBoolean("ui.mnemonic.enable", enable);
    }

    private boolean isSupportMnemonic() {
        return true;
    }

    private Font getDefaultFont() {
        return Font.getFont("imabw.font");
    }

    private String getDefaultLaf() {
        return System.getProperty("imabw.theme", Constants.DEFAULT_LAF_THEME);
    }

    private String getDefaultIcons() {
        return System.getProperty("imabw.icons", Constants.DEFAULT_ICON_SET);
    }

    public static String[] supportedLafs() {
        ArrayList<String> lafs = new ArrayList<>(IxinUtilities.lafMap.keySet());
        Collections.sort(lafs);
        return lafs.toArray(new String[lafs.size()]);
    }

    public static String[] supportedIcons() {
        return new String[]{Constants.DEFAULT_ICON_SET};
    }
}
