
package org.opentravel.schemas.widgets;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

/**
 * A Simple Extension to the ErrorDialog class, allowing for Exception Stack Traces in the Details
 * area
 * 
 */
public class ErrorWithExceptionDialog extends ErrorDialog {

    private IStatus status;

    public ErrorWithExceptionDialog(Shell parentShell, String dialogTitle, String message,
            IStatus status, int displayMask) {
        super(parentShell, dialogTitle, message, status, displayMask);
        this.status = status;
    }

    public static int openError(Shell parentShell, String title, String message, IStatus status) {
        ErrorWithExceptionDialog dialog = new ErrorWithExceptionDialog(parentShell, title, message,
                status, IStatus.OK | IStatus.INFO | IStatus.WARNING | IStatus.ERROR);
        return dialog.open();
    }

    @Override
    protected List createDropDownList(Composite parent) {
        List rtc = super.createDropDownList(parent);
        if (status != null && status.getException() != null) {
            for (String line : joinStackTrace(status.getException()).split("\n")) {
                rtc.add(line);
            }
        }
        return rtc;
    }

    public static String joinStackTrace(Throwable e) {
        StringWriter writer = null;
        try {
            writer = new StringWriter();
            joinStackTrace(e, writer);
            return writer.toString();
        } finally {
            if (writer != null)
                try {
                    writer.close();
                } catch (IOException e1) {
                    // ignore
                }
        }
    }

    public static void joinStackTrace(Throwable e, StringWriter writer) {
        PrintWriter printer = null;
        try {
            printer = new PrintWriter(writer);

            while (e != null) {

                printer.println(e);
                StackTraceElement[] trace = e.getStackTrace();
                for (int i = 0; i < trace.length; i++)
                    printer.println("\tat " + trace[i]);

                e = e.getCause();
                if (e != null)
                    printer.println("Caused by:");
            }
        } finally {
            if (printer != null)
                printer.close();
        }
    }
}