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
package org.opentravel.schemas.types;

import java.util.ConcurrentModificationException;

import org.opentravel.schemas.node.Node;
import org.opentravel.schemas.node.NodeNameUtils;

/**
 * @author Pawel Jedruch
 * 
 */
public class PostTypeChange {

    /**
     * Because changing name from {@link Type#set(Node)} in case of setting the family
     * will cause {@link ConcurrentModificationException} (in context of TypeResolver.resolveTypes
     * visitor), this method is in purpose of catching the new type assignments. From GUI this can
     * be achieved from different actions (e.g. DND, or assign button).
     * 
     * <p>
     * Later it should be removed and replaced by future model notifications or other future that
     * will eliminate {@link ConcurrentModificationException}.
     * </p>
     * 
     * @param property
     *            - property to whom assigned new type
     * @param newType
     *            - newType assigned
     */
    public static void notyfications(Node property, Node newType) {
        NodeNameUtils.fixName(property);

    }

}
