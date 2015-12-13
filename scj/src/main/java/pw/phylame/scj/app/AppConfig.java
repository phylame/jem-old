/*
 * Copyright 2014-2015 Peng Wan <phylame@163.com>
 *
 * This file is part of SCJ.
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

package pw.phylame.scj.app;

import java.util.Date;
import java.util.Locale;
import java.io.IOException;

import pw.phylame.jem.core.Jem;
import pw.phylame.gaf.core.Settings;

/**
 * Configurations for SCJ.
 */
public class AppConfig extends Settings {
    private static AppConfig instance;

    static AppConfig sharedInstance() {
        if (instance == null) {
            try {
                instance = new AppConfig();
            } catch (IOException e) {
                throw new RuntimeException("cannot load app settings", e);
            }
        }
        return instance;
    }

    private AppConfig() throws IOException {
        super();
    }

    @Override
    public void reset() {
        clear();

        setComment(String.format("Configurations for PW SCJ %s\nCreated: %s",
                Constants.VERSION, new Date()));

        setAppLocale(Locale.getDefault());
        setDebugLevel(getDebugLevel());
        setOutputFormat(getOutputFormat());
        setViewKey(getViewKey());
        setTocIndent(getTocIndent());
    }

    Locale getAppLocale() {
        return getItem("app.locale", Locale.getDefault(), Locale.class);
    }

    void setAppLocale(Locale locale) {
        setItem("app.locale", locale, Locale.class);
    }

    String getDebugLevel() {
        return getString("sci.debug.level", Constants.DEBUG_NONE);
    }

    void setDebugLevel(String level) {
        setString("sci.debug.level", level);
    }

    String getOutputFormat() {
        return getString("jem.output.defaultFormat", Jem.PMAB_FORMAT);
    }

    void setOutputFormat(String format) {
        setString("jem.output.defaultFormat", format);
    }

    String getViewKey() {
        return getString("sci.view.defaultKey", Constants.VIEW_ALL);
    }

    void setViewKey(String key) {
        setString("sci.view.defaultKey", key);
    }

    String getTocIndent() {
        return getString("sci.view.tocIndent", "  ");
    }

    void setTocIndent(String indent) {
        setString("sci.view.tocIndent", indent);
    }
}
