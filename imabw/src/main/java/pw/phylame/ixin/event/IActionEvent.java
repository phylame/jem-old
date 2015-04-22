/*
 * Copyright 2014 Peng Wan <phylame@163.com>
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

package pw.phylame.ixin.event;

import pw.phylame.ixin.com.IAction;

import java.util.EventObject;

/**
 * Event for {@code IAction}.
 */
public class IActionEvent extends EventObject {
    private IAction action;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public IActionEvent(Object source) {
        super(source);
    }

    public IActionEvent(Object source, IAction action) {
        super(source);
        this.action = action;
    }

    public IAction getAction() {
        return action;
    }
}
