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

package pw.phylame.imabw.app.model;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import pw.phylame.jem.util.FileFactory;
import pw.phylame.imabw.app.Imabw;
import pw.phylame.imabw.app.config.AppConfig;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileHistory {
    private static final Log LOG = LogFactory.getLog(FileFactory.class);
    private static final Imabw app = Imabw.sharedInstance();
    private static final AppConfig config = AppConfig.sharedInstance();

    public static final String HISTORY_FILE = "history";
    public static final String HISTORY_ENCODING = "UTF-8";

    private static HashSet<String> checks = new HashSet<>();
    private static LinkedList<String> histories = new LinkedList<>();
    private static boolean modified = false;

    static {
        load();
        app.addCleanup(FileHistory::syncIfNeed);
    }

    private static File getHistoryFile() {
        return new File(app.getHome(), Imabw.SETTINGS_HOME + HISTORY_FILE);
    }

    private static void load() {
        if (!config.isHistoryEnable()) {
            return;
        }
        File file = getHistoryFile();
        // not exists
        if (!file.exists()) {
            return;
        }
        try {
            Iterator<String> iterator = FileUtils.lineIterator(file, HISTORY_ENCODING);
            while (iterator.hasNext()) {
                if (histories.size() > config.getHistoryLimits()) {
                    break;
                }
                String path = iterator.next();
                histories.addLast(path);
                checks.add(path);
            }
        } catch (IOException e) {
            LOG.error("cannot read history: " + file.getPath(), e);
        }
    }

    private static void syncIfNeed() {
        if (!config.isHistoryEnable()) {
            return;
        }
        if (!modified) {
            return;
        }
        File file = getHistoryFile();
        try {
            FileUtils.writeLines(file, HISTORY_ENCODING, histories);
        } catch (IOException e) {
            LOG.error("cannot write history: " + file.getPath(), e);
        }
    }

    public static Iterable<String> histories() {
        return histories;
    }

    public static void insert(File file, boolean updateMenu) {
        if (!config.isHistoryEnable()) {
            return;
        }
        if (file != null) {
            try {
                String path = file.getCanonicalPath();
                if (!checks.contains(path)) {
                    if (histories.size() > config.getHistoryLimits()) {
                        histories.removeLast();
                    }
                    histories.addFirst(path);
                    checks.add(path);
                    modified = true;
                }
            } catch (IOException e) {
                LOG.error("cannot add file to history: " + file, e);
            }
        }
        if (updateMenu) {
            updateHistoryMenu();
        }
    }

    public static void remove(File file, boolean updateMenu) {
        if (!config.isHistoryEnable()) {
            return;
        }
        if (file != null) {
            try {
                String path = file.getCanonicalPath();
                if (histories.remove(path)) {
                    checks.remove(path);
                    modified = true;
                }
            } catch (IOException e) {
                LOG.error("cannot remove file from history: " + file, e);
            }
        }
        if (updateMenu) {
            updateHistoryMenu();
        }
    }

    public static void clear(boolean updateMenu) {
        if (!config.isHistoryEnable()) {
            return;
        }
        if (!histories.isEmpty()) {
            histories.clear();
            checks.clear();
            modified = true;
        }
        if (updateMenu) {
            updateHistoryMenu();
        }
    }

    private static void updateHistoryMenu() {
        app.getForm().updateHistory();
    }
}
