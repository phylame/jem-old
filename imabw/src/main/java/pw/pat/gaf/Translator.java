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

import java.util.Locale;
import java.util.ResourceBundle;
import java.text.MessageFormat;

/**
 * Translation provider.
 */
public class Translator implements I18nSupport {
    private ResourceBundle bundle;

    public Translator(String path) {
        bundle = ResourceBundle.getBundle(path);
    }

    public Translator(String path, Locale locale) {
        bundle = ResourceBundle.getBundle(path, locale);
    }

    public Translator(String path, Locale locale, ClassLoader classLoader) {
        bundle = ResourceBundle.getBundle(path, locale, classLoader);
    }

    public ResourceBundle getBundle() {
        return bundle;
    }

    public void setBundle(ResourceBundle bundle) {
        if (bundle == null) {
            throw new NullPointerException("bundle");
        }
        this.bundle = bundle;
    }

    @Override
    public String getText(String key) {
        return bundle.getString(key);
    }

    @Override
    public String getText(String key, Object ... args) {
        return MessageFormat.format(bundle.getString(key), args);
    }
}
