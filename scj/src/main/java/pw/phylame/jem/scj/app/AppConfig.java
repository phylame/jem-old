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

package pw.phylame.jem.scj.app;

import java.util.Date;
import java.util.Locale;
import java.io.IOException;
import pw.phylame.jem.core.Jem;
import pw.phylame.gaf.core.Settings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Configurations for SCJ.
 */
public class AppConfig extends Settings {
    private static Log       LOG      = LogFactory.getLog(AppConfig.class);

    private static AppConfig instance = null;

    public static AppConfig getInstance() {
        if (instance == null) {
            try {
                instance = new AppConfig();
            } catch (IOException e) {
                LOG.debug(e);
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

        setComment("Configurations for PW SCJ\nCreated: " + new Date());

        setAppLocale(Locale.getDefault());
        setDefaultFormat(getDefaultFormat());
        setTocIndent(getTocIndent());
    }

    public Locale getAppLocale() {
        return getLocal("app.locale", Locale.getDefault());
    }

    public void setAppLocale(Locale locale) {
        setLocale("app.locale", locale, "Locale settings for SCJ");
    }

    public String getDefaultFormat() {
        return getString("jem.defaultFormat", Jem.PMAB_FORMAT);
    }

    public void setDefaultFormat(String format) {
        setString("jem.defaultFormat", format, "Default format using by Jem");
    }

    public String getTocIndent() {
        return getString("scj.view.tocIndent", "  ");
    }

    public void setTocIndent(String indent) {
        setString("scj.view.tocIndent", indent, "Indent of SCJ toc tree viewer");
    }
}
