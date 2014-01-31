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
package org.opentravel.schemas.utils;

import java.util.Comparator;

public abstract class StringComparator<T> implements Comparator<T> {

    @Override
    public final int compare(T o1, T o2) {
        String str1 = getNotNullString(o1);
        String str2 = getNotNullString(o2);
        return str1.compareToIgnoreCase(str2);
    }

    private String getNotNullString(T object) {
        String str = getString(object);
        if (str == null) {
            return "";
        } else {
            return str;
        }
    }

    protected abstract String getString(T object);

}
