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
package org.opentravel.schemas.navigation;

import java.util.Collection;
import java.util.TreeSet;

import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.ISelectionListener;
import org.opentravel.schemas.navigation.DefaultNavigationService.IdSelectionChangedEvent;

public interface INavigationService extends ISelectionListener {

    public interface ISelectionHandler {

        void setSelection(TreeSet<IdSelectionChangedEvent> events);

    }

    public abstract Collection<SelectionChangedEvent> getBackwardEvent();

    public abstract Collection<SelectionChangedEvent> getForwardEvent();

    public abstract Collection<SelectionChangedEvent> getCurrentSelection();

    public abstract void goBackward();

    public abstract void goForward();

    public abstract void setSelectionHandler(ISelectionHandler handler);

}