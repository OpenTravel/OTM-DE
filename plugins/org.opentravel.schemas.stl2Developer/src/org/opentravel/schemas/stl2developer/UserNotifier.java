
package org.opentravel.schemas.stl2developer;

/**
 * UNUSED -
 * 
 * @author Agnieszka Janowska
 * 
 */
public interface UserNotifier {

    void openWarning(String title, String message);

    void openInformation(String title, String message);

    void openError(String title, String message);

    boolean openConfirm(String title, String message);

    boolean openQuestion(String title, String question);

    int openQuestionWithCancel(String title, String question);

}
