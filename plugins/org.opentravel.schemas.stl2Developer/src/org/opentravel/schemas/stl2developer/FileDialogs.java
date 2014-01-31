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
package org.opentravel.schemas.stl2developer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;

/**
 * @author Agnieszka Janowska
 * 
 */
public class FileDialogs {

    private static final String[] EXTENSIONS = { "*.otm; *.xml; *.xsd" };
    private static final String DialogText = "Select Library";

    /**
     * Use the systems native file dialog to allow the user to select one file.
     * 
     * @return string with file name
     */
    public static String postFileDialog() {
        return postFileDialog(EXTENSIONS, DialogText);
    }

    public static String postFileDialog(String[] extensions, String text) {
        final FileDialog fd = new FileDialog(OtmRegistry.getActiveShell(), SWT.OPEN);
        fd.setText(text);
        fd.setFilterExtensions(extensions);
        final String fileName = fd.open();
        return fileName;
    }

    /**
     * Open multiple file dialog.
     * 
     * @return
     */
    public static FileDialog postFilesDialog() {
        final FileDialog fd = new FileDialog(OtmRegistry.getActiveShell(), SWT.OPEN | SWT.MULTI);
        fd.setText("Select Library Modules");
        fd.setFilterExtensions(EXTENSIONS);
        fd.open();
        return fd;
    }

    /**
     * @return
     */
    public static String postDirDialog(String text) {
        final DirectoryDialog dd = new DirectoryDialog(OtmRegistry.getActiveShell());
        dd.setText(text);
        return dd.open();
    }

    /**
     * @return
     */
    public static String postFileSaveDialog() {
        final FileDialog fd = new FileDialog(OtmRegistry.getActiveShell(), SWT.SAVE);
        fd.setText("Select Directory to save to");
        fd.setFilterExtensions(EXTENSIONS);
        final String fileName = fd.open();
        return fileName;
    }

}
