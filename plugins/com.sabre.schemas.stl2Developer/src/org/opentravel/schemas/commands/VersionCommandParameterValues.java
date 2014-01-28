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
