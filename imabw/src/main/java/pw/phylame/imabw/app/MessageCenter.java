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

package pw.phylame.imabw.app;

import java.util.Date;
import java.util.LinkedList;
import java.text.SimpleDateFormat;

import pw.phylame.imabw.app.ui.Viewer;

/**
 * Message management.
 */
public class MessageCenter {
    public static final int MAX_COUNT = 360;
    public static final String DATE_FORMAT = "a h:m:s: ";

    private Viewer viewer;
    private SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);

    public MessageCenter(Viewer viewer) {
        this.viewer = viewer;
    }

    public void message(String message) {
        if (messages.size() < MAX_COUNT) {
            messages.add(sdf.format(new Date()) + message);
        }
        viewer.setStatusText(message);
    }

    private LinkedList<String> messages = new LinkedList<>();
}
