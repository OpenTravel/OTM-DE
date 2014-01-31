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