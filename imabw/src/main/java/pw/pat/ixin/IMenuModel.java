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

package pw.pat.ixin;

/**
 * Model of menu item.
 */
public class IMenuModel {

    /** Menu item type */
    public enum MenuType {
        PLAIN, RADIO, CHECK
    }

    /** ID of menu action */
    private String actionID;

    /** Type of this menu */
    private MenuType menuType;

    /** State of this menu */
    private boolean state;

    public IMenuModel(String actionID) {
        this(actionID, MenuType.PLAIN, false);
    }

    public IMenuModel(String actionID, MenuType menuType, boolean state) {
        this.actionID = actionID;
        this.menuType = menuType;
        this.state = state;
    }

    public String getID() {
        return actionID;
    }

    public MenuType getType() {
        return menuType;
    }

    public boolean getState() {
        return state;
    }
}
