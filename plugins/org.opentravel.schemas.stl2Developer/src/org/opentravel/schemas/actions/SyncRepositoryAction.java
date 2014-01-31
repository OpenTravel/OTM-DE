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
package org.opentravel.schemas.actions;

import org.opentravel.schemas.commands.RepositoryHandler;
import org.opentravel.schemas.properties.ExternalizedStringProperties;
import org.opentravel.schemas.properties.StringProperties;

public class SyncRepositoryAction extends OtmAbstractAction {
    private static final StringProperties propsDefault = new ExternalizedStringProperties(
            "action.repository.sync");

    public SyncRepositoryAction(StringProperties props) {
        super(props);
    }

    public SyncRepositoryAction() {
        super(propsDefault);
    }

    @Override
    public void run() {
        new RepositoryHandler().sync();
    }
}
