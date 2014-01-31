/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opentravel.schemas.controllers;

import java.util.List;

import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.stl2developer.MainWindow;
import org.opentravel.schemas.views.OtmView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dave Hollander
 * 
 */
public class OtmControllerBase implements OtmController {
    private static final Logger LOGGER = LoggerFactory.getLogger(OtmControllerBase.class);

    protected static MainController mc;
    private static MainWindow mainWindow;

    protected OtmControllerBase(MainController mwc) {
        this.mc = mwc;
        mainWindow = mwc.getMainWindow();
    }

    protected MainWindow getMainWindow() {
        return mainWindow;
    }

    protected MainController getMainWindowController() {
        return mc;
    }

    protected OtmView getView() {
        return mc.getDefaultView();
    }

    @Override
    public void refresh() {
        // TODO Auto-generated method stub

    }

    @Override
    public Node getSelected() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Node> getSelections() {
        // TODO Auto-generated method stub
        return null;
    }

}
