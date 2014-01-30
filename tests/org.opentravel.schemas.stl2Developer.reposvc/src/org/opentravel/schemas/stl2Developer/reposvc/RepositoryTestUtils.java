/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemas.stl2Developer.reposvc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;

/**
 * Static utility methods shared by numerous tests.
 * 
 * @author S. Livezey
 */
public class RepositoryTestUtils {

    /**
     * Displays the validation findings if debugging is enabled.
     * 
     * @param findings
     *            the validation findings to display
     */
    public static void printFindings(ValidationFindings findings) {
        printFindings(findings, null);
    }

    /**
     * Displays the validation findings if one or more findings of the specified type are present
     * (and debugging is enabled).
     * 
     * @param findings
     *            the validation findings to display
     * @param findingType
     *            the finding type to search for
     */
    public static void printFindings(ValidationFindings findings, FindingType findingType) {
        boolean hasFindings = ((findingType == null) && findings.hasFinding())
                || ((findingType != null) && findings.hasFinding(findingType));

        if (hasFindings) {
            System.out.println("Validation Findings:");

            for (String message : findings.getAllValidationMessages(FindingMessageFormat.DEFAULT)) {
                System.out.println("  " + message);
            }
        }
    }

    /**
     * Recursively deletes the contents of the specified folder and removes the folder itself.
     * 
     * @param fileOrFolder
     *            the file or folder location to delete
     */
    public static void deleteContents(File fileOrFolder) {
        if (fileOrFolder.isDirectory()) {
            for (File folderMember : fileOrFolder.listFiles()) {
                deleteContents(folderMember);
            }
        }
        fileOrFolder.delete();
    }

    /**
     * Recursively copies the contents of the source folder to the specified destination.
     * 
     * @param src
     *            the source folder location
     * @param dest
     *            the destination folder location
     * @throws IOException
     *             thrown if one or more files cannot be copied
     */
    public static void copyContents(File src, File dest) throws IOException {
        if (!src.exists()) {
            throw new IOException("Source file not found: " + src.getAbsolutePath() + ".");

        } else if (!src.canRead()) {
            throw new IOException("Source file cannot be read: " + src.getAbsolutePath() + ".");
        }

        if (src.isDirectory()) {
            if (!dest.exists()) {
                if (!dest.mkdirs()) {
                    throw new IOException("Unable to create direcotry: " + dest.getAbsolutePath()
                            + ".");
                }
            }
            String list[] = src.list();

            for (int i = 0; i < list.length; i++) {
                File dest1 = new File(dest, list[i]);
                File src1 = new File(src, list[i]);

                if (src1.getName().equals(".svn")) {
                    continue;
                }
                copyContents(src1, dest1);
            }

        } else {
            FileInputStream fin = null;
            FileOutputStream fout = null;
            byte[] buffer = new byte[4096];
            int bytesRead;

            try {
                fin = new FileInputStream(src);
                fout = new FileOutputStream(dest);

                while ((bytesRead = fin.read(buffer)) >= 0) {
                    fout.write(buffer, 0, bytesRead);
                }
            } finally {
                try {
                    if (fin != null)
                        fin.close();
                } catch (Throwable t) {
                }
                try {
                    if (fout != null)
                        fout.close();
                } catch (Throwable t) {
                }
            }
        }
    }

}
