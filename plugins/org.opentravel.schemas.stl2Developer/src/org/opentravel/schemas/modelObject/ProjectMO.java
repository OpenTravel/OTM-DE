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
package org.opentravel.schemas.modelObject;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opentravel.schemacompiler.model.AbstractLibrary;

/**
 * OBSOLETE - to be deleted
 * 
 * @author Dave Hollander
 * 
 */
public class ProjectMO extends ModelObject<TLProject> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectMO.class);

    public ProjectMO() {
        srcObj = new TLProject();
    }

    public ProjectMO(TLProject obj) {
        super(obj);
        srcObj = obj;
    }

    public boolean isDeprecated() {
        return getTLModelObj().isDeprecated();
    }

    public boolean isEditable() {
        return !getName().contains("Built");
        // return getTLModelObj().isEditable();
    }

    @Override
    public List<?> getChildren() {
        // TODO Auto-generated method stub
        return getTLModelObj().getProjectItems();
    }

    @Override
    public String getComponentType() {
        // TODO Auto-generated method stub
        return "Project: " + getName();
    }

    @Override
    protected AbstractLibrary getLibrary(TLProject obj) {
        return null;
    }

    @Override
    public String getName() {
        return getTLModelObj().getName();
    }

    @Override
    public String getNamePrefix() {
        return "";
    }

    @Override
    public String getNamespace() {
        return getTLModelObj().getNamespace();
    }

    @Override
    public boolean setName(String name) {
        getTLModelObj().setName(name);
        return true;
    }

    @Override
    public void delete() {
        LOGGER.debug("Not Implemented - library delete");
    }

    public void createItem(String string) {
        // getTLModelObj().add(item);
    }

}
