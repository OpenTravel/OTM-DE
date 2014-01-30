
package org.opentravel.schemas.actions.proxy;

import java.net.Authenticator;
import java.net.InetAddress;
import java.net.PasswordAuthentication;
import java.net.UnknownHostException;

import org.eclipse.core.net.proxy.IProxyData;
import org.opentravel.schemas.stl2developer.Activator;

public class EclipseAuthenticator extends Authenticator {

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        IProxyData[] data = Activator.getDefault().getProxyService().getProxyData();
        if (data == null)
            return null;
        for (final IProxyData d : data) {
            if (d.getUserId() == null || d.getHost() == null)
                continue;
            if (d.getPort() == getRequestingPort() && hostMatches(d))
                return auth(d);
        }
        return null;
    }

    private PasswordAuthentication auth(final IProxyData d) {
        final String user = d.getUserId();
        final String pass = d.getPassword();
        final char[] passChar = pass != null ? pass.toCharArray() : new char[0];
        return new PasswordAuthentication(user, passChar);
    }

    private boolean hostMatches(final IProxyData d) {
        try {
            final InetAddress dHost = InetAddress.getByName(d.getHost());
            InetAddress rHost = getRequestingSite();
            if (rHost == null)
                rHost = InetAddress.getByName(getRequestingHost());
            return dHost.equals(rHost);
        } catch (UnknownHostException err) {
            return false;
        }
    }
}