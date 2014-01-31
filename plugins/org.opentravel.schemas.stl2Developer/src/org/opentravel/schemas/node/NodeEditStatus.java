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

import org.opentravel.schemacompiler.repository.RepositoryItemState;

/**
 * Editing states are based on namespace, repository and versioning rules.
 * 
 * @author Dave Hollander
 * 
 */
public enum NodeEditStatus {

    /**
     * Not editable. The owning library does not belong to the governing namespace.
     */
    NOT_EDITABLE("editStatus.notEditable"),

    /**
     * Read-Only. The library is managed in a repository and its state does not allow editing. See
     * {@link RepositoryItemState}
     */
    MANAGED_READONLY("editStatus.managedReadOnly"),

    /**
     * Patch level only. Only new simple types and extension point facets are allowed.
     */
    // TODO - should allow documentation, examples and equivalents to be changed.
    PATCH("editStatus.patch"),

    /**
     * Minor. All component types are allowed. Services are only allowed if they do not exist in the
     * chain. Existing Core and Business objects may only be "appended" to. Append means new
     * properties are at the end of a facet and must be optional.
     */
    MINOR("editStatus.minor"),

    /**
     * Full. Unmanaged or Major versions allow editing with no restrictions.
     */
    FULL("editStatus.full");

    private String msgID; // message.properties key

    NodeEditStatus(String msgID) {
        this.msgID = msgID;
    }

    public String msgID() {
        return msgID;
    }

}
