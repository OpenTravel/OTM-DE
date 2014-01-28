package org.opentravel.schemas.controllers.repository;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.trees.repository.RepositoryNode;
import org.opentravel.schemas.utils.LibraryNodeBuilder;
import org.osgi.framework.Version;

import com.sabre.schemacompiler.repository.RepositoryException;
import com.sabre.schemacompiler.repository.impl.RemoteRepositoryClient;
import com.sabre.schemacompiler.saver.LibrarySaveException;

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
