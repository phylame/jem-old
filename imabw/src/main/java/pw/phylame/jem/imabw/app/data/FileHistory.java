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

package pw.phylame.jem.imabw.app.data;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import pw.phylame.jem.imabw.app.Imabw;
import pw.phylame.jem.imabw.app.config.AppSettings;
import pw.phylame.jem.util.FileFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileHistory {
    private static Log LOG = LogFactory.getLog(FileFactory.class);

    static final int MAX_COUNT = AppSettings.getInstance().getHistoryLimits();
    public static final String HISTORY_FILE = "history";
    public static final String HISTORY_ENCODING = "UTF-8";

    private static LinkedList<String> histories = new LinkedList<>();

    static {
        load();
        Imabw.getInstance().addExitHook(FileHistory::sync);
    }

    private static File getHistoryFile() {
        return new File(Imabw.getInstance().getHome(), Imabw.SETTINGS_HOME + HISTORY_FILE);
    }

    private static void load() {
        File file = getHistoryFile();
        // not exists
        if (! file.exists()) {
            return;
        }
        try {
            Iterator<String> it = FileUtils.lineIterator(file, HISTORY_ENCODING);
            while (it.hasNext()) {
                if (histories.size() > MAX_COUNT) {
                    break;
                }
                histories.addLast(it.next());
            }
        } catch (IOException e) {
            LOG.debug("cannot read history: "+file.getPath(), e);
        }
    }

    public static void sync() {
        File file = getHistoryFile();
        try {
            FileUtils.writeLines(file, HISTORY_ENCODING, histories);
        } catch (IOException e) {
            LOG.debug("cannot write history: " + file.getPath(), e);
        }
    }

    public static String lastFile() {
        if (histories.isEmpty()) {
            return null;
        }
        return histories.getFirst();
    }

    public static Iterable<String> iterator() {
        return histories;
    }

    public static void add(File file) {
        try {
            String path = file.getCanonicalPath();
            if (!histories.contains(path)) {
                if (histories.size() > MAX_COUNT) {
                    histories.removeLast();
                }
                histories.addFirst(path);
                updateMenu();
            }
        } catch (IOException e) {
            Imabw.getInstance().debug("cannot add file to history: "+file, e);
        }
    }

    private static void updateMenu() {
        Imabw.getInstance().getActiveViewer().updateHistoryMenu();
    }

    public static void remove(File file) {
        try {
            if (histories.remove(file.getCanonicalPath())) {
                updateMenu();
            }
        } catch (IOException e) {
            Imabw.getInstance().debug("cannot remove file from history: "+file, e);
        }
    }

    public static void clear() {
        histories.clear();
        updateMenu();
    }
}
