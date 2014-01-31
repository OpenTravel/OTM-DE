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
/**
 * 
 */
package org.opentravel.schemas.commands;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.IParameterValues;

/**
 * @author Dave Hollander
 * 
 */
public class VersionCommandParameterValues implements IParameterValues {

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.commands.IParameterValues#getParameterValues()
     */
    @Override
    public Map getParameterValues() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("Major", "stl2Developer.Version.Major");
        params.put("Minor", "stl2Developer.Version.Minor");
        params.put("Patch", "stl2Developer.Version.Patch");
        return params;
    }

}
