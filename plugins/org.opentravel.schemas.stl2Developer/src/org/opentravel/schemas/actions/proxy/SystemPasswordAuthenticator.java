
package org.opentravel.schemas.actions.proxy;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class SystemPasswordAuthenticator extends Authenticator {

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        String proxyUser = System.getProperty("http.proxyUser");
        String proxyPasswd = System.getProperty("http.proxyPassword");
        if (proxyUser != null && proxyPasswd != null) {
            return new PasswordAuthentication(proxyUser, proxyPasswd.toCharArray());
        }
        return null;
    }

}
