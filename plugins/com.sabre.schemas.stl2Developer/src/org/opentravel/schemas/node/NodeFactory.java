/**
 * 
 */
package org.opentravel.schemas.node;

import org.opentravel.schemas.modelObject.TLValueWithAttributesFacet;
import org.opentravel.schemas.modelObject.TLnSimpleAttribute;
import org.opentravel.schemas.node.properties.AttributeNode;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.node.properties.ElementReferenceNode;
import org.opentravel.schemas.node.properties.EnumLiteralNode;
import org.opentravel.schemas.node.properties.IdNode;
import org.opentravel.schemas.node.properties.IndicatorElementNode;
import org.opentravel.schemas.node.properties.IndicatorNode;
import org.opentravel.schemas.node.properties.RoleNode;
import org.opentravel.schemas.node.properties.SimpleAttributeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sabre.schemacompiler.model.LibraryMember;
import com.sabre.schemacompiler.model.TLAlias;
import com.sabre.schemacompiler.model.TLAttribute;
import com.sabre.schemacompiler.model.TLAttributeType;
import com.sabre.schemacompiler.model.TLBusinessObject;
import com.sabre.schemacompiler.model.TLClosedEnumeration;
import com.sabre.schemacompiler.model.TLCoreObject;
import com.sabre.schemacompiler.model.TLEnumValue;
import com.sabre.schemacompiler.model.TLExtensionPointFacet;
import com.sabre.schemacompiler.model.TLFacet;
import com.sabre.schemacompiler.model.TLIndicator;
import com.sabre.schemacompiler.model.TLListFacet;
import com.sabre.schemacompiler.model.TLModelElement;
import com.sabre.schemacompiler.model.TLOpenEnumeration;
import com.sabre.schemacompiler.model.TLOperation;
import com.sabre.schemacompiler.model.TLProperty;
import com.sabre.schemacompiler.model.TLRole;
import com.sabre.schemacompiler.model.TLRoleEnumeration;
import com.sabre.schemacompiler.model.TLSimple;
import com.sabre.schemacompiler.model.TLSimpleFacet;
import com.sabre.schemacompiler.model.TLValueWithAttributes;

/**
 * Create Component Nodes of various sub-types.
 * 
 * @author Dave Hollander
 * 
 */
public class NodeFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeFactory.class);

    /*******************************************************************************************
     * New ComponentNode methods that also create new objects in underlying model
     */

    /**
     * Create a new component. Assigns types to all of its properties based on TL object and/or
     * documentation for XSD derived nodes. Unlike constructors, the factory method also assigns the
     * type node.
     * 
     * @param mbr
     * @return
     */
    public static ComponentNode newComponent(LibraryMember mbr) {
        ComponentNode newNode = newComponent_UnTyped(mbr);
        if (newNode.isTypeUser())
            newNode.getTypeClass().setAssignedTypeForThisNode(newNode);
        for (Node n : newNode.getDescendants_TypeUsers()) {
            n.getTypeClass().setAssignedTypeForThisNode(n);
        }
        return newNode;
    }

    /**
     * Create a new component and use the type node to set its type.
     * 
     * @param mbr
     * @param type
     * @return newly created and typed node.
     */
    public static ComponentNode newComponent(LibraryMember mbr, INode type) {
        ComponentNode newNode = newComponent_UnTyped(mbr);
        if (newNode.isTypeUser())
            newNode.setAssignedType((Node) type, false);
        return newNode;
    }

    /*******************************************************************************************
     * New ComponentNode methods that also create new objects in underlying model
     */
    public static ComponentNode newComponent_UnTyped(final LibraryMember mbr) {
        ComponentNode cn = null;
        if (mbr == null)
            return cn;

        if (mbr instanceof TLValueWithAttributes)
            cn = new VWA_Node(mbr);
        else if (mbr instanceof TLBusinessObject)
            cn = new BusinessObjectNode(mbr);
        else if (mbr instanceof TLCoreObject)
            cn = new CoreObjectNode(mbr);
        else if (mbr instanceof TLSimple)
            cn = new SimpleTypeNode(mbr);
        else if (mbr instanceof TLOpenEnumeration)
            cn = new EnumerationOpenNode(mbr);
        else if (mbr instanceof TLClosedEnumeration)
            cn = new EnumerationClosedNode(mbr);
        else if (mbr instanceof TLExtensionPointFacet)
            cn = new ExtensionPointNode(mbr);
        else {
            cn = new ComponentNode(mbr);
            LOGGER.debug("Using default factory type for " + mbr.getClass().getSimpleName());
        }

        return cn;
    }

    /**
     * Creates a member of a top level component and properties.
     * 
     * @param parent
     *            is the top-level component used for properties, can be null
     * @param tlObj
     *            is TL model object to create member from
     * @return the newly created and modeled node
     */
    public static ComponentNode newComponentMember(INode parent, Object tlObj) {
        ComponentNode nn = null;
        // LOGGER.debug("adding "+ tlObj.getClass().getCanonicalName());
        // Properties
        if (tlObj instanceof TLProperty) {
            if (((TLProperty) tlObj).isReference())
                nn = new ElementReferenceNode((TLModelElement) tlObj, parent);
            else
                nn = new ElementNode((TLModelElement) tlObj, parent);
        } else if (tlObj instanceof TLIndicator) {
            if (((TLIndicator) tlObj).isPublishAsElement())
                nn = new IndicatorElementNode((TLModelElement) tlObj, parent);
            else
                nn = new IndicatorNode((TLModelElement) tlObj, parent);
        } else if (tlObj instanceof TLAttribute) {
            TLAttributeType type = ((TLAttribute) tlObj).getType();
            if (type != null && type.getNamespace() != null
                    && type.getNamespace().equals(Node.XSD_NAMESPACE)
                    && type.getLocalName().equals("ID"))
                nn = new IdNode((TLModelElement) tlObj, parent);
            else
                nn = new AttributeNode((TLModelElement) tlObj, parent);
        } else if (tlObj instanceof TLRole) {
            nn = new RoleNode((TLRole) tlObj, (RoleFacetNode) parent);
        } else if (tlObj instanceof TLEnumValue) {
            nn = new EnumLiteralNode((TLModelElement) tlObj, parent);
        } else if (tlObj instanceof TLnSimpleAttribute) {
            nn = new SimpleAttributeNode((TLModelElement) tlObj, parent);
            // Aliases
        } else if (tlObj instanceof TLAlias) {
            nn = new AliasNode((Node) parent, (TLAlias) tlObj);
            // Facets
        } else if (tlObj instanceof TLValueWithAttributesFacet) {
            nn = new FacetNode((TLValueWithAttributesFacet) tlObj);
        } else if (tlObj instanceof TLFacet) {
            nn = createFacet((TLFacet) tlObj);
        } else if (tlObj instanceof TLListFacet) {
            nn = new FacetNode((TLListFacet) tlObj);
        } else if (tlObj instanceof TLSimpleFacet) {
            nn = new SimpleFacetNode((TLSimpleFacet) tlObj);
        } else if (tlObj instanceof TLRoleEnumeration) {
            nn = new RoleFacetNode((TLRoleEnumeration) tlObj);
        } else if (tlObj instanceof TLOperation) {
            nn = new OperationNode((TLOperation) tlObj);
            // Others
        } else if (tlObj instanceof TLModelElement) {
            nn = new ComponentNode((TLModelElement) tlObj);
            // LOGGER.debug("newComponentNode() - generic source TLModelElement type. "
            // + tlObj.getClass().getSimpleName());
        }
        if (parent != null && nn.getParent() == null) {
            ((Node) parent).linkChild(nn, false);
            nn.setLibrary(parent.getLibrary());
        }

        return nn;
    }

    private static ComponentNode createFacet(TLFacet facet) {
        switch (facet.getFacetType()) {
            case CUSTOM:
            case QUERY:
                return new RenamableFacet(facet);
            default:
                return new FacetNode(facet);
        }
    }
}
