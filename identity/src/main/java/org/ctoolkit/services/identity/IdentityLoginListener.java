package org.ctoolkit.services.identity;

import org.ctoolkit.restapi.client.identity.Identity;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The listener to handle notification once a identity toolkit user has logged in.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public interface IdentityLoginListener
{
    /**
     * Gets notification about right logged in authenticated user.
     * The implementation is responsible to put target authenticated user in to session at given session attribute.
     *
     * @param request          the current HTTP request
     * @param response         the current HTTP response
     * @param identity         the identity instance of the logged in user
     * @param sessionAttribute the session attribute where parent filter will check an authenticated user presence
     * @throws IOException
     */
    void processIdentity( @Nonnull HttpServletRequest request,
                          @Nonnull HttpServletResponse response,
                          @Nonnull Identity identity,
                          @Nonnull String sessionAttribute ) throws IOException;
}
