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
package org.opentravel.schemas.stl2Developer.editor.internal.filters;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IFilter;

/**
 * @author Pawel Jedruch
 * 
 */
public class FilterManager implements IFilter {

    private List<IFilter> filters = new ArrayList<IFilter>();

    public void addFilter(IFilter filter) {
        filters.add(filter);
    }

    public void removeFilter(IFilter filter) {
        filters.remove(filter);
    }

    @Override
    public boolean select(Object toTest) {
        if (filters.isEmpty())
            return true;

        for (IFilter f : filters) {
            if (f.select(toTest))
                return true;
        }
        return false;
    }

}
