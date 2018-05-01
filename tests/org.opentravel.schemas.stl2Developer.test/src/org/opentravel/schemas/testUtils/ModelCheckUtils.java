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
package org.opentravel.schemas.testUtils;

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opentravel.schemacompiler.model.LibraryMember;

/**
 * Creates a mock library in the runtime-OT2Editor.product directory. Is added to the passed
 * project.
 * 
 * @author Dave Hollander
 * 
 */
public class ModelCheckUtils {
    static final Logger LOGGER = LoggerFactory.getLogger(ModelCheckUtils.class);

    /**
     * Check the number of named types in gui model against those in the TL Library model.
     * 
     * @param lib
     * @return
     */
    public static boolean checkModelCounts(final LibraryNode lib) {
        int tlCount = 0, guiCount = 0;
        guiCount = lib.getDescendants_LibraryMembersAsNodes().size();
        tlCount = lib.getTLaLib().getNamedMembers().size();
        if (guiCount != tlCount) {
            LOGGER.error("GUI member count" + guiCount + " is out of sync with TL model " + tlCount
                    + ".");
            return false;
        }
        // LOGGER.debug(lib + " has " + guiCount + " children.");
        return true;
    }

    public static boolean compareModels(LibraryNode lib) {
        List<Node> members = lib.getDescendants_LibraryMembersAsNodes();
        List<String> memberNames = new ArrayList<String>();
        for (Node m : members)
            memberNames.add(m.getName());
        for (LibraryMember tlm : lib.getTLaLib().getNamedMembers()) {
            if (memberNames.contains(tlm.getLocalName()))
                memberNames.remove(tlm.getLocalName());
            else
                LOGGER.error("Found tl model member not in GUI model: " + tlm.getLocalName());
        }
        if (!memberNames.isEmpty())
            for (String name : memberNames)
                LOGGER.error("Found GUI member not in TL model: " + name);
        return true;
    }

}
