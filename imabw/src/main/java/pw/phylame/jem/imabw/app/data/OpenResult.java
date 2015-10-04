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
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Holds objects of open file dialog.
 */
public class OpenResult {
    private File[]      mFiles;
    private FileFilter  mFilter     = null;
    private String[]    mFormats    = null;

    public OpenResult(File[] files, FileFilter filter) {
        mFiles = files;
        mFilter = filter;
        if (mFilter instanceof FileNameExtensionFilter) {
            mFormats = ((FileNameExtensionFilter) mFilter).getExtensions();
        }
    }

    public File getFile() {
        if (mFiles == null || mFiles.length == 0) {
            return null;
        }

        return mFiles[0];
    }

    public File[] getFiles() {
        return mFiles;
    }

    public FileFilter getFileFilter() {
        return mFilter;
    }

    public String getFormat() {
        if (mFormats == null || mFormats.length == 0) {
            return null;
        }
        return mFormats[0];
    }

    public String[] getFormats() {
        return mFormats;
    }
}
