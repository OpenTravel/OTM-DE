package org.opentravel.schemas.testUtils;

import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemacompiler.model.LibraryMember;

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
        guiCount = lib.getDescendants_NamedTypes().size();
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
        List<Node> members = lib.getDescendants_NamedTypes();
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
