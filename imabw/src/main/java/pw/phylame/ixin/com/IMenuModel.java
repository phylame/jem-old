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

package pw.phylame.ixin.com;

/**
 * Model of menu item.
 */
public class IMenuModel {

    /** Menu item type */
    public static enum MenuType {
        PLAIN, RADIO, CHECK
    }

    /** Id of menu action */
    private Object actionId;

    /** Type of this menu */
    private MenuType menuType;

    /** State of this menu */
    private boolean state;

    public IMenuModel(Object actionId) {
        this(actionId, MenuType.PLAIN, false);
    }

    public IMenuModel(Object actionId, MenuType menuType, boolean state) {
        this.actionId = actionId;
        this.menuType = menuType;
        this.state = state;
    }

    public Object getActionId() {
        return actionId;
    }

    public MenuType getMenuType() {
        return menuType;
    }

    public boolean getState() {
        return state;
    }
}
