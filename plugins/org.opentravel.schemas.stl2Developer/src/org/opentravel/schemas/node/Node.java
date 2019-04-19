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

package org.opentravel.schemas.node;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.opentravel.schemacompiler.codegen.CodeGenerationException;
import org.opentravel.schemacompiler.codegen.example.ExampleBuilder;
import org.opentravel.schemacompiler.codegen.example.ExampleDocumentBuilder;
import org.opentravel.schemacompiler.codegen.example.ExampleGeneratorOptions;
import org.opentravel.schemacompiler.event.ModelElementListener;
import org.opentravel.schemacompiler.model.LibraryElement;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.ModelElement;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttributeType;
import org.opentravel.schemacompiler.model.TLComplexTypeBase;
import org.opentravel.schemacompiler.model.TLContextReferrer;
import org.opentravel.schemacompiler.model.TLDocumentation;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLEquivalentOwner;
import org.opentravel.schemacompiler.model.TLExampleOwner;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.Validatable;
import org.opentravel.schemacompiler.validate.ValidationException;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.version.MinorVersionHelper;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.Versioned;
import org.opentravel.schemas.controllers.LibraryModelManager;
import org.opentravel.schemas.controllers.ValidationManager;
import org.opentravel.schemas.node.handlers.DocumentationHandler;
import org.opentravel.schemas.node.handlers.children.ChildrenHandlerI;
import org.opentravel.schemas.node.interfaces.ContextualFacetOwnerInterface;
import org.opentravel.schemas.node.interfaces.ExtensionOwner;
import org.opentravel.schemas.node.interfaces.FacadeInterface;
import org.opentravel.schemas.node.interfaces.FacetInterface;
import org.opentravel.schemas.node.interfaces.FacetOwner;
import org.opentravel.schemas.node.interfaces.INode;
import org.opentravel.schemas.node.interfaces.InheritedInterface;
import org.opentravel.schemas.node.interfaces.LibraryMemberInterface;
import org.opentravel.schemas.node.interfaces.SimpleMemberInterface;
import org.opentravel.schemas.node.interfaces.VersionedObjectInterface;
import org.opentravel.schemas.node.libraries.LibraryChainNode;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.node.listeners.BaseNodeListener;
import org.opentravel.schemas.node.listeners.INodeListener;
import org.opentravel.schemas.node.listeners.NodeIdentityListener;
import org.opentravel.schemas.node.objectMembers.ContributedFacetNode;
import org.opentravel.schemas.node.objectMembers.ExtensionPointNode;
import org.opentravel.schemas.node.objectMembers.FacetOMNode;
import org.opentravel.schemas.node.objectMembers.OperationFacetNode;
import org.opentravel.schemas.node.objectMembers.OperationNode;
import org.opentravel.schemas.node.properties.IValueWithContextHandler;
import org.opentravel.schemas.node.properties.PropertyNode;
import org.opentravel.schemas.node.properties.PropertyNodeType;
import org.opentravel.schemas.node.properties.SimpleAttributeFacadeNode;
import org.opentravel.schemas.node.resources.ResourceNode;
import org.opentravel.schemas.node.typeProviders.AbstractContextualFacet;
import org.opentravel.schemas.node.typeProviders.ContextualFacetNode;
import org.opentravel.schemas.node.typeProviders.ImpliedNode;
import org.opentravel.schemas.node.typeProviders.ImpliedNodeType;
import org.opentravel.schemas.node.typeProviders.QueryFacetNode;
import org.opentravel.schemas.node.typeProviders.RoleFacetNode;
import org.opentravel.schemas.properties.Images;
import org.opentravel.schemas.properties.Messages;
import org.opentravel.schemas.trees.type.TypeSelectionFilter;
import org.opentravel.schemas.types.ExtensionHandler;
import org.opentravel.schemas.types.TypeProvider;
import org.opentravel.schemas.types.TypeProviderAndOwners;
import org.opentravel.schemas.types.TypeUser;
import org.opentravel.schemas.types.WhereExtendedHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Main node structure for representing OTM objects.
 * 
 * For test validation, {@link visitAllNodes(NodeVisitors().new ValidateVisitor());}
 * 
 * @author Dave Hollander
 * 
 */
public abstract class Node implements INode {
    private static final Logger LOGGER = LoggerFactory.getLogger( Node.class );
    // See ImpliedNodeType
    public static final String UNDEFINED_PROPERTY_TXT = "Missing";
    // TO DO - eliminate root from node. It should be maintained by the model controller.
    protected static ModelNode root; // The root of the library catalog.

    protected static int nodeCount = 1; // used to assign nodeID
    protected String nodeID; // unique ID assigned to each node automatically

    // Facets and library members can be extended by an extension owner.
    protected WhereExtendedHandler whereExtendedHandler = null;

    // Any node that uses TLDocumentation will get a handler
    protected DocumentationHandler docHandler = null;

    // Children
    protected ChildrenHandlerI<?> childrenHandler = null;
    protected TLModelElement tlObj = null;

    // Ancestry
    protected Node parent; // link to the parentNode node
    protected VersionNode versionNode; // Link to the version node representing this node in a chain

    protected boolean deleted = false;

    @Deprecated
    public boolean xsdType = false; // True if this node represents an object that was created by
                                    // the XSD utilities but has not be imported.
                                    // @Deprecated

    /**
     * Public class for comparing nodes. Use: Collection.sort(list, node.new NodeComparable()) Uses node name and prefix
     * in the comparison.
     */
    public class NodeComparable implements Comparator<Node> {

        @Override
        public int compare(Node o1, Node o2) {
            return (o1.getNameWithPrefix().compareTo( o2.getNameWithPrefix() ));
        }
    }

    /**
     * Public class for comparing type providers. Uses node name and prefix in the comparison.
     * <p>
     * Use: Collection.sort(list, node.new TypeProviderComparable())
     */
    public class TypeProviderComparable implements Comparator<TypeProvider> {

        @Override
        public int compare(TypeProvider o1, TypeProvider o2) {
            return (((Node) o1).getNameWithPrefix().compareTo( ((Node) o2).getNameWithPrefix() ));
        }
    }

    /**
     * Visitors *********************************************
     * 
     * Sample Usage: thisModel.getModelNode().visitAllNodes(this.new count()); where: public class count implements
     * NodeVisitor {
     * 
     * NodeVisitor visitor = new NodeVisitors().new NodeVisitors().new ValidateNodeTypes();
     * curNode.visitAllNodes(visitor);
     */
    public interface NodeVisitor {
        public void visit(INode n);
    }

    /**
     * Within the node classes, public adders and setters are responsible for keeping the nodes and underlying library
     * model in sync.
     * 
     */

    /**
     * Static method to return all libraries in the model.
     * 
     * @return new list of library nodes for all libraries in the model
     */
    public static List<LibraryNode> getAllLibraries() {
        return getModelNode().getLibraries();
    }

    /**
     * Static method to return all projects in the model.
     * 
     * @return new list of projects nodes for all projects in the model
     */
    public static List<ProjectNode> getAllProjects() {
        return getModelNode().getProjects();
    }

    /**
     * Static method to return all libraries in the model.
     * 
     * @return new list of library nodes for all libraries in the model
     */
    public static List<LibraryNode> getAllUserLibraries() {
        return getModelNode().getUserLibraries();
    }

    /**
     * Get the static root model node.
     */
    public static ModelNode getModelNode() {
        return root;
    }

    /**
     * Get the static root library model manager.
     */
    public static LibraryModelManager getLibraryModelManager() {
        return root.getLibraryManager();
    }

    /**
     * @return - return the node from the identity listener from the collection tlObject's listeners. If a facade, get
     *         it's wrapped node. FacadeInterface nodes return the get() value.
     */
    static public Node GetNode(Collection<ModelElementListener> listeners) {
        Node n = null;
        for (ModelElementListener listener : listeners)
            if (listener instanceof NodeIdentityListener) {
                n = ((NodeIdentityListener) listener).getNode();
                break;
            }
        if (n instanceof FacadeInterface)
            n = ((FacadeInterface) n).get();
        return n;
    }

    // TODO - why are these static? why have both static and non-static?
    /**
     * @return - return the node from the tlObject's identity listener. If a facade, get it's wrapped node.
     */
    static public Node GetNode(ModelElement tlObj) {
        return tlObj != null ? GetNode( ((TLModelElement) tlObj).getListeners() ) : null;
    }

    /**
     * If the listener has a null node, don't use it for finding node.
     * 
     * @param listeners collection from the TL Object
     * @return the node associated with the first NodeIdentityListener.
     */
    public Node getNode(Collection<ModelElementListener> listeners) {
        Node n = null;
        for (ModelElementListener listener : listeners)
            if (listener instanceof NodeIdentityListener) {
                if (((NodeIdentityListener) listener).getNode() != null)
                    n = ((NodeIdentityListener) listener).getNode();
                break;
            }
        if (n instanceof FacadeInterface)
            n = ((FacadeInterface) n).get();
        return n;
    }

    /**
     * ******************************************************** Abstract Node Constructor
     */
    public Node() {
        parent = null;
        nodeID = Integer.toString( nodeCount++ );
        versionNode = null;
    }

    public Node(String identity) {
        this();
    }

    /**
     * Create a node containing modelObject for the tlObject and assign name and description fields. Can be used for
     * LibraryMember elements which are top level members of the library.
     * 
     * The model object will NEVER be null. It may contain an EmptyMO. Model object factory links in the TLModelElement
     * and sets edit-able flag
     */
    public Node(final TLModelElement tlModelObject) {
        this();

        tlObj = tlModelObject;
        if (isDocumentationOwner())
            docHandler = new DocumentationHandler( this );
    }

    /**
     * Can Assign - can the type be assigned to node?
     * 
     * @param type - the node representing the type to be assigned
     * @return - true if the assignment conforms to the rules.
     */
    // TODO - look at how the TLnSimpleAttribute uses exception.
    // This approach may simplify assignment logic.
    public boolean canAssign(Node type) {
        return false;
    }

    // /**
    // * Clone a node. Clone this node and all of its children. Creates new ModelObject, TL source object. Sets types
    // for
    // * all the properties. Creates type node. Types are assigned to this component. Clones the TL and Model objects.
    // * Must be a library member. Assigns libraries and types. Added to parent and a family node may be created to
    // * contain <i>this</i> and the clone.
    // *
    // * Note: the new component is <b>not</b> used to replace type users of this node (see {@link replaceTypesWith()}
    // *
    // * @param library
    // * to assign the new node to. If null, new node is not in a library.
    // * @param nameSuffix
    // * Append to the new node's name.
    // * @return
    // */
    // @Override
    // @Deprecated
    // public Node clone() {
    // return clone(this.getLibrary(), null);
    // }

    /**
     * Clone this node. If parent is null, the new node is only added to this library. If the parent is a library then
     * the new node is added to <i>this</i> node's parent adjacent to this node if a property. If this is a
     * namedMember() then the clone is added to this.library. Otherwise, parent is used to contain the new node.
     * 
     * @param parent - will try to use "this" parent if null
     * @param nameSuffix
     * @return null if error
     */
    // TODO - refactor to classes that implement cloneTLObj()
    @Deprecated
    public Node clone(Node parent, String nameSuffix) {
        LibraryNode lib = this.getLibrary();
        Node newNode = null;

        // // Use the compiler to create a new TL src object.
        TLModelElement newLM = (TLModelElement) cloneTLObj();
        if (newLM == null)
            return null;

        // Use the node factory to create the gui representation.
        if (this instanceof PropertyNode) {
            // REFACTORED
            assert false;
        } else if (newLM instanceof LibraryMember) {
            // REFACTORED to library member base
            assert false;
        } else {
            LOGGER.warn( "clone not supported for this node: " + this );
            assert false;
            return null;
        }

        return newNode;
    }

    public Node clone(String nameSuffix) {
        if (!(this instanceof ComponentNode))
            return null;
        if (this instanceof LibraryMemberInterface)
            return (Node) ((LibraryMemberInterface) this).clone( this.getLibrary(), nameSuffix );
        else if (this instanceof PropertyNode)
            return ((PropertyNode) this).clone( getParent(), nameSuffix );

        LOGGER.warn( "Invalid object type to clone: " + this.getClass().getSimpleName() );
        // assert false;
        return null;
    }

    /**
     * Use compiler to clone object including type. Resulting object has no owner or listeners.
     * 
     * @return the cloned copy of a TL Model object.
     */
    public LibraryElement cloneTLObj() {
        if (getLibrary() == null) {
            LOGGER.error( "Can not clone without having a library." );
            return null;
        }

        LibraryElement newLE = null;
        try {
            newLE = getTLModelObject().cloneElement( getLibrary().getTLModelObject() );
        } catch (IllegalArgumentException e) {
            LOGGER.warn( "Can not clone " + this + ". Exception: " + e.getLocalizedMessage() );
            newLE = null;
        }
        return newLE;
    }

    public TLModelElement getTLModelObject() {
        return tlObj;
    }

    @Override
    public void close() {
        if (getLibrary() != null)
            getLibrary().setEditable( true );
        this.visitAllNodes( new NodeVisitors().new closeVisitor() );
    }

    public String compileExampleXML(boolean quiet) {
        final ExampleBuilder<Document> exampleBuilder = new ExampleDocumentBuilder( new ExampleGeneratorOptions() )
            .setModelElement( (NamedEntity) this.getTLModelObject() );
        String xml = "ERROR";
        try {
            xml = exampleBuilder.buildString();
        } catch (ValidationException e) {
            if (!quiet)
                LOGGER.debug( "Validation Exception on " + this + " : " + e );
            for (String finding : e.getFindings().getAllValidationMessages( FindingMessageFormat.IDENTIFIED_FORMAT ))
                if (!quiet)
                    LOGGER.debug( "Finding: " + finding );
        } catch (CodeGenerationException e) {
            LOGGER.debug( "CodeGen Exception on " + this + " : " + e );
        }
        // LOGGER.debug("XML example generated: " + xml);
        return xml;
    }

    /**
     * Return true if the direct children (not inherited children) includes the candidate. If candidate is a contextual
     * facet, its matching where contributed node is used.
     */
    public boolean contains(Node candidate) {
        if (candidate instanceof ContextualFacetNode)
            candidate = ((ContextualFacetNode) candidate).getWhereContributed();
        return getChildren() != null ? getChildren().contains( candidate ) : false;
    }

    @Override
    public void delete() {
        // If a version-ed library, then also remove from aggregate
        // Library may be null! It is in some j-units.
        if (isDeleteable()) {
            NodeVisitor visitor = new NodeVisitors().new deleteVisitor();
            // LOGGER.debug("Deleting " + this);
            this.visitAllNodes( visitor );
        } else
            LOGGER.debug( "Not Deleteable: " + this );
    }

    public String emptyIfNull(String s) {
        return s == null ? "" : s;
    }

    /**
     * Find node under this node using TL Model object validation identity.
     * 
     * @param ID
     */
    public Node findNode(final String validationIdentity) {
        Node c;
        if (validationIdentity != null && !validationIdentity.isEmpty()) {
            for (final Node n : this.getChildren()) {
                if (n.getValidationIdentity().equals( validationIdentity )) {
                    return n;
                } else if ((c = n.findNode( validationIdentity )) != null) {
                    return c;
                }
            }
        }
        return null;
    }

    private String getValidationIdentity() {
        if (getTLModelObject() != null && getTLModelObject() instanceof Validatable)
            return ((Validatable) getTLModelObject()).getValidationIdentity();
        return "";
    }

    /**
     * find a named node starting from <i>this</i> node.
     */
    public Node findNode(final String name, String ns) {
        // LOGGER.debug("findNode() - Testing: " + this.getNamespace() + " : " + this);
        if (name == null || name.isEmpty())
            return null;
        if (ns == null || ns.isEmpty())
            ns = ModelNode.Chameleon_NS;
        // Get past the model node.
        Node x;
        if (this instanceof ModelNode)
            for (Node n : getAllLibraries())
                if ((x = n.findNode( name, ns )) != null)
                    return x;

        Node c = null;
        // test to see if the library is in the target namespace then just test
        // their kids for name
        // String nodeNS = getNamespace();
        if (!getNamespace().equals( ns ) && !(getNamespace().equals( ModelNode.Chameleon_NS )))
            return null;

        for (final Node n : getChildren()) {
            if (n.getName().equals( name ) && !n.isNavigation()) {
                // if (n instanceof XsdNode)
                // return ((XsdNode) n).getOtmModel();
                return n;

            } else if ((c = n.findNode( name, ns )) != null) {
                return c;
            }
        }
        return null;
    }

    /**
     * Find the first library member node in the named type descendants of this node with the given name. The order
     * searched is not guaranteed. Will not find family nodes.
     * 
     * @param name
     * @return node found or null
     */
    public Node findLibraryMemberByName(String name) {
        for (LibraryMemberInterface n : getDescendants_LibraryMembers()) {
            if (n.getName().equals( name ))
                return (Node) n;
        }
        return null;
    }

    /**
     * Find the first child, including inherited children, of this node with the given name. The order searched is not
     * guaranteed.
     * <p>
     * Contextual facets have prefix from owner, so this method matches contextual facets that end in name.
     * <p>
     * Warning - contextual facets may have multiple of the same name
     * 
     * @param name
     * @return node found or null
     */
    public Node findChildByName(String name) {
        List<Node> allKids = new ArrayList<>( getChildrenHandler().get() );
        allKids.addAll( getChildrenHandler().getInheritedChildren() );

        for (Node n : allKids) {
            if (n instanceof AbstractContextualFacet)
                if (n.getName().endsWith( name ))
                    return n;
            if (n instanceof ContributedFacetNode)
                if (n.getName().endsWith( name ))
                    return n;
            if (n.getName().equals( name ))
                return n;
        }
        return null;
    }

    /**
     * Find node under this node using ID value.
     * 
     * @param ID
     */
    public Node findNodeID(final String ID) {
        Node kid = null;
        for (final Node n : getChildren()) {
            if (n.nodeID.equals( ID )) {
                return n; // this is it.
            }
            if ((kid = n.findNodeID( ID )) != null) {
                return kid; // recurse to check children
            }
        }
        return null;
    }

    /**
     * Assure this node and all its descendants use the default context id.
     */
    public void fixContexts() {
        // 3/23/2017 - simplified context handling to just set to new library default.
        if (getLibrary() == null)
            return;
        String ctx = getLibrary().getDefaultContextId();
        for (Node child : getDescendants())
            if (child.getTLModelObject() instanceof TLContextReferrer)
                ((TLContextReferrer) child.getTLModelObject()).setContext( ctx );

        assert getLibrary().getTLLibrary().getContexts().size() == 1;
    }

    @Override
    public INode.CommandType getAddCommand() {
        return INode.CommandType.NONE;
    }

    /**
     * Get list of ancestors by traversing the parents of each node.
     * 
     * @return
     */
    public List<Node> getAncestors() {
        List<Node> ancestors = new ArrayList<>();
        Node n = this;
        do {
            ancestors.add( n );
            n = n.getParent();
        } while (!(n instanceof ModelNode));
        return ancestors;
    }

    /**
     * Return the single node that represents this object. Null if none. For VWA and Core, the simple property node is
     * returned.
     * 
     * @return
     */
    public Node getAssignable() {
        return null;
    }

    /**
     * Return the library chain that this node belongs to.
     * 
     * @return the chain or null if not in a chain.
     */
    public LibraryChainNode getChain() {
        return getLibrary() != null ? getLibrary().getChain() : null;
    }

    /**
     * Get Component Node Type
     * 
     * @return the enumerated value associated with this node or null
     */
    public ComponentNodeType getComponentNodeType() {
        return null;
    }

    @Override
    public String getComponentType() {
        return getComponentNodeType() == null ? "" : getComponentNodeType().getDescription();
    }

    /**
     * 
     * @return non-empty string
     */
    public String getDecoration() {
        // if it is a named entity in a versioned library get version.
        String decoration = " ";

        // The number of users for this type provider
        if (this instanceof TypeProvider && !(this instanceof ImpliedNode))
            decoration += " (" + ((TypeProvider) this).getWhereUsedCount() + " users)";

        if (isDeleted()) {
            decoration += " (Deleted) ";
            return decoration;
        }

        if (isDeprecated())
            decoration += " (Deprecated)";

        if (this instanceof InheritedInterface)
            decoration += "  Inherited from " + getInheritedFrom().getNameWithPrefix();

        // Extension
        if (this instanceof ExtensionOwner) {
            String extensionTxt = "";

            // The only true extension will be to the oldest version of this object.
            // All other extensions will be for versions.
            ComponentNode exBase = (ComponentNode) getExtendsType();
            if (exBase != null) {
                extensionTxt += "Extends: " + exBase.getNameWithPrefix() + " ";
                if (getChain() != null)
                    extensionTxt += " - ";
            }
            // Version and edit-ability
            if (getChain() != null) {
                // if (versionBase != null)
                if (isInHead())
                    if (getLibrary().isMajorVersion())
                        extensionTxt += "Major Version";
                    else if (isNewToChain())
                        extensionTxt += "New to this version";
                    else
                        extensionTxt += "Current Version";
                else
                    extensionTxt += "Version: " + getTlVersion();
                if (isEditable())
                    extensionTxt += " - ";
            }
            if (isEditable()) {
                if (getChain() == null) // Not in chain
                    extensionTxt += "Full Editing";
                else if (isInHead())
                    if (isNewToChain())
                        extensionTxt += "Full Editing"; // Is newly added to chain
                    else
                        extensionTxt += "Minor Editing";
                else if (this instanceof VersionedObjectInterface)
                    extensionTxt += "Minor Editing will create new version";
            }
            decoration += surround( extensionTxt );
        }
        return decoration.isEmpty() ? " " : decoration;
    }

    public String surround(String txt) {
        if (txt != null && !txt.isEmpty()) {
            return " (" + txt + ")";
        }
        return "";
    }

    public FacetInterface getFacet_Default() {
        return this instanceof FacetOwner ? ((FacetOwner) this).getFacet_Default() : null;
    }

    /**
     * @return a new list containing all children nodes and their descendants. No filtering; includes aggregate, version
     *         and navNodes.
     */
    public List<Node> getDescendants() {
        return new NodeDescendantHandler().getDescendants( this );
    }

    /**
     * Gets all the types assigned to this type and all the types assigned to those types, etc. Sends back a list of
     * unique types. Types used recursively are only added to the list once.
     * 
     * @param currentLibraryOnly - only list types in this library.
     * @return new list of assigned types or empty list.
     */
    public List<Node> getDescendants_AssignedTypes(boolean currentLibraryOnly) {
        return new NodeDescendantHandler().getDescendants_AssignedTypes( this, currentLibraryOnly );
    }

    /**
     * @return new list of all descendants that are extension owners.
     */
    public List<ExtensionOwner> getDescendants_ExtensionOwners() {
        return new NodeDescendantHandler().getDescendants_ExtensionOwners( this );
    }

    public List<ContextualFacetOwnerInterface> getDescendants_ContextualFacetOwners() {
        return new NodeDescendantHandler().getDescendants_ContextualFacetOwners( this );
    }

    public List<ContributedFacetNode> getDescendants_ContributedFacets() {
        return new NodeDescendantHandler().getDescendants_ContributedFacets( this );
    }

    /**
     * 
     * @return new list of all contextual facets including contributed facets
     */
    public List<ContextualFacetNode> getDescendants_ContextualFacets() {
        return new NodeDescendantHandler().getDescendants_ContextualFacets( this );
    }

    /**
     * return new list of NamedEntities. Traverse via hasChildren. For version chains, it returns the newest version
     * using the version node and does not touch aggregates.
     */
    @Override
    @Deprecated
    public List<Node> getDescendants_LibraryMembersAsNodes() {
        // keep duplicates out of the list that version aggregates may introduce
        HashSet<Node> namedKids = new HashSet<>();
        if (getChildrenHandler() != null)
            for (LibraryMemberInterface c : getDescendants_LibraryMembers())
                namedKids.add( (Node) c );
        return new ArrayList<>( namedKids );

    }

    /**
     * return new list of Library Members. Traverse via hasChildren. For version chains, it returns the newest version
     * using the version aggregate node and does not touch other aggregates.
     */
    public List<LibraryMemberInterface> getDescendants_LibraryMembers() {
        return new NodeDescendantHandler().getDescendants_LibraryMembers( this );
    }

    /**
     * Gets the descendants that are type SimpleComponentNode. Does not return navigation nodes.
     * 
     * @return new list of all descendants that simple components.
     */
    public ArrayList<SimpleMemberInterface> getDescendants_SimpleMembers() {
        return new NodeDescendantHandler().getDescendants_SimpleMembers( this );
    }

    /**
     * Get all resources in the model.
     */
    public List<ResourceNode> getAllResources() {
        final ArrayList<ResourceNode> resources = new ArrayList<>();
        for (final LibraryNode ln : getModelNode().getLibraries())
            for (final Node n : ln.getResourceRoot().getChildren())
                if (n instanceof ResourceNode)
                    resources.add( (ResourceNode) n );
        return resources;
    }

    /**
     * Gets the descendants that are type providers (can be assigned as a type). Does not return navigation nodes.
     * 
     * @return new list of all descendants that can be assigned as a type.
     */
    public List<TypeProvider> getDescendants_TypeProviders() {
        return new NodeDescendantHandler().getDescendants_TypeProviders( this );
    }

    /**
     * Gets the descendants that are type users (can be assigned a type). Does not return navigation nodes.
     * {@link #getChildren_TypeUsers() Use getChildren_TypeUsers() for only immediate children.}
     * 
     * @return new list of all descendants that can be assigned a type.
     */
    public List<TypeUser> getDescendants_TypeUsers() {
        return new NodeDescendantHandler().getDescendants_TypeUsers( this );
    }

    public String getDescription() {
        return docHandler != null ? docHandler.getDescription() : "";
    }

    public DocumentationHandler getDocHandler() {
        if (docHandler == null && isDocumentationOwner())
            docHandler = new DocumentationHandler( this );
        return docHandler;
    }

    public TLDocumentation getDocumentation() {
        return getDocHandler() != null ? docHandler.getOrNewTL() : null;
    }

    /**
     * Get the editing status of the node based on chain head library or unmanaged library. Use to check if an object
     * can be acted upon by the user.
     * 
     * To find out the specific status of the actual library and not the chain, see {@link LibraryNode#getEditStatus()}
     */
    public NodeEditStatus getEditStatus() {
        NodeEditStatus status = NodeEditStatus.FULL;
        if (getLibrary() == null)
            return status; // if there is no library, allow anything.

        if (getChain() == null) {
            if (getLibrary().isEditable())
                status = NodeEditStatus.FULL;
            else
                status = NodeEditStatus.NOT_EDITABLE;
        } else {
            if (getChain().getHead() == null)
                status = NodeEditStatus.NOT_EDITABLE;
            else if (!getChain().isEditable())
                status = NodeEditStatus.MANAGED_READONLY;
            else if (getChain().getHead().isMajorVersion())
                status = NodeEditStatus.FULL;
            else if (getChain().getHead().isMinorOrMajorVersion())
                status = NodeEditStatus.MINOR;
            else
                status = NodeEditStatus.PATCH;
        }
        // LOGGER.debug(this + " library has " + status + " edit status.");
        return status;
    }

    public String getEditStatusMsg() {
        return Messages.getString( getEditStatus().msgID() );
    }

    @Override
    public IValueWithContextHandler getEquivalentHandler() {
        return null;
    }

    @Override
    public IValueWithContextHandler getExampleHandler() {
        return null;
    }

    /**
     * Return the actual extension base object or null. Will not return objects that are using extension for version
     * relationships. This method will examine the whole chain to find the oldest version of the object and return its
     * base type if any.
     * 
     * @see ExtensionOwner#getExtensionBase()
     */
    public Node getExtendsType() {
        if (this instanceof ExtensionOwner) {
            Node oldestVersion = this;
            if (getVersionNode() != null)
                oldestVersion = getVersionNode().getOldestVersion();
            if (oldestVersion instanceof ExtensionOwner && !oldestVersion.isVersioned())
                return ((ExtensionOwner) oldestVersion).getExtensionBase();
        }
        return null;
    }

    /**
     * Find the actual extension object and return its name. Will not find objects that are using extension for version
     * relationships.
     * 
     * @see #getExtendsType()
     * @return name of the extension entity or empty string
     */
    public String getExtendsTypeName() {
        return getExtendsType() != null ? getExtendsType().getName() : "";
    }

    @Override
    public Image getImage() {
        final ImageRegistry imageRegistry = Images.getImageRegistry();
        return imageRegistry.get( "file" );
    }

    public List<Node> getInheritedChildren() {
        if (getChildrenHandler() != null)
            return genericToNode( getChildrenHandler().getInheritedChildren() );
        return Collections.emptyList();
    }

    @Deprecated
    public Node getInheritedFrom() {
        return null;
    }

    /**
     * Label is posted in second column of facet table.
     */
    @Override
    public String getLabel() {
        return getName();
    }

    /**
     * True if unversioned or versioned and the latest version.
     * 
     * @return
     */
    public boolean isLatestVersion() {
        if (getVersionNode() == null)
            return true;
        return getOwningComponent().getVersionNode().getNewestVersion() == this;
        // return false;
    }

    /**
     * Returns true if the other's Library meets both of the following conditions:
     * <ul>
     * <li>The other library is assigned to the same version scheme and base namespace as this one.</li>
     * <li>The version of the other library is considered to be later than this library's version according to the
     * version scheme.</li>
     * </ul>
     * 
     * @see org.opentravel.schemacompiler.model.AbstractLibrary.isLaterVersion
     * 
     * @param other
     * @return boolean
     */
    public boolean isLaterVersion(Node other) {
        if (getLibrary() == null || other.getLibrary() == null)
            return false;
        return getLibrary().getTLModelObject().isLaterVersion( other.getLibrary().getTLModelObject() );
    }

    /**
     * Use the Minor version helper to get later versions of the assigned type.
     * 
     * @return for the type assigned to this node, return a list of later versions in a minor chain or null.
     */
    public List<Node> getLaterVersions() {
        if (!(this instanceof TypeUser))
            return null;
        Node assignedType = (Node) ((TypeUser) this).getAssignedType();
        if (assignedType == null || assignedType instanceof ImpliedNode)
            return null;
        if (!(assignedType.getTLModelObject() instanceof Versioned))
            return null;

        List<Versioned> versions = null;
        List<Node> vNodes = new ArrayList<>();
        try {
            versions = new MinorVersionHelper().getLaterMinorVersions( (Versioned) assignedType.getTLModelObject() );
            for (Versioned v : versions) {
                for (ModelElementListener l : ((TLModelElement) v).getListeners())
                    if (l instanceof INodeListener)
                        vNodes.add( ((INodeListener) l).getNode() ); // could be duplicates if multiple listeners
            }
        } catch (VersionSchemeException e) {
            LOGGER.debug( "Error: " + e.getLocalizedMessage() );
            return null;
        }
        return vNodes.isEmpty() ? null : vNodes;
    }

    // /**
    // * Get all libraries under <i>this</i> node. Note - only searches library containers. Libraries in the tree with
    // an
    // * ancestor that is not a library container will not be found. Returns libraries in chains, not the chain.
    // *
    // * <p>
    // * TODO - this returns multiple copies of the same node if in chain!
    // * <p>
    // * Use ProjectNode.getLibraries()
    // *
    // * @return new list of library nodes.
    // */
    // @Deprecated
    // public List<LibraryNode> getLibraries() {
    // ArrayList<LibraryNode> libs = new ArrayList<LibraryNode>();
    // if (getChildrenHandler() == null)
    // return libs;
    //
    // for (Node n : getChildrenHandler().get()) {
    // if (n instanceof LibraryNode)
    // libs.add((LibraryNode) n);
    // else if (n.isLibraryContainer())
    // libs.addAll(n.getLibraries());
    // }
    // return libs;
    // }

    @Override
    public LibraryNode getLibrary() {
        return getOwningComponent() != null ? getOwningComponent().getLibrary() : null;
    }

    /**
     * Name is posted in first column of facet table.
     */
    @Override
    public abstract String getName();

    @Override
    public String getPrefix() {
        return getLibrary() == null ? "" : getLibrary().getPrefix();
    }

    @Override
    public String getNamespace() {
        return getLibrary() == null ? "" : getLibrary().getNamespace();
    }

    public String getNamespaceWithPrefix() {
        return getLibrary() == null ? "" : getLibrary().getNamespaceWithPrefix();
    }

    public String getNameWithPrefix(String padding) {
        String prefix = "";
        if (getLibrary() == null) {
            // owning library might have been closed
            if (getTLModelObject() instanceof NamedEntity
                && ((NamedEntity) getTLModelObject()).getOwningLibrary() != null)
                prefix = ((NamedEntity) getTLModelObject()).getOwningLibrary().getPrefix();
        } else
            prefix = getLibrary().getPrefix();
        return prefix + padding + ":" + padding + getName();
    }

    @Override
    public String getNameWithPrefix() {
        return getNameWithPrefix( " " );
    }

    /**
     * The string to present in the navigator tree and other library trees. Is label unless overridden.
     * 
     * @return
     */
    public String getNavigatorName() {
        return getLabel();
    }

    /**
     * Get a new listener for this type of node.
     * 
     * @return
     */
    public BaseNodeListener getNewListener() {
        return new NodeIdentityListener( this );
    }

    /**
     * Used in drag-n-drop
     */
    public String getNodeID() {
        return nodeID;
    }

    /**
     * Get owning library member.
     * <p>
     * For contextual facets that have been contributed to a named entity then the owner of all children will be the
     * named entity.
     * 
     * @return the owning library member or null if not owned by library member.
     */
    @Override
    public LibraryMemberInterface getOwningComponent() {
        if (this instanceof LibraryMemberInterface)
            return (LibraryMemberInterface) this;
        if (getParent() instanceof LibraryMemberInterface)
            return (LibraryMemberInterface) getParent();
        if (getParent() == null || !(getParent() instanceof ComponentNode))
            return null;
        return getParent().getOwningComponent();
    }

    /**
     * Return actual parent.
     */
    @Override
    public Node getParent() {
        return parent;
    }

    // TODO - move to model node
    @Override
    public List<ProjectNode> getProjects() {
        ArrayList<ProjectNode> libs = new ArrayList<>();
        for (Node n : getChildren()) {
            if (n instanceof ProjectNode)
                libs.add( (ProjectNode) n );
        }
        return libs;
    }

    /**
     * @return a new list of children of the parent after this node is removed
     */
    public List<Node> getSiblings() {
        if (parent == null)
            return null;
        final List<Node> siblings = new LinkedList<>( parent.getChildren() );
        siblings.remove( this );
        return siblings;
    }

    /**
     * returns If a type user then return getAssignedType() node else null
     */
    @Override
    public Node getType() {
        return (Node) ((this instanceof TypeUser) ? ((TypeUser) this).getAssignedType() : null);
    }

    @Override
    public String getAssignedTypeName() {
        if (this instanceof TypeUser)
            return ((TypeUser) this).getTypeHandler().getAssignedTypeName();
        return "";
        // return getType() != null ? getType().getName() : "";
    }

    @Override
    public String getTypeNameWithPrefix() {
        return "";
    }

    /**
     * @return a type selection filter. May be overridden to provide node specific filter.
     */
    public TypeSelectionFilter getTypeSelectionFilter() {
        return new TypeSelectionFilter();
    }

    // /**
    // * @return - list of unique TLContexts used by any child of this node. Empty list if none.
    // */
    // @Deprecated
    // public List<TLContext> getUsedContexts() {
    // final Map<String, TLContext> ctxMap = new LinkedHashMap<String, TLContext>();
    // ArrayList<TLContext> ret = new ArrayList<TLContext>();
    // List<TLContext> list = getCtxList();
    // for (TLContext tlc : list) {
    // if ((tlc != null && tlc.getApplicationContext() != null))
    // ctxMap.put(tlc.getApplicationContext(), tlc);
    // }
    // ret.addAll(ctxMap.values());
    // // LOGGER.debug("Found "+ret.size()+" contexts in "+this.getName());
    // return ret;
    // }

    /**
     * Get all user libraries (OTM TLLibrary) from the LibraryModelManager.
     * 
     * @return new list of library nodes.
     */
    @Override
    public List<LibraryNode> getUserLibraries() {
        return getModelNode().getLibraryManager().getUserLibraries();
    }

    /**
     * Simple getter of the versionNode field.
     * 
     * @return the version node representing this node in the specific library in a chain.
     */
    public VersionNode getVersionNode() {
        return versionNode;
    }

    /**
     * @return where extended handler. Will create one if null.
     */
    public WhereExtendedHandler getWhereExtendedHandler() {
        if (whereExtendedHandler == null)
            whereExtendedHandler = new WhereExtendedHandler( this );
        return whereExtendedHandler;
    }

    @Override
    public boolean hasChildren() {
        return !getChildren().isEmpty();
    }

    @Override
    public boolean hasChildren_TypeProviders() {
        return getChildrenHandler() != null ? getChildrenHandler().hasChildren_TypeProviders() : false;
    }

    public boolean hasInheritedChildren() {
        return getChildrenHandler() != null ? getChildrenHandler().hasInheritedChildren() : false;
    }

    /**
     * Used in wizards, drag-n-drop and filters
     * 
     * @return true if type provider and NOT implied node
     */
    public boolean isAssignable() {
        return this instanceof TypeProvider && !(this instanceof ImpliedNode);
    }

    @Override
    @Deprecated
    public boolean isAssignedByReference() {
        return false;
    }

    /**
     * @return true if node has a library which is builtIn() (TL is instanceof BuiltInLibrary)
     */
    public boolean isBuiltIn() {
        return getLibrary() != null && getLibrary().isBuiltIn();
    }

    /**
     * @return - true if this node is in the current default library
     */
    @Deprecated
    public boolean isDefaultLibrary() {
        if (getLibrary() == null)
            return false;
        ProjectNode pNode = getLibrary().getProject();
        return pNode != null ? pNode.getTLProject().getDefaultItem() == getLibrary().getProjectItem() : false;
    }

    /**
     * @return False for node that can not be deleted: not-editable, facets, simpleFacets. Custom and Query Facets are
     *         delete-able. Libraries are <b>always</b> delete-able
     */
    public boolean isDeleteable() {
        if (getLibrary() == null)
            return false;

        // If it doesn't have a parent then it is not linked and can be deleted.
        if (getOwningComponent() == null || getOwningComponent().getParent() == null)
            return true;

        // You can't delete anything from a patch except an extension point OR a newly added object
        if (getLibrary().getChain() != null)
            if (getOwningComponent().isInHead() && getLibrary().getChain().getHead().isPatchVersion()) {
                if (!getOwningComponent().isVersioned())
                    return true; // new to the patch
                else if ((getOwningComponent() instanceof ExtensionPointNode))
                    return true;
                else
                    return false; // nothing else can be deleted
            }

        // Services always return false for inhead(). Make sure it is in the head library.
        if (isInService() && getChain() != null)
            return getLibrary().getChain().getHead() == getLibrary() && isEditable();

        // Library members can only be deleted if they are new to the chain.
        if (getLibrary().isManaged()) {
            if (isInHead() && isEditable()) {
                if (this instanceof LibraryMemberInterface)
                    return isNewToChain();
                return true;
            } else
                return false;
        }
        return isEditable();
    }

    @Override
    public boolean isDeleted() {
        if (!(this instanceof FacadeInterface) && getTLModelObject() == null)
            deleted = true;
        return deleted;
    }

    @Override
    public boolean isDeprecated() {
        return docHandler != null && docHandler.getDeprecation( 0 ) != null ? true : false;
        // return modelObject != null && modelObject.getDeprecation() != null ? true : false;
    }

    public boolean isDocumentationOwner() {
        if (this instanceof ImpliedNode)
            return false;
        return getTLModelObject() instanceof TLDocumentationOwner && !(getTLModelObject() instanceof TLListFacet);
    }

    /**
     * Implied nodes and nodes without libraries are always editable. Nodes in chains return if chain is editable.
     * 
     * @return true if the node's library or chain is editable and it is not inherited.
     * @see Node#isInherited()
     */
    @Override
    public boolean isEditable() {
        boolean result = false;
        if (getChain() != null)
            result = getChain().isEditable();
        else if (this instanceof ImpliedNode)
            result = true;
        else if (isInherited())
            result = false;
        else if (getLibrary() == null)
            result = true;
        else
            result = getLibrary().isEditable();
        // LOGGER.debug("Is " + this + " editable? " + result);
        return result;
    }

    /**
     * @return Can the description field be edited?
     */
    public boolean isEditable_description() {
        return getTLModelObject() instanceof TLDocumentationOwner ? isInHead2() && !isInherited() && isEditable()
            : false;
    }

    public boolean isEditable_equivalent() {
        return getTLModelObject() instanceof TLEquivalentOwner ? !isInherited() && isEditable() : false;
    }

    public boolean isEditable_example() {
        return getTLModelObject() instanceof TLExampleOwner ? !isInherited() && isEditable() : false;
    }

    /**
     * Could this object be edited if a minor version was created? Use in GUI enabling actions that are allowed for
     * minor versions. Objects that pass this test may have to have a minor version created before the model is modified
     * {see isEditable_inMinor()}
     * 
     * @return
     */
    public boolean isEditable_ifMinorCreated() {
        if (getLibrary() == null || isDeleted() || !isEditable())
            return false; // not editable

        if (getChain() == null)
            return true; // editable and not in a chain

        if (getOwningComponent().getVersionNode() == null)
            return true; // editable, and not in a chain (duplicate logic?)

        if (getChain().getHead() == null)
            return false; // error
        if (getChain().getHead() == getLibrary())
            return false; // is already in head library so we can't make a minor version of the object.

        // is head library editable and a minor version?
        return getChain().getHead().isEditable() && getChain().getHead().equals( NodeEditStatus.MINOR );
    }

    /**
     * Is the owning object in a Minor version library and ready for model changes. Used in the model to assure editing
     * is allowed. The test does NOT consider if the action is allowed. Is the owning object editable and either new to
     * the chain OR in an editable minor version. Used for model change tests for object characteristics that are
     * allowed to be changed in a minor version of an object that is extends an older version.
     * 
     * Use for characteristics that may be changed in a minor version of an object
     */
    // Only used for enum literals and roles
    public boolean isEditable_inMinor() {
        if (getLibrary() == null || isDeleted() || !isEditable())
            return false; // not editable

        if (getChain() == null)
            return true; // editable and not in a chain

        if (getOwningComponent().getVersionNode() == null)
            return true; // editable, and not in a chain (duplicate logic?)

        if (getLibrary() != getChain().getHead())
            return false; // is not in the head library of the chain.

        // FIXME - should not use version node, should use library chain.
        // FIXME - should not return true...might not be a minor
        // It is in head of chain. Return true if there are no previous versions
        if (getOwningComponent().getVersionNode().getPreviousVersion() == null)
            return true;

        // It is in editable head library and is a minor with previous versions.
        return getEditStatus().equals( NodeEditStatus.MINOR );
    }

    /**
     * @return true if this node editable, not a patch and either a service, operation, message or message property.
     */
    // Done 4/28/2017 - reconcile with isInService()
    public boolean isEditable_inService() {
        if (!isEditable())
            return false;
        if (getLibrary() == null)
            return false;
        if (getLibrary().getChain() != null)
            if (getLibrary().getChain().getHead().isPatchVersion())
                return false;
        return isInService();
    }

    /**
     * Returns true if this is a service, operation, operationFacet or owned by an operaton facet.
     * 
     * JUNIT: {@link org.opentravel.schemas.node.Node_IsTests#isInServiceTest()}
     * 
     * @return
     */
    public boolean isInService() {
        if (this instanceof ServiceNode)
            return true;
        if (this instanceof OperationNode)
            return true;
        if (this instanceof OperationFacetNode)
            return true;
        if (getParent() != null)
            if (this instanceof PropertyNode && getParent() instanceof OperationFacetNode)
                return true;
        return false;
    }

    /**
     * Is this owner editable based on not being in a chain, in the editable head library, or could be used to create a
     * minor version.
     * 
     * @return true if it is editable or could be edited.
     */
    public boolean isEditable_isNewOrAsMinor() {
        if (getLibrary() == null || isDeleted() || !isEditable() || getOwningComponent() == null)
            return false; // not editable

        // Service nodes are not in a chain
        if (!(this instanceof ServiceNode) && !(this instanceof OperationNode)
            && !(this instanceof OperationFacetNode)) {
            if (getChain() == null || getOwningComponent().getVersionNode() == null)
                return true; // editable because it is not in a chain

            if (getChain().getHead() == null || !getChain().getHead().isEditable())
                return false; // not editable head library
        }
        if (getChain() == null)
            return true;

        if (getLibrary() == getChain().getHead())
            return true; // editable by being in the head library

        if (getChain().getHead().isMinorVersion() && (getOwningComponent() instanceof VersionedObjectInterface))
            return true; // could have a component created that is editable

        return false;

    }

    /**
     * Is the owning object editable and new to the chain. The object is represented by one or more nodes with the same
     * name within the chain. Non-inherited properties in an object in a head library are new and therefore editable.
     * 
     * @return True if this node is editable AND is not in a chain, OR it is in the latest library of the chain AND not
     *         in a previous version.
     * 
     */
    public boolean isEditable_newToChain() {
        if (getLibrary() == null || isDeleted() || !isEditable())
            return false; // not editable

        if (getChain() == null)
            return true; // editable and not in a chain

        if (getChain().getHead() == null)
            return true; // error condition seen on close

        // Only properties in an extension point may be edited in a patch version
        if (getChain().getHead().isPatchVersion())
            if (getOwningComponent() instanceof ExtensionPointNode)
                return !isInherited();
            else
                return false; // no editing in a patch version

        // Service components are only editable if they are in the head library. inHead() does not work.
        if (isInService())
            return getLibrary().getChain().getHead() == getLibrary();

        if (getOwningComponent() == null)
            return false; // Is not an editable component (nav-node, etc)

        // FIXME - this should be tested differently
        if (getOwningComponent().getVersionNode() == null)
            return true; // will be true for service descendants, editable, and not in a chain (duplicate logic?)

        if (getLibrary() != getChain().getHead())
            return false; // is not in the head library of the chain.

        if (this instanceof PropertyNode)
            return !isInherited(); // properties in the head library are editable

        // It is in head of chain. Return true if there are no previous versions
        return !getOwningComponent().isVersioned();
        // return getOwningComponent().getVersionNode().getPreviousVersion() == null;
    }

    /**
     * Tests if a node can be added to based edit-ability and version status. Used in global selection tester.
     * <b>Note,</b> a minor version might have to be created before properties can be added. (use isNewToChain() to
     * test).
     * 
     * • Values with Attributes (VWA) • Core Object • Business Object • Operation
     * 
     * @return true if the node can have properties added.
     */
    // Override to DISABLE adding properties
    public boolean isEnabled_AddProperties() {
        if (getLibrary() == null || parent == null || !isEditable() || isDeleted())
            return false;

        if (isEditable_inService() && getLibrary().getChain() != null
            && getLibrary().getChain().getHead() == getLibrary())
            // Only add properties to service in the head library.
            return !getLibrary().getChain().getHead().getEditStatus().equals( NodeEditStatus.PATCH );

        // Operations, business, core, vwa, open enums - allow major, minor, or unmanaged and
        if (this instanceof VersionedObjectInterface)
            return isEditable_isNewOrAsMinor();

        // Facets - not versioned but can add if owner allows
        if (this instanceof FacetInterface)
            return getOwningComponent().isEditable_isNewOrAsMinor();

        // Enumeration is a versionedObjectInterface implementer
        // delegated: ServiceNode, Enumeration, ExtensionPointNode, ContextualFacetNode, SimpleAttributeNode,
        // SimpleFacetNode, ListFacetNode, VWA_AttributeFacetNode, RoleFacetNode, PropertyNode

        return false;
    }

    /**
     * @return true if this property (or simple attribute) is enabled for setting assigned type
     */
    public boolean isEnabled_AssignType() {
        boolean enabled = false;
        if (isEditable() && this instanceof TypeUser)
            // if (isEditable() && this instanceof TypeUser && !isInheritedProperty())
            if (getChain() == null || getChain().isMajor())
                enabled = true; // Unmanaged or major - allow editing.
            else if (getChain().isPatch())
                enabled = false; // no changes in a patch
            else if (this instanceof SimpleAttributeFacadeNode)
                enabled = isNewToChain(); // only allow editing if owner is new to the minor
            else if (isInHead2() && !isInherited())
                enabled = true; // Allow unless this property also exists in prev version
            else if (getLaterVersions() != null)
                enabled = true; // If the assigned type has a newer version then allow them to select that.
        return enabled;
    }

    /**
     * Wording on this is weird -- it really does: this.getExtensionBase == base
     * 
     * @return true if this is extended by the passed base node
     */
    public boolean isExtendedBy(Node base) {
        if (this instanceof ExtensionOwner)
            return ((ExtensionOwner) this).getExtensionBase() == base;
        return false;
    }

    /**
     * Extensible objects have the ability to create extension points when compiled into schemas. These include core and
     * business objects as well as operations and extension points. {@link #isExtensibleObject()}
     * 
     * @return true if this object has the characteristic of being extensible and the model object is set to create
     *         extension points on compile.
     */
    public boolean isExtensible() {
        if (getTLModelObject() instanceof TLComplexTypeBase)
            return !((TLComplexTypeBase) getTLModelObject()).isNotExtendable();
        return false;
    }

    /**
     * Does this object have the ability to create extension points when compiled into schemas. These include choice,
     * 
     * @return true if this object has the characteristic of being extensible
     */
    public boolean isExtensibleObject() {
        return false; // FIXED 4/5/2017 - should be overridden to be true
    }

    // @Deprecated
    // public boolean isFacetAlias() {
    // // assert false; // should never be reached
    // return false; // called from navigator menus
    // }

    public boolean isFacetUnique(final INode testNode) {
        Node n = this;
        if (n instanceof PropertyNode) {
            n = n.parent;
        }
        if (n instanceof FacetOMNode && !(n instanceof QueryFacetNode) && !(n instanceof RoleFacetNode)) {
            n = n.parent; // compare across all facets.
        }
        if (n.nameEquals( testNode )) {
            return false;
        }
        for (final Node facet : n.getChildren()) {
            if (facet.nameEquals( testNode )) {
                return false;
            }
        }
        return true;
    }

    public boolean isImportable() {
        return false;
    }

    /**
     * @return true only if this object is in the version head library. false if not, false if owner is a service, or
     *         unmanaged. See also: isInHead2()
     * 
     */
    // TODO Compare results of this from the commonly used:
    // if (selectedNode.getLibrary() != selectedNode.getChain().getHead())
    public boolean isInHead() {
        LibraryMemberInterface owner = getOwningComponent();
        if (owner instanceof OperationNode)
            owner = owner.getOwningComponent();

        // service do not have versionNode
        if (owner instanceof ServiceNode)
            return true;

        // False if un-owned or unmanaged.
        if (owner == null || owner.getVersionNode() == null)
            return false;

        if (getChain() == null || getChain().getHead() == null)
            return false;

        return getChain().getHead().contains( (Node) owner );
    }

    // TODO - understand difference, especially for contextual facets in different library than where contributed
    /**
     * @return true if unmanaged (no chain) or head of the chain.
     */
    public boolean isInHead2() {
        if (getChain() == null)
            return true;
        return getLibrary() == getChain().getHead();
    }

    /**
     * @return the inherited field value or else false.
     */
    public boolean isInherited() {
        if (this instanceof InheritedInterface)
            return true;
        return getInheritedFrom() != null;
    }

    /**
     * Is <i>this</i> node an instance of the passed node? Does this tl object have an tlExtension with an extended
     * entity of node's tl object?
     * 
     * @param node
     * @return true if this is extended by the passed node
     */
    public boolean isInstanceOf(Node node) {
        if (isExtendedBy( node )) {
            if (!node.getWhereExtendedHandler().getWhereExtended().contains( this ))
                LOGGER.warn( "Base node " + node.getNameWithPrefix() + " does not have extension "
                    + this.getNameWithPrefix() + " in its where extended list. " );
            return true;
        } else {
            Node base = null;
            if (this instanceof ExtensionOwner) {
                base = ((ExtensionOwner) this).getExtensionBase();
            }
            return base == null ? false : base.isInstanceOf( node );
            // TOOD - what does this else clause do? Only seems to be used for Business Objects in NodeExtensionTest
        }
    }

    public boolean isInTLLibrary() {
        return getLibrary() != null ? getLibrary().isTLLibrary() : false;
    }

    /**
     * NOTE - this does not detect nodes that are created by the xsd utilities that represent local or anonymous types.
     * Use {@code isXsdType()} instead.
     * 
     * @return true if the model object one of the XSD model objects
     */
    @Deprecated
    public boolean isInXSDSchema() {
        return isXsdType();
    }

    /*****************************************************************************
     * is Properties
     */
    // /**
    // * @return true if this node or its descendants can contain libraries
    // */
    // @Override
    // public boolean isLibraryContainer() {
    // return false;
    // }

    // /**
    // * True if is a compiler LibraryMember and not an implied node. False for version 1.5 contextual facets.
    // */
    // public boolean isLibraryMember() {
    // if (this instanceof ImpliedNode)
    // return false;
    // if (this instanceof ContextualFacetNode)
    // return false;
    // return getTLModelObject() instanceof LibraryMember;
    // };

    public boolean isMergeSupported() {
        return false;
    }

    /**
     * Do NOT use instanceof TypeProvider because implied node is a type provider!
     */
    @Override
    public boolean isNamedEntity() {
        if (this instanceof ImpliedNode)
            return false;
        return getTLModelObject() instanceof NamedEntity;
    }

    /**
     * Fast method to determine if this node should be displayed in navigation views.
     * 
     * @return true if this node should be displayed in navigator view tree with no filters
     * @see {@link org.opentravel.schemas.node.Node_NavChildren_Tests#hasTests() }
     */
    public boolean isNavChild(boolean deep) {
        return this instanceof LibraryMemberInterface;
    }

    @Override
    public boolean isNavigation() {
        return false;
    }

    /**
     * 
     * @return true <b>only</b> if owning components is in chain and new to the chain
     */
    // TODO - this may not work for deep chains
    public boolean isNewToChain() {
        assert getOwningComponent() != null;
        return !getOwningComponent().isVersioned();
    }

    // /**
    // * <p>
    // * ONLY Used by type selection wizard to filter list.
    // * <p>
    // * Note: if parent is not known, attributes are assumed to not be part of a ValueWithAttribute and therefore are
    // * simpleTypeUsers
    // *
    // * Overridden where true.
    // *
    // * @return true if <b>only</b> simple types can be assigned to this type user.
    // */
    // public boolean isOnlySimpleTypeUser() {
    // return false;
    // }

    @Override
    public boolean isRenameable() {
        return false;
    }

    /**
     * @return true if this object can be assigned as a simple type (not by reference).
     */
    public boolean isSimpleAssignable() {
        return false;
        // Override - closed, core, Open???, simple facet, vwa, xsd simple
    }

    public boolean isTLLibrary() {
        return false;
    }

    // /**
    // * @return true if tl model object is a library member
    // */
    // public boolean isTLLibraryMember() {
    // return getTLModelObject() instanceof LibraryMember;
    // }

    /**
     * @return true if this node could be assigned a type but is unassigned.
     */
    public boolean isUnAssigned() {
        if (!(this instanceof TypeUser))
            return false;
        if (getType() instanceof ImpliedNode)
            if (((ImpliedNode) getType()).getImpliedType() == ImpliedNodeType.UnassignedType)
                return true;
        return false;
    }

    // /**
    // * Returns true if the node name and namespace is unique compared against all of <i>this</i> children. If the
    // child
    // * is in a query facet, then the test is only across that facet.
    // *
    // * @param test
    // * node to test, <i>this</i> node to check children of. If property or facet, will go to parentNode to
    // * start check.
    // * @return true if unique
    // *
    // * FIXME - this assumes that we are only comparing properties of complex objects Will not work for nav nodes
    // * FIXME - this does not handle contributed facets
    // */
    // public boolean isUnique(final INode testNode) {
    // assert false; // Should not be reached
    //
    // // Node n = this;
    // // if (this instanceof PropertyNode) {
    // // n = n.parent;
    // // }
    // // if (n instanceof FacetOMNode && !(n instanceof CustomFacetNode) && !(n instanceof QueryFacetNode)
    // // && !(n instanceof RoleFacetNode)) {
    // // n = n.parent; // compare across all facets.
    // // }
    // // for (final Node facet : n.getChildren()) {
    // // if (facet.nameEquals(testNode)) {
    // // return false;
    // // }
    // // for (final Node prop : facet.getChildren()) {
    // // if (prop.nameEquals(testNode)) {
    // return false;
    // // }
    // // }
    // // }
    // // return true;
    // }

    /**
     * Generate validation results starting with this node.
     * 
     * @return true if no errors.
     */
    public boolean isValid() {
        return ValidationManager.isValid( this );
    }

    public boolean isValid_NoWarnings() {
        return ValidationManager.isValidNoWarnings( this );
    }

    /**
     * Can this object contain properties of the specified type? Only FacetNodes can be containers.
     *
     * @param type
     * @return
     */
    @Deprecated
    public boolean canOwn(PropertyNodeType type) {
        return false;
    }

    /**
     * Can this object contain properties of the specified type? Only FacetNodes can be containers.
     *
     * @param type
     * @return
     */
    // Find out if FacetNode really needs to override this
    // It is NEVER called!
    @Deprecated
    public boolean canOwn(PropertyNode property) {
        return false;
    }

    /**
     * @return true if this object is a later version of another object. True if has same base ns and name as the object
     *         it extends.
     */
    public boolean isVersioned() {
        // return (this instanceof ExtensionOwner) ? getExtendsTypeName().equals(getName()) : false;
        ExtensionHandler handler = null;
        if (this instanceof ExtensionOwner)
            handler = ((ExtensionOwner) this).getExtensionHandler();
        return handler != null ? handler.isVersioned() : false;
    }

    public boolean isVWASimpleAssignable() {
        return getTLModelObject() instanceof TLAttributeType;
    }

    public boolean isXSDSchema() {
        return false;
    }

    /**
     * @return true if the node represents a non-imported XSD type. Note - the library is a TLLibrary, but the type is
     *         not editable.
     */
    public boolean isXsdType() {
        return xsdType;
    }

    /**
     * Merge source properties into <i>this</i> node. Does not change source node.
     * 
     * @param target
     */
    public void merge(Node source) {}

    /**
     * Change all context users to use targetId. Iterates on all children. If the context would be duplicated, it is
     * added as an implementors documentation item.
     * <p>
     * This node must be in a library. EQ/EX handler depends on being in a library.
     * <p>
     * 
     * @param targetId - replace with this contextId
     */
    public void mergeContext(String targetId) {
        if (getLibrary() == null)
            return;

        if (this instanceof PropertyNode && ((PropertyNode) this).getEquivalentHandler() != null)
            ((PropertyNode) this).getEquivalentHandler().fix( targetId );
        if (this instanceof PropertyNode && ((PropertyNode) this).getExampleHandler() != null)
            ((PropertyNode) this).getExampleHandler().fix( targetId );

        if (getDocHandler() != null)
            getDocHandler().fix( targetId );

        // Iterate through all children
        for (Node n : getChildren())
            n.mergeContext( targetId );
    }

    /**
     * Replace all type assignments (base and assigned type) to this node with assignments to passed node. For every
     * assignable descendant of sourceNode, find where the corresponding sourceNode children are used and change them as
     * well. See {@link #replaceWith(Node)}.
     * 
     * @param this - replace assignments to this node (sourceNode)
     * @param replacement - use replacement node instead of this node
     * @param scope (optional) - scope of the search or null for all libraries
     */
    public void replaceTypesWith(TypeProvider replacement, LibraryNode scope) {
        // if (!(replacement instanceof TypeProvider))
        // return;
        // Override
    }

    /**
     * Replace this node with the replacement. The replacement is added to this node's library. All type assignments to
     * this node are replaced with the replacement {@link #replaceWith(Node)}. This node is removed from its library.
     * <p>
     * Does <b>not</b> delete this node. <br>
     * <i>Does</i> remove parent and library links. <br>
     * <i>Does</i> remove type usage links to this node.
     * 
     * @param newNode node to replace this node with
     */
    public void replaceWith(LibraryMemberInterface newNode) {
        if (newNode == null)
            return;

        if (getLibrary() == null) {
            LOGGER.error( "The node being replaced is not in a library. " + this );
            return;
        }

        // Add newNode if it is not already a member, otherwise it does nothing
        getLibrary().addMember( newNode );

        // Replace types on users and extensions in all editable libraries
        if (newNode instanceof TypeProvider)
            replaceTypesWith( (TypeProvider) newNode, null );

        // must be done after replaceTypesWith. Listeners will remove the whereUsed links.
        if (this instanceof LibraryMemberInterface)
            getLibrary().removeMember( (LibraryMemberInterface) this );
        // NOTE - users in non-editable libraries will end up with null assignment but valid ns:name in tl object
    }

    public void setDescription(final String string) {
        if (docHandler != null)
            docHandler.setDescription( string );
    }

    /**
     * Simple setter of the deleted field
     */
    public void setDeleted(boolean value) {
        deleted = value;
    }

    /**
     * Enable the extend-able property for this object. For faceted objects, this instructs compiler to create extension
     * points. For enumerations, this creates an open enumeration.
     * 
     * @return - returns this node, or the created open/closed enumeration node {@link #isExtensible()}
     */
    public Node setExtensible(boolean extensible) {
        return this;
    }

    /**
     * Deprecated - only set on LibraryMembers Set the library field in this node and all of its children. Does
     * <b>NOT</b> change the underlying TL library.
     * 
     * @param ln
     */
    @Deprecated
    public void setLibrary(final LibraryNode ln) {
        // Overriden in LibraryMemberBase
    }

    /**
     * Set the name in the node and underlying TL model object for <i>this</i> node. Complex types propagate name change
     * type users.
     * 
     * NOTE - not safe to use in constructors until a TL model object is instantiated.
     * 
     * @param name - new name
     */
    @Override
    public void setName(final String name) {
        // Do Nothing
    }

    /**
     * Simple parent setter. Set to null if it is the root node.
     */
    public void setParent(final Node n) {
        parent = n;
    }

    /**
     * Simple setter of the versionNode field.
     * 
     * @param versionNode to represent this node in a specific library in a chain.
     */
    public void setVersionNode(VersionNode version) {
        this.versionNode = version;
    }

    @Override
    public String toString() {
        if (deleted)
            return getName() + " (Deleted)";
        return getName();
    }

    /**
     * Use the compiler to validate a node.
     */
    public ValidationFindings validate() {
        return ValidationManager.validate( this );
    }

    /**
     * Visit all descendants that are instances of ExtensionOwner
     * 
     * @param visitor to run on each ExtensionOwner
     */
    public void visitAllExtensionOwners(NodeVisitor visitor) {
        for (Node c : getChildren()) {
            c.visitAllExtensionOwners( visitor );
        }
        if (this instanceof ExtensionOwner)
            visitor.visit( this );
    }

    // Depth First node traversal
    @Override
    public void visitAllNodes(NodeVisitor visitor) {
        ArrayList<Node> kids = new ArrayList<>( getChildren() );
        for (Node child : kids)
            child.visitAllNodes( visitor );
        visitor.visit( this );
    }

    @Override
    public void visitAllTypeUsers(NodeVisitor visitor) {
        for (Node c : getChildren()) {
            c.visitAllTypeUsers( visitor );
        }
        if (this instanceof TypeUser) {
            visitor.visit( this );
        }
    }

    @Override
    public void visitChildren(NodeVisitor visitor) {
        for (Node c : getChildren())
            visitor.visit( c );
        visitor.visit( this );
    }

    /**
     * Get the version from the TLLibrary
     * 
     * @return string of the version
     */
    protected String getTlVersion() {
        String version = "";
        if (getTLModelObject() instanceof LibraryElement) {
            LibraryElement le = (NamedEntity) getTLModelObject();
            if (le != null && le.getOwningLibrary() != null)
                version = le.getOwningLibrary().getVersion();
        }
        return version;
    }

    /**
     * @return true if name and namespace are equal to other node
     */
    protected boolean nameEquals(final INode other) {
        if (this == other)
            return true;

        if (other == null)
            return false;

        if (getName() == null) {
            if (other.getName() != null) {
                return false;
            }
        } else if (!getName().equals( other.getName() )) {
            return false;
        }
        if (getNamespace() == null) {
            if (other.getNamespace() != null) {
                return false;
            }
        } else if (!getNamespace().equals( other.getNamespace() )) {
            return false;
        }
        return true;
    }

    public void deleteTL() {
        // Override where useful.
    }

    /** ************** Children Handler Facades *******************************/
    /**
     * Get node children from children handler or empty list.
     */
    @Override
    public List<Node> getChildren() {
        if (getChildrenHandler() != null)
            return genericToNode( getChildrenHandler().get() );
        return Collections.emptyList();
    }

    /**
     * Simple getter for parameterized children handler
     */
    public ChildrenHandlerI<?> getChildrenHandler() {
        return childrenHandler;
    }

    /**
     * @return a new ArrayList containing the children
     */
    public List<Node> getChildren_New() {
        if (getChildrenHandler() != null)
            return genericToNode( getChildrenHandler().getChildren_New() );
        return new ArrayList<>();
    }

    /**
     * Get all immediate navChildren that are to be presented in the OTM Object Tree. Includes where used nodes.
     * Overridden on nodes that add nodes such as where used to the tree view.
     *
     * @see {@link #getNavChildren()}
     *
     * @param deep - include properties
     *
     * @return new list
     */
    public List<Node> getTreeChildren(boolean deep) {
        if (getChildrenHandler() != null)
            return genericToNode( getChildrenHandler().getTreeChildren( deep ) );
        return getNavChildren( deep );
    }

    @Override
    public List<TypeProviderAndOwners> getChildren_TypeProviders() {
        if (getChildrenHandler() != null)
            return getChildrenHandler().getChildren_TypeProviders();
        return Collections.emptyList();
    }

    /**
     * Gets the children that are type users (can be assigned a type). Does not return navigation nodes.
     * {@link #getDescendants_TypeUsers() Use getDescendants_TypeUsers() for all children.}
     * 
     * @return all immediate children that can be assigned a type.
     */
    public List<Node> getChildren_TypeUsers() {
        if (getChildrenHandler() != null)
            return genericToNode( getChildrenHandler().getChildren_TypeUsers() );
        return Collections.emptyList();
    }

    // Work around until all children getters are safe
    @Deprecated
    private List<Node> genericToNode(List<?> gList) {
        List<Node> list = new ArrayList<>();
        for (Object g : gList)
            if (g instanceof Node)
                list.add( (Node) g );
        return list;
    }

    /**
     * Get a new list of child nodes that are to be displayed in navigator trees.
     * 
     * @param deep when true some nodes will return more children such as properties
     * 
     * @see {@link #isNavChild()}
     * @see {@link org.opentravel.schemas.node.Node_NavChildren_Tests#getNavChildrenTests()}
     * 
     * @return new list of children to be used for navigation purposes.
     */
    public List<Node> getNavChildren(boolean deep) {
        if (getChildrenHandler() != null)
            return genericToNode( getChildrenHandler().getNavChildren( deep ) );
        return Collections.emptyList();
    }

    /**
     * Fast (no array creation) method to determine if there are navChildren that should be displayed in navigator
     * trees.
     * 
     * @param deep enable the "deep" property mode
     */
    public boolean hasNavChildren(boolean deep) {
        if (getChildrenHandler() != null)
            return getChildrenHandler().hasNavChildren( deep );

        for (final Node n : getChildren())
            if (n.isNavChild( deep ))
                return true;
        return false;
    }

    // Override on classes that add to getNavChildren()
    public boolean hasTreeChildren(boolean deep) {
        if (getChildrenHandler() != null)
            return getChildrenHandler().hasTreeChildren( deep );

        return hasNavChildren( deep );
    }

    /**
     * setter to the tlObj field.
     * <p>
     * Setting to null in effect deletes this node. Does NOT actively delete anything, just sets deleted flag and clears
     * node identity listener for this node.
     * 
     * @param object or null to signal the node is deleted
     */
    public void setTlModelObject(TLModelElement object) {
        if (object == null) {
            if (getTLModelObject() != null) {
                // Remove this named type listener so this node is not associated with the tl object.
                Collection<ModelElementListener> listeners = new ArrayList<>( getTLModelObject().getListeners() );
                for (ModelElementListener l : listeners)
                    if (l instanceof NodeIdentityListener)
                        if (((NodeIdentityListener) l).getNode() == this) {
                            getTLModelObject().removeListener( l );
                            LOGGER.debug( "Removing listner from: " + this );
                        }
            }
            deleted = true;
        }
        tlObj = object;
    }

}
