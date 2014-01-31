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
package org.opentravel.schemas.properties;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author Agnieszka Janowska
 * 
 */
public class DefaultStringProperties implements StringProperties {

    private final Map<PropertyType, String> properties;

    public DefaultStringProperties(final Map<PropertyType, String> properties) {
        this.properties = new EnumMap<PropertyType, String>(properties);
    }

    /**
	 *
	 */
    public DefaultStringProperties() {
        properties = new EnumMap<PropertyType, String>(PropertyType.class);
    }

    @Override
    public String get(final PropertyType propType) {
        return properties.get(propType);
    }

    @Override
    public StringProperties set(final PropertyType propType, final String value) {
        properties.put(propType, value);
        return this;
    }
}
