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
import org.eclipse.jface.viewers.IFilter;
import org.opentravel.schemas.stl2Developer.editor.view.DependeciesView;

/**
 * @author Pawel Jedruch
 * 
 */
public class EnableFilterAction extends Action {

    private IFilter filter;
    private DependeciesView view;

    public EnableFilterAction(String text, IFilter filter, DependeciesView view) {
        super(text, IAction.AS_CHECK_BOX);
        this.filter = filter;
        this.view = view;
    }

    @Override
    public void run() {
        if (isChecked()) {
            view.addNodeFilter(filter);
        } else {
            view.removeNodeFilter(filter);
        }
    }

}
