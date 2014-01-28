package org.opentravel.schemas.modelObject;

import java.io.File;
import java.util.List;

import com.sabre.schemacompiler.model.TLModel;
import com.sabre.schemacompiler.model.TLModelElement;
import com.sabre.schemacompiler.repository.ProjectItem;
import com.sabre.schemacompiler.repository.Repository;
import com.sabre.schemacompiler.repository.RepositoryException;
import com.sabre.schemacompiler.repository.RepositoryItem;
import com.sabre.schemacompiler.repository.RepositoryManager;

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
