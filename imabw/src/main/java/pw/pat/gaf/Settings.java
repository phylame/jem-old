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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;

import java.util.Set;
import java.util.Map;
import java.util.Date;
import java.util.Locale;
import java.util.TreeMap;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * General settings provider.
 */
public class Settings {
    private static Log LOG = LogFactory.getLog(Settings.class);

    public static final String ENCODING = "UTF-8";

    public static final String COMMENT_LABEL = "#";

    public static final String KEY_VALUE_SEPARATOR = "=";

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    public static final String DATE_FORMAT = "yyyy-M-D h:m:s";

    private class SettingItem {
        public String comment;
        public String value;

        public SettingItem(String comment, String value) {
            this.comment = comment;
            this.value = value;
        }
    }

    private String settingsFile;

    private Map<String, SettingItem> map = new TreeMap<>();

    private String comment = null;

    private boolean changed = false;

    public Settings() {
        this(true);
    }

    public Settings(boolean loading) {
        this(loading, "settings");
    }

    public Settings(boolean loading, String baseName) {
        settingsFile = Application.getApplication().getHome() + "/" +
                baseName + ".pfc";
        if (loading) {
            init();
        }
        setChanged(false);
    }

    private void load(InputStream in) throws IOException {
        BufferedReader reader        = new BufferedReader(
                new InputStreamReader(in, ENCODING));
        String         line, comment = null;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith(COMMENT_LABEL)) {   // comment
                comment = line.substring(COMMENT_LABEL.length()).trim();
                continue;
            }
            int ix = line.indexOf(KEY_VALUE_SEPARATOR);
            if (ix == -1) {
                continue;
            }
            String key = line.substring(0, ix);
            String value = line.substring(ix + KEY_VALUE_SEPARATOR.length());
            map.put(key, new SettingItem(comment, value));
            comment = null;
        }
    }

    protected void init() {
        File file = new File(settingsFile);
        if (!file.exists()) {   // not exists, create new
            reset();
            sync();
            return;
        }
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            load(in);
        } catch (IOException e) {
            LOG.debug("load settings failed: " + settingsFile, e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOG.debug("close settings failed: " + settingsFile, e);
                }
            }
        }
    }

    protected void store(OutputStream out) throws IOException {
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(out, ENCODING));
        if (!isEmpty(comment)) {
            for (String line: comment.split("(\\r\\n)|(\\n)|(\\r)")) {
                writer.write(COMMENT_LABEL+" "+line.trim()+LINE_SEPARATOR);
            }
            writer.write(LINE_SEPARATOR);
        }
        for (Map.Entry<String, SettingItem> entry: map.entrySet()) {
            SettingItem item = entry.getValue();
            boolean hasComment = ! isEmpty(item.comment);
            if (hasComment) {
                writer.write(COMMENT_LABEL+" "+item.comment+LINE_SEPARATOR);
            }
            writer.write(entry.getKey()+KEY_VALUE_SEPARATOR+item.value+
                    LINE_SEPARATOR);
            if (hasComment) {
                writer.write(LINE_SEPARATOR);
            }
        }
        writer.flush();
    }

    private void ensureSettingsHomeExisted() {
        File dir = new File(settingsFile).getParentFile();
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                throw new RuntimeException(
                        "Cannot create settings home: "+dir.getAbsolutePath());
            }
        }
    }

    public void sync() {
        FileOutputStream out = null;
        try {
            ensureSettingsHomeExisted();
            out = new FileOutputStream(settingsFile);
            store(out);
        } catch (IOException e) {
            LOG.debug("save settings failed: " + settingsFile, e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    LOG.debug("close settings failed: " + settingsFile, e);
                }
            }
        }
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public void clear() {
        map.clear();
        setChanged(true);
    }

    public int settingCount() {
        return map.size();
    }

    public Set<String> settingNames() {
        return map.keySet();
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    protected String getProperty(String key) {
        SettingItem item = map.get(key);
        if (item == null) {
            return null;
        }

        return item.value;
    }

    protected void setProperty(String key, String value, String comment) {
        map.put(key, new SettingItem(comment, value));
        setChanged(true);
    }

    public String getString(String key, String defaultValue) {
        String str = getProperty(key);
        return ! isEmpty(str) ? str : defaultValue;
    }

    public void setString(String key, String str, String comment) {
        setProperty(key, str, comment);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String str = getProperty(key);
        if (isEmpty(str)) {
            return defaultValue;
        }
        return Boolean.parseBoolean(str);
    }

    public void setBoolean(String key, boolean value, String comment) {
        setProperty(key, Boolean.toString(value), comment);
    }

    public int getInteger(String key, int defaultValue) {
        String str = getProperty(key);
        if (isEmpty(str)) {
            return defaultValue;
        }
        try {
            return Integer.decode(str);
        } catch (NumberFormatException e) {
            LOG.debug("invalid integer format: "+str, e);
            return defaultValue;
        }
    }

    public void setInteger(String key, int n, String comment) {
        setProperty(key, Integer.toString(n), comment);
    }

    public double getReal(String key, double defaultValue) {
        String str = getProperty(key);
        if (isEmpty(str)) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            LOG.debug("invalid real format: "+str, e);
            return defaultValue;
        }
    }

    public void setReal(String key, double n, String comment) {
        setProperty(key, Double.toString(n), comment);
    }

    public Date getDate(String key, Date defaultValue) {
        String str = getProperty(key);
        if (isEmpty(str)) {
            return defaultValue;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        try {
            return sdf.parse(str);
        } catch (ParseException e) {
            LOG.debug("invalid date format: "+str, e);
            return defaultValue;
        }
    }

    public void setDate(String key, Date date, String comment) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        setProperty(key, sdf.format(date), comment);
    }

    public Locale getLocal(String key, Locale defaultLocale) {
        String str = getProperty(key);
        if (isEmpty(str)) {
            return defaultLocale;
        }
        return Locale.forLanguageTag(str.replace("_", "-"));
    }

    public void setLocale(String key, Locale locale, String comment) {
        setProperty(key, locale.toLanguageTag(), comment);
    }

    public void reset() {

    }
}
