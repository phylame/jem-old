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

import pw.phylame.imabw.app.Imabw;
import pw.phylame.imabw.app.Worker;
import pw.phylame.imabw.app.ui.Viewer;
import pw.phylame.imabw.app.ui.dialog.WaitingWork;

public class WriteBook extends WaitingWork<File, Void> {
    protected String title;
    protected MakerData md;

    protected Viewer viewer;
    protected Worker worker;

    protected WriteBook(String title, MakerData md) {
        this.md = md;
        this.title = title;

        worker = Worker.sharedInstance();
        viewer = Imabw.sharedInstance().getForm();
    }

    @Override
    protected File doInBackground() throws Exception {
        viewer.getTabbedEditor().cacheAllTabs();
        worker.writeBook(md);
        return md.file;
    }

    @Override
    protected void onFailure(Throwable error) {
        hideWaitingDialog();
        worker.showSaveError(viewer, title, md, error.getCause());
    }
}
