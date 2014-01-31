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
