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
package org.opentravel.schemas.modelObject;

import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLProperty;

/**
 * An empty TLModelElement to allow constructors to work on entities that do not exist in the TL
 * Model
 */
public class TLEmpty extends TLProperty {

    @Override
    public String getValidationIdentity() {
        return "";
    }

    @Override
    public TLModel getOwningModel() {
        return null;
    }

    public void delete() {
    }

}
