
package org.opentravel.schemas.utils;

import org.opentravel.schemas.node.FacetNode;
import org.opentravel.schemas.node.NodeFactory;

import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLProperty;

public class FacetNodeBuilder {
    interface TLCreator {
        Object create(String name);
    }

    private FacetNode facet = new FacetNode(new TLFacet());

    public static FacetNodeBuilder create() {
        return new FacetNodeBuilder();
    }

    private FacetNodeBuilder addObjects(TLCreator tlCreator, String... names) {
        for (String n : names) {
            Object obj = tlCreator.create(n);
            NodeFactory.newComponentMember(facet, obj);
        }
        return this;
    }

    public FacetNodeBuilder addElements(String... names) {
        return addObjects(new TLCreator() {

            @Override
            public Object create(String name) {
                TLProperty prop = new TLProperty();
                prop.setName(name);
                return prop;
            }
        }, names);
    }

    public FacetNodeBuilder addAttributes(String... names) {
        return addObjects(new TLCreator() {

            @Override
            public Object create(String name) {
                TLAttribute prop = new TLAttribute();
                prop.setName(name);
                return prop;
            }
        }, names);
    }

    public FacetNodeBuilder addIndicators(String... names) {
        return addObjects(new TLCreator() {

            @Override
            public Object create(String name) {
                TLIndicator prop = new TLIndicator();
                prop.setName(name);
                return prop;
            }
        }, names);
    }

    public FacetNodeBuilder addAliases(String... names) {
        return addObjects(new TLCreator() {

            @Override
            public Object create(String name) {
                TLAlias prop = new TLAlias();
                prop.setName(name);
                return prop;
            }
        }, names);
    }

    public FacetNode build() {
        return facet;
    }

}