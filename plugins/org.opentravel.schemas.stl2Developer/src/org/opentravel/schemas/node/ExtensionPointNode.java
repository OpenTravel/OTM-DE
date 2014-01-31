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

import org.eclipse.swt.graphics.Image;
import org.opentravel.schemas.node.properties.ElementNode;
import org.opentravel.schemas.properties.Images;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLProperty;

/**
 * Extension points are used to add properties to facets in business or core objects. They have a
 * facet structure
 * 
 * @author Dave Hollander
 * 
 */
public class ExtensionPointNode extends ComponentNode implements ComplexComponentInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionPointNode.class);

    public ExtensionPointNode(LibraryMember mbr) {
        super(mbr);
        addMOChildren();

        // Tests
        // if (!(mbr instanceof TLExtensionPointFacet)) {
        // LOGGER.error("Tried to make an extension point node from wrong class "
        // + mbr.getClass().getName());
        // throw new IllegalArgumentException(
        // "Tried to make an extension point node from wrong class "
        // + mbr.getClass().getName());
        // }
        // if (((TLExtensionPointFacet) mbr).getExtension() == null) {
        // LOGGER.debug("NULL Extension. " + mbr.getLocalName());
        // } else if (((TLExtensionPointFacet) mbr).getExtension().getExtendsEntityName() == null) {
        // LOGGER.debug("NULL extends entity name." + mbr.getLocalName());
        // } else if (((TLExtensionPointFacet) mbr).getExtension().getExtendsEntityName().isEmpty())
        // LOGGER.debug("Empty extends entity name." + mbr.getLocalName());
    }

    @Override
    public INode createProperty(final Node type) {
        Node n = new ElementNode(this, type.getName());
        n.setDescription(type.getDescription());
        // linkChild(n, nodeIndexOf());
        n.setAssignedType(type);
        return n;
    }

    @Override
    public ComponentNode getAttributeFacet() {
        return null;
    }

    @Override
    public Image getImage() {
        return Images.getImageRegistry().get(Images.Facet);
    }

    @Override
    public String getLabel() {
        return getName();
    }

    @Override
    public String getName() {
        String name = "unnamed";
        // Could be TLEmpty
        if ((getTLModelObject() != null) && (getTLModelObject() instanceof TLExtensionPointFacet))
            name = ((TLExtensionPointFacet) getTLModelObject()).getLocalName();
        if (name == null)
            name = "unnamed";
        String prefix = "ExtensionPoint_";
        if (name.startsWith(prefix))
            name = name.substring(prefix.length(), name.length());
        return name + "_ExtensionPoint";
    }

    @Override
    public boolean setSimpleType(Node type) {
        return false;
    }

    @Override
    public boolean isExtensionPointFacet() {
        return true;
    }

    protected Node newElementProperty() {
        ElementNode n = new ElementNode(new TLProperty(), this);
        return n;
    }

    @Override
    public ComponentNode getSimpleType() {
        return null;
    }

    @Override
    public SimpleFacetNode getSimpleFacet() {
        return (SimpleFacetNode) super.getSimpleFacet();
    }

    @Override
    public boolean isNamedType() {
        return true;
    }

}
