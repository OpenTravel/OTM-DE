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
package org.opentravel.schemas.testers;

import org.eclipse.core.expressions.PropertyTester;
import org.opentravel.schemas.trees.repository.RepositoryNode;
import org.opentravel.schemas.trees.repository.RepositoryNode.RepositoryInstanceNode;

import org.opentravel.schemacompiler.repository.RemoteRepository;

public class RepositoryTypeTester extends PropertyTester {

    public static final String TYPE_REMOTE = "remote";
    public static final String TYPE_LOCAL = "local";

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        if (!(receiver instanceof RepositoryNode)) {
            return false;
        }
        RepositoryInstanceNode node = (RepositoryInstanceNode) receiver;
        if (TYPE_REMOTE.equals(property)) {
            return isRemoteRepository(node);
        } else if (TYPE_LOCAL.equals(property)) {
            return !isRemoteRepository(node);
        }
        return false;
    }

    private boolean isRemoteRepository(RepositoryNode selected) {
        return selected.getRepository() instanceof RemoteRepository;
    }
}
