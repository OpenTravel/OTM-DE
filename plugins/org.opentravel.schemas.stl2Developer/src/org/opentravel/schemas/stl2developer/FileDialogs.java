/*
 * Copyright (c) 2011, Sabre Inc.
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
