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
package org.opentravel.schemas.controllers;

/**
 * Coordinates the actions related to documentation items between model and view. Needs to be
 * initialized with the proper view.
 * 
 * @author Agnieszka Janowska
 * 
 */
public interface DocumentationController {

    /**
     * Adds documentation item to the currently selected documentation type list
     */
    void addDocItem();

    /**
     * Removes documentation item from the currently selected documentation type list
     */
    void deleteDocItems();

    /**
     * Moves up documentation item on the currently selected documentation type list. Guards from
     * moving the documentation item out of the current type list.
     */
    void upDocItem();

    /**
     * Moves down documentation item on the currently selected documentation type list. Guards from
     * moving the documentation item out of the current type list.
     */
    void downDocItem();

    /**
     * Clears (but not removes) the value of the selected documentation item
     */
    void clearDocItem();

    /**
     * Changes the type root of selected documentation item to new one selected in the view
     */
    void changeDocItemsType();

    /**
     * Clones the selected documentation items
     */
    void cloneDocItems();
}
