/**
 * 
 */
package com.sabre.schemas.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemacompiler.model.XSDSimpleType;
import com.sabre.schemas.modelObject.XsdModelingUtils;
import com.sabre.schemas.node.AliasNode;
import com.sabre.schemas.node.ComplexComponentInterface;
import com.sabre.schemas.node.INode;
import com.sabre.schemas.node.ImpliedNode;
import com.sabre.schemas.node.LibraryNode;
import com.sabre.schemas.node.ModelNode;
import com.sabre.schemas.node.Node;
import com.sabre.schemas.node.Node.NodeVisitor;
import com.sabre.schemas.node.XsdNode;

/**
 * Run across all nodes in the model and resolve types.
 * 
 * NOTE - it creates a static map of the node tree so it should NOT be retained after use. Future
 * development - use modelController to persist this map. Be very careful of all places where adding
 * or deleting nodes.
 * 
 * @author Dave Hollander
 * 
 */
public class TypeResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(TypeResolver.class);

    private final Map<QName, Node> providerMap = new HashMap<QName, Node>(Node.getNodeCount());

    public Map<QName, Node> getProviderMap() {
        return providerMap;
    }

    private int resolvedTypes = 0;
    private int unResolvedTypes = 0;
    // private int duplicates = 0;

    private int typeProviders = 0;
    private int typeUsers = 0;
    int visitCount = 0;

    /**
     * Create a type resolver which creates a map of all type providers. NOTE - it creates a static,
     * unmaintained map of the node tree so it should NOT be retained after use.
     */
    public TypeResolver() {
        LOGGER.debug("Begin map building ************************************");
        Node.getModelNode().visitAllTypeProviders(new addToMap());

        typeProviders = providerMap.size();
        // LOGGER.debug("Resolver map created with " + typeProviders + " providers available.");
        if (typeProviders != visitCount)
            LOGGER.warn("Not all visited nodes (" + visitCount + ") were added to the type map ("
                    + typeProviders + "). ");
        // if (duplicates > 0) {
        // LOGGER.debug("Duplicates: " + Node.getModelNode().getDuplicateTypes());
        // }
    }

    public class addToMap implements NodeVisitor {
        @Override
        public void visit(INode n) {
            QName qn = new QName(n.getNamespace(), n.getName());
            if (providerMap.put(qn, (Node) n) != null) {
                // duplicates++; // must be able to tolerate duplicate objects.
                // Node.getModelNode().addDuplicateType((Node) n);
                // LOGGER.debug("Duplicate found: " + qn);
            }
            visitCount++;
        }
    }

    public void clear() {
        providerMap.clear();
        resolvedTypes = 0;
    }

    public void delete() {
        clear();
    }

    /**
     * Resolved types across entire model.
     */
    public void resolveTypes() {
        ArrayList<LibraryNode> libs = new ArrayList<LibraryNode>(Node.getAllLibraries());
        resolveTypes(libs);
    }

    public void resolveTypes(LibraryNode lib) {
        ArrayList<LibraryNode> newLibs = new ArrayList<LibraryNode>();
        newLibs.add(lib);
        resolveTypes(newLibs);
    }

    public void resolveTypes(List<LibraryNode> newLibs) {
        boolean wasEditable = false;
        for (LibraryNode lib : newLibs) {
            wasEditable = lib.isEditable(); // Resolve all libraries, not just editable ones
            lib.setEditable(true);
            if (lib.isInChain()) {
                lib.getChain().visitAllTypeUsers(new resolveTypes());
                lib.getChain().visitAllBaseTypeUsers(new resolveBaseTypes());
            } else {
                lib.visitAllTypeUsers(new resolveTypes());
                lib.visitAllBaseTypeUsers(new resolveBaseTypes());
            }
            lib.setEditable(wasEditable);
        }

        LOGGER.debug("Visitor Resolver -- visited: " + typeUsers + "  Resolved: " + resolvedTypes
                + "  UnResolved: " + unResolvedTypes + "  Unassigned: "
                + ModelNode.getUnassignedNode().getTypeUsersCount());
    }

    private class resolveTypes implements NodeVisitor {
        @Override
        public void visit(INode in) {
            Node n = (Node) in;
            typeUsers++;

            if (n.getTypeClass().getTypeNode() != null
                    && !(n.getTypeClass().getTypeNode() instanceof ImpliedNode))
                return;

            // Do assignment and record success/failure
            if (isXSD_Atomic(n))
                n.getTypeClass().setTypeNode(ModelNode.getAtomicTypeNode());
            else if (!XsdModelingUtils.getAssignedXsdUnion(n).isEmpty())
                n.getTypeClass().setTypeNode(ModelNode.getUnionNode());
            else if (n.getTypeClass().setAssignedTypeForThisNode(n, providerMap))
                resolvedTypes++;
            else
                unResolvedTypes++;
        }
    }

    private class resolveBaseTypes implements NodeVisitor {
        @Override
        public void visit(INode in) {
            Node n = (Node) in;
            INode target = findBaseType(n, providerMap);
            if (target != null) {
                n.setExtendsType(target);
            }
        }
    }

    public static INode findBaseType(Node n, Map<QName, Node> providerMap) {
        QName typeQname = null;
        typeQname = new QName(n.getExtendsTypeNS(), n.getExtendsTypeName());
        INode in = providerMap.get(typeQname);
        if ((typeQname != null && !typeQname.getLocalPart().isEmpty()) && in == null)
            LOGGER.error("ERROR - missing node for qname: " + typeQname);
        return providerMap.get(typeQname);
    }

    /**
     * @return true if assigned type does not have JaxB type
     */
    private boolean isXSD_Atomic(Node n) {
        if (!n.isXsdType())
            return false;
        XsdNode xn = n.getXsdNode();
        if (xn != null)
            if (xn.getTLModelObject() != null)
                if (xn.getTLModelObject() instanceof XSDSimpleType)
                    if (((XSDSimpleType) xn.getTLModelObject()).getJaxbType() == null)
                        return true;
        return false;
    }

    /**
     * @param n
     *            - Node with assigned type
     * @return type of given node. For most of the nodes this method will return
     *         {@link Node#getTypeNode()}. For SimpleFacet it will return type of simple attribute.
     *         For alias it will return type of parent.
     */
    public static Node getNodeType(Node n) {
        Node type = n.getTypeNode();
        if (type == null)
            return null;

        if (type.isSimpleFacet()) {
            ComplexComponentInterface owner = (ComplexComponentInterface) type.getOwningComponent();
            return owner.getSimpleType();
        } else if (type.isAlias()) {
            AliasNode alias = (AliasNode) type;
            return alias.getOwningComponent();
        }
        return type;
    }

}
