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

package pw.phylame.jem.imabw.app.ui.dialog;

import javax.swing.SwingWorker;

public abstract class WaitingWork<T, V> extends SwingWorker<T, V> {
    private WaitingDialog dialog;

    void setDialog(WaitingDialog dialog) {
        this.dialog = dialog;
    }

    protected void hideWaitingDialog() {
        dialog.setVisible(false);
    }

    protected void updateTipText(String text) {
        dialog.setTipText(text);
    }

    protected void updateWaitingText(String text) {
        dialog.setWaitingText(text);
    }

    protected void updateProgress(int v) {
        dialog.setProgress(v);
    }
}
