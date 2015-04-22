/*
 * Copyright 2015 Peng Wan <phylame@163.com>
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

package pw.phylame.imabw.ui;

import pw.phylame.imabw.Application;
import pw.phylame.imabw.Constants;
import pw.phylame.imabw.ui.com.MainPane;
import pw.phylame.ixin.com.IPaneRender;
import pw.phylame.ixin.frame.IFrame;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * The viewer of Imabw.
 * <p>Viewer is the main frame of Imabw</p>
 */
public class Viewer extends IFrame {
    static {
        IFrame.setActionsModel(UIDesign.MENU_ACTIONS);
        IFrame.setMenuBarModel(UIDesign.MENU_BAR_MODEL);
        IFrame.setToolBarModel(UIDesign.TOOL_BAR_MODEL);
    }

    /** The application */
    private Application app = Application.getApplication();

    public Viewer() {
        super();
        createComponent();
        init();
    }

    private void createComponent() {
    }

    private void init() {
        setTitle(app.getText("App.Name"));
        setIconImage(pw.phylame.ixin.IToolkit.createImage(app.getText("App.Icon")));

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onAction(Constants.EXIT_APP);
            }
        });

        setSize(1066, 600);
        setLocationRelativeTo(null);
    }

    public void showOrHideSideBar() {
        IPaneRender paneRender = getPaneRender();
        if (paneRender instanceof MainPane) {
            ((MainPane) paneRender).showOrHideSideBar();
        }
    }

    @Override
    public String getText(String key, Object... args) {
        return app.getText(key, args);
    }

    @Override
    public void onAction(Object actionID) {
        app.onCommand(actionID);
    }
}
