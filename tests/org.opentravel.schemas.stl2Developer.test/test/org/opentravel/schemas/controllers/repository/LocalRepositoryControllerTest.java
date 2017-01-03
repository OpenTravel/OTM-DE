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
package org.opentravel.schemas.controllers.repository;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.opentravel.schemas.node.libraries.LibraryNode;
import org.opentravel.schemas.trees.repository.RepositoryNode;
import org.opentravel.schemas.utils.LibraryNodeBuilder;
import org.osgi.framework.Version;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.impl.RemoteRepositoryClient;
import org.opentravel.schemacompiler.saver.LibrarySaveException;

public class LocalRepositoryControllerTest extends RepositoryControllerTest {

    @Override
    public RepositoryNode getRepositoryForTest() {
        return rc.getLocalRepository();
    }

    @Test
    public void saveLocalRepositoryMetadataDoesntCleanupChangeSet() throws RepositoryException,
            LibrarySaveException {
        // getRepositoryForTest is forcing refresh. We want to avoid this
        RepositoryNode localRepository = getRepositoryForTest();

        // change credential to anonymous
        repositoryManager.setCredentials(remoteRepository, null, "");
        RemoteRepositoryClient client = (RemoteRepositoryClient) remoteRepository;
        Assert.assertNull(client.getUserId());

        // Manage library in local repository to force "fileManager.startChangeSet();"
        LibraryNode testLibary = LibraryNodeBuilder.create("name",
                localRepository.getNamespace() + "/Test", "prefix", new Version(1, 0, 0)).build(
                defaultProject, pc);
        rc.manage(localRepository, Collections.singletonList(testLibary));

        // need to refresh local repository info and refreshLocalRepositoryInfo() is private
        repositoryManager.getLocalRepositoryDisplayName();

        // make sure user id didn't change
        Assert.assertNull(client.getUserId());
    }

}
