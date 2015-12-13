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

import java.util.List;
import java.util.LinkedList;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import pw.phylame.imabw.app.Imabw;
import pw.phylame.imabw.app.Worker;
import pw.phylame.imabw.app.ui.Viewer;
import pw.phylame.imabw.app.ui.dialog.WaitingWork;

public class ReadBook extends WaitingWork<List<ParseResult>, ParseResult>
        implements PropertyChangeListener {
    private static final Imabw app = Imabw.sharedInstance();

    public static final String START_ITEM = "start-item";

    protected String title;

    protected Viewer viewer;
    protected Worker worker;

    protected ParserData[] pds;
    protected ParserData errpd;

    public ReadBook(String title, ParserData[] pds) {
        this.title = title;
        this.pds = pds;

        worker = Worker.sharedInstance();
        viewer = app.getForm();

        addPropertyChangeListener(this);
    }

    @Override
    protected List<ParseResult> doInBackground() throws Exception {
        LinkedList<ParseResult> result = new LinkedList<>();
        ParseResult pr;
        for (ParserData pd : pds) {
            try {
                firePropertyChange(START_ITEM, null, pd);
                pr = worker.readBook(pd);
                result.add(pr);
                publish(pr);
            } catch (Exception e) {
                errpd = pd;
                throw e;
            }
        }
        return result;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(START_ITEM)) {
            itemStarted((ParserData) evt.getNewValue());
        }
    }

    protected void itemStarted(ParserData pd) {
    }

    @Override
    protected void onFailure(Throwable error) {
        hideWaitingDialog();
        worker.showOpenError(viewer, title, errpd, error.getCause());
    }
}
