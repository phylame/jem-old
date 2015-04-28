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

package pw.phylame.imabw.ui.com;

import javax.swing.*;

public class UIFactory {
    public static JLabel infoLabel(Object[][] info) {
        StringBuilder sb = new StringBuilder();

        sb.append("<html><table>");
        for (Object[] ls: info) {
            sb.append("<tr>");
            for (Object o: ls) {
                sb.append("<td>").append(o).append("</td>");
            }
            sb.append("</tr>");
        }
        sb.append("</table></html>");

        return new JLabel(sb.toString());
    }
}
