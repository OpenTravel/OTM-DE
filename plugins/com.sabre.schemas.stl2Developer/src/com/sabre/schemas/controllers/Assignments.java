/**
 * 
 */
package com.sabre.schemas.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import com.sabre.schemas.node.Node;

/**
 * Manage finding and replacing assigned types including base types.
 * 
 * NOTE: this class creates a map of type assignments which can get out of sync with the actual
 * assignments.
 * 
 * @author Dave Hollander
 * 
 */
public class Assignments {
    protected ArrayList<Node> users = new ArrayList<Node>();

    private class Users {
        public Users(Node n) {
            users.add(n);
        }

        protected void add(Node n) {
            users.add(n);
        }
    }

    // Maps to hold the current model indexed by assignedType and assignedBase
    private static Map<QName, Users> typeMap = new HashMap<QName, Users>(Node.getNodeCount());
    private static Map<QName, Users> baseMap = new HashMap<QName, Users>(Node.getNodeCount());
    protected static boolean isDirty = true;

    public Assignments() {
        // Initialize the maps
        assert typeMap == null;
        initializeMaps();
    }

    private void initializeMaps() {
        isDirty = false;
        for (Node n : Node.getModelNode().getChildren()) {
            if (n.getTLTypeObject() != null) {
                QName qn = new QName(n.getTLTypeObject().getNamespace(), n.getTLTypeObject()
                        .getLocalName());
                if (typeMap.containsKey(qn)) {
                    typeMap.get(qn).add(n);
                } else
                    typeMap.put(qn, new Users(n));
            }
            if (n.getTLBaseType() != null) {
                QName qn = new QName(n.getTLTypeObject().getNamespace(), n.getTLTypeObject()
                        .getLocalName());
                if (typeMap.containsKey(qn)) {
                    typeMap.get(qn).add(n);
                } else
                    typeMap.put(qn, new Users(n));
            }
        }
    }
}
