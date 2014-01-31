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
package org.opentravel.schemas.stl2Developer.editor.internal.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.opentravel.schemas.stl2Developer.editor.i18n.Messages;
import org.opentravel.schemas.stl2Developer.editor.internal.Features;
import org.opentravel.schemas.stl2Developer.editor.view.DependeciesView;

/**
 * @author Pawel Jedruch
 * 
 */
public class ToggleLayout extends Action {
    private DependeciesView view;

    public ToggleLayout(DependeciesView view) {
        super(getLabel(), IAction.AS_CHECK_BOX);
        this.view = view;
    }

    @Override
    public void run() {
        setText(getLabel());
        view.refresh();
    }

    private static String getLabel() {
        if (Features.isLayoutEnabled()) {
            return Messages.ToggleLayout_DisableLayout;
        } else {
            return Messages.ToggleLayout_EnableLayout;
        }
    }

    @Override
    public boolean isChecked() {
        return Features.isLayoutEnabled();
    }

    @Override
    public void setChecked(boolean checked) {
        Features.setLayoutEnabled(checked);
        super.setChecked(checked);
    }

}
