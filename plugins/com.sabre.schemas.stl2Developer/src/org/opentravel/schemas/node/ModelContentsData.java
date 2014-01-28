package org.opentravel.schemas.node;

import java.util.List;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.opentravel.schemas.trees.repository.RepositoryNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelContentsData implements IPersistableElement {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelContentsData.class);

    private List<LibraryNode> libraries;
    private List<ProjectNode> projects;
    private List<RepositoryNode> repositories;

    public ModelContentsData() {
    }

    public List<LibraryNode> getLibraries() {
        return libraries;
    }

    public void setLibraries(List<LibraryNode> libraries) {
        this.libraries = libraries;
    }

    public List<ProjectNode> getProjects() {
        return projects;
    }

    public void setProjects(List<ProjectNode> projects) {
        this.projects = projects;
    }

    public List<RepositoryNode> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<RepositoryNode> repositories) {
        this.repositories = repositories;
    }

    @Override
    public void saveState(IMemento memento) {
        memento = memento.createChild("Projects");
        memento.createChild("descriptor", "THIS IS A TEST");
        memento.createChild("descriptor2", "THIS IS A TEST 2");
        memento = memento.createChild("Libraries");
        LOGGER.debug("Created memento project");
    }

    @Override
    public String getFactoryId() {
        return ModelContentsFactory.ModelContentsFactory_ID;
    }

}