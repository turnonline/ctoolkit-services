/*
 * Copyright (c) 2018 Comvai, s.r.o. All Rights Reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.ctoolkit.services.endpoints;

import com.google.api.server.spi.auth.GoogleOAuth2Authenticator;
import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.response.ServiceUnavailableException;
import com.google.appengine.api.utils.SystemProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The {@link GoogleOAuth2Authenticator} extended by following functionality.
 * Authenticator to validate Google App Engine Default Service Account, whether the authenticated email
 * comes from the same Google Cloud Project. Intended only for server to server calls.
 * It's recommended to be used only in trusted environment,
 * where caller and called server are being served within a same project.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 * @see OnBehalfOfUser
 */
@Singleton
public class ServerToServerAuthenticator
        extends GoogleOAuth2Authenticator
{
    public static final String ON_BEHALF_OF_EMAIL = "X-On-Behalf-Of-Email";

    public static final String ON_BEHALF_OF_USER_ID = "X-On-Behalf-Of-User-Id";

    private final String applicationId;

    public ServerToServerAuthenticator()
    {
        this( SystemProperty.applicationId.get() );
    }

    public ServerToServerAuthenticator( String appId )
    {
        this.applicationId = checkNotNull( appId );
    }

    /**
     * In case authenticated user it's not a default service account of the current Google Cloud Project,
     * it will return {@code null} as unauthenticated.
     * If passed it returns instance of {@link OnBehalfOfUser} and mapping of the properties are following:
     * <ul>
     * <li>{@link User#getEmail()} populated from the request header {@link #ON_BEHALF_OF_EMAIL},
     * the email as an identification of the user on behalf of whom the service account has been authenticated</li>
     * <li>{@link User#getId()} populated from the request header {@link #ON_BEHALF_OF_USER_ID}</li>
     * <li>{@link OnBehalfOfUser#getServiceAccount()} email taken from the authenticated user</li>
     * <li>{@link OnBehalfOfUser#getServiceAccountId()} user Id taken from the authenticated user</li>
     * </ul>
     */
    @Override
    public User authenticate( HttpServletRequest request ) throws ServiceUnavailableException
    {
        User authenticated = internalAuthenticate( request );
        if ( authenticated != null
                && authenticated.getEmail().toLowerCase().endsWith( "appspot.gserviceaccount.com" ) )
        {
            String serviceAccount = authenticated.getEmail().toLowerCase();
            Iterable<String> strings = Splitter.on( "@" ).omitEmptyStrings().trimResults().split( serviceAccount );

            for ( String next : strings )
            {
                if ( applicationId.toLowerCase().equals( next.toLowerCase() ) )
                {
                    String email = request.getHeader( ON_BEHALF_OF_EMAIL );
                    String userId = request.getHeader( ON_BEHALF_OF_USER_ID );

                    OnBehalfOfUser.Builder builder = new OnBehalfOfUser.Builder();
                    builder.email( email )
                            .userId( userId )
                            .serviceAccount( serviceAccount )
                            .serviceAccountId( authenticated.getId() );

                    return new OnBehalfOfUser( builder );
                }
            }
        }
        // unauthenticated
        return null;
    }

    @VisibleForTesting
    User internalAuthenticate( HttpServletRequest request ) throws ServiceUnavailableException
    {
        return super.authenticate( request );
    }
}
