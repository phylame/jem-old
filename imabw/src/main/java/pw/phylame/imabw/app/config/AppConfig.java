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

import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.io.IOException;

import pw.phylame.gaf.core.GafUtilities;
import pw.phylame.gaf.core.Settings;
import pw.phylame.imabw.app.Constants;
import pw.phylame.imabw.app.Imabw;

/**
 * Main settings for Imabw.
 */
public class AppConfig extends Settings {
    private static AppConfig instance;

    public static AppConfig sharedInstance() {
        if (instance == null) {
            try {
                instance = new AppConfig();
            } catch (IOException e) {
                throw new RuntimeException("cannot load app config", e);
            }
        }
        return instance;
    }

    public static AppConfig dumpedInstance() {
        try {
            AppConfig config = new AppConfig(0);    // no error occurred
            config.update(sharedInstance());
            return config;
        } catch (IOException e) {
            throw new AssertionError("BUG: IOException should't be raised here");
        }
    }

    private AppConfig() throws IOException {
        super(Constants.SETTINGS_HOME + "settings", true, true);
    }

    private AppConfig(int unused) throws IOException {
        super(null, false, false);
    }

    @Override
    public void reset() {
        clear();
        setComment("Common settings for Imabw");

        setAppLocale(Locale.getDefault());
        setHistoryEnable(true);
        setHistoryLimits(Constants.DEFAULT_HISTORY_LIMITS);
    }

    public Locale getAppLocale() {
        return getItem("app.locale", Locale.getDefault(), Locale.class);
    }

    public void setAppLocale(Locale locale) {
        setItem("app.locale", locale, Locale.class);
    }

    public boolean isHistoryEnable() {
        return getBoolean("app.history.enable", true);
    }

    public void setHistoryEnable(boolean enable) {
        setBoolean("app.history.enable", enable);
    }

    public int getHistoryLimits() {
        return getInteger("app.history.limits", Constants.DEFAULT_HISTORY_LIMITS);
    }

    public void setHistoryLimits(int limits) {
        setInteger("app.history.limits", limits);
    }

    public static Locale[] supportedLocales() {
        URL url = AppConfig.class.getResource("/" + Imabw.I18N_DIR + "all.txt");
        List<String> tags = null;
        try {
            tags = GafUtilities.supportedLanguages(url);
        } catch (IOException e) {
            // ignored
        }
        assert tags != null;
        Locale[] locales = new Locale[tags.size()];
        for (int i = 0; i < tags.size(); i++) {
            locales[i] = Locale.forLanguageTag(tags.get(i).replace('_', '-'));
        }
        return locales;
    }
}
