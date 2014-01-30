
package org.opentravel.schemas.modelObject;

import java.io.File;
import java.util.List;

import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.Repository;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryManager;

/**
 * STUB - waiting for repo to create one.
 * 
 * @author Dave Hollander
 * 
 */
public class TLProject extends TLModelElement {

    private String name = "ProjectX";

    public String getName() {
        return name;
    }

    public String getNamespace() {
        return "http://example.com/ProjectX";
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDeprecated() {
        return false;
    }

    public boolean isEditable() {
        return true;
    }

    public Repository getRepository() {
        return null;
    }

    public RepositoryManager getRepositoryManager() {
        // TODO Auto-generated method stub
        return null;
    }

    public TLModel getModel() {
        // TODO Auto-generated method stub
        return null;
    }

    public List<ProjectItem> getProjectItems() {
        // TODO Auto-generated method stub
        return null;
    }

    public ProjectItem newWorkspaceItem(File libraryFile) {
        // TODO Auto-generated method stub
        return null;
    }

    public void add(RepositoryItem item) throws RepositoryException {
        // TODO Auto-generated method stub

    }

    public void remove(ProjectItem item) throws RepositoryException {
        // TODO Auto-generated method stub

    }

    public void lock(ProjectItem item) throws RepositoryException {
        // TODO Auto-generated method stub

    }

    public void unlock(ProjectItem item) throws RepositoryException {
        // TODO Auto-generated method stub

    }

    public void commit(ProjectItem item) throws RepositoryException {
        // TODO Auto-generated method stub

    }

    public void revert(ProjectItem item) throws RepositoryException {
        // TODO Auto-generated method stub

    }

    public void publish(ProjectItem item, Repository repository) throws RepositoryException {
        // TODO Auto-generated method stub

    }

    public TLModel getOwningModel() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getValidationIdentity() {
        return name;
    }

}
