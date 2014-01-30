
package org.opentravel.schemas.utils;

import java.io.File;
import java.net.URL;

import org.opentravel.schemas.controllers.ProjectController;
import org.opentravel.schemas.node.LibraryNode;
import org.opentravel.schemas.node.ProjectNode;
import org.osgi.framework.Version;

import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.saver.LibraryModelSaver;
import org.opentravel.schemacompiler.saver.LibrarySaveException;
import org.opentravel.schemacompiler.util.URLUtils;

public class LibraryNodeBuilder {

    public static LibraryNodeBuilder create(String name, String namespace, String prefix,
            Version version) {
        return new LibraryNodeBuilder(name, namespace, prefix, version);
    }

    private final TLLibrary tlLib;

    private LibraryNodeBuilder(String name, String namespace, String prefix, Version version) {
        tlLib = new TLLibrary();
        tlLib.setName(name);
        tlLib.setNamespaceAndVersion(namespace, version.toString());
        tlLib.setPrefix(prefix);
    }

    public LibraryNode build(ProjectNode testProject, ProjectController pc)
            throws LibrarySaveException {
        URL libURL = URLUtils.toURL(new File(testProject.getProject().getProjectFile()
                .getParentFile(), tlLib.getName() + ".otm"));
        tlLib.setLibraryUrl(libURL);
        new LibraryModelSaver().saveLibrary(tlLib);
        return pc.add(testProject, tlLib);
    }

    public LibraryNodeBuilder makeFinal() {
        tlLib.setStatus(TLLibraryStatus.FINAL);
        return this;
    }

}
