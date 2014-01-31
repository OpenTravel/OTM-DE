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
/**
 * 
 */
package org.opentravel.schemas.node;

/**
 * The version aggregate node collects libraries that are in a chain. The library chain displays it
 * children which are Aggregate Node and a Version Aggregate Node.
 * 
 * Children this node are only allowed to be libraries.
 * 
 * @author Dave Hollander
 * 
 */
public class VersionAggregateNode extends AggregateNode {

    public VersionAggregateNode(AggregateType type, Node parent) {
        super(type, parent);
    }

    public void add(LibraryNode ln) {
        getChildren().add(ln);
        ln.getParent().getChildren().remove(ln);
        ln.setParent(this);
    }

    public void add(Node n) {
        throw (new IllegalStateException("Version aggregates can not contain "
                + n.getClass().getSimpleName()));
    }
}
