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
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.Iterator;
import java.util.Optional;

/**
 * Intended for third-party to TurnOnline.biz Ecosystem server (App Engine) calls.
 * It validates Google App Engine default service account and extracts its ProjectId as audience.
 *
 * @author <a href="mailto:medvegy@turnonline.biz">Aurel Medvegy</a>
 * @see VerifiedUser
 */
@Singleton
public class ThirdPartyToServerAuthenticator
        extends GoogleOAuth2Authenticator
{
    public static final String ON_BEHALF_OF_EMAIL = "vnd.turnon.cloud.on-behalf-of-email";

    public static final String ON_BEHALF_OF_USER_ID = "vnd.turnon.cloud.on-behalf-of-userId";

    private static final String X_ON_BEHALF_OF_EMAIL = "X-On-Behalf-Of-Email";

    private static final String X_ON_BEHALF_OF_USER_ID = "X-On-Behalf-Of-User-Id";

    private static final Logger LOGGER = LoggerFactory.getLogger( ThirdPartyToServerAuthenticator.class );

    /**
     * In case authenticated user it's not Google Cloud Project default service account
     * '<strong>my-project-id@appspot.gserviceaccount.com</strong>', it will return {@code null} as unauthenticated.
     * Once passes, it will return instance of {@link VerifiedUser} and mapping of its properties are following:
     * <ul>
     * <li>{@link VerifiedUser#getEmail()} populated from the request header {@link #ON_BEHALF_OF_EMAIL},
     * the email as an identification of the user on behalf of whom the service account has been authenticated</li>
     * <li>{@link VerifiedUser#getId()} populated from the request header {@link #ON_BEHALF_OF_USER_ID}</li>
     * <li>{@link VerifiedUser#getAudience()} <strong>applicationId</strong> taken from the default service account:
     * <strong>my-project-id</strong> from left of @</li>
     * <li>{@link VerifiedUser#getServiceAccount()} email address of the default service account</li>
     * </ul>
     * For the use case with authenticated default service account but even one of the 'on behalf of'
     * headers are missing, it will return authenticated service account of type {@link User}.
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

            String email = Optional.ofNullable( request.getHeader( ON_BEHALF_OF_EMAIL ) )
                    .orElse( request.getHeader( X_ON_BEHALF_OF_EMAIL ) );

            String userId = Optional.ofNullable( request.getHeader( ON_BEHALF_OF_USER_ID ) )
                    .orElse( request.getHeader( X_ON_BEHALF_OF_USER_ID ) );

            String audience = getApplicationId( strings );

            if ( Strings.isNullOrEmpty( email )
                    || Strings.isNullOrEmpty( userId )
                    || Strings.isNullOrEmpty( audience ) )
            {
                LOGGER.warn( "Not all required User properties taken from headers are present: "
                        + MoreObjects.toStringHelper( "User" )
                        .add( "email", email )
                        .add( "userId", userId )
                        .add( "audience", audience )
                        .add( "serviceAccount", serviceAccount ) );

                // no on behalf of user, return authenticated service account
                return authenticated;
            }

            VerifiedUser.Builder builder = new VerifiedUser.Builder();
            builder.email( email )
                    .userId( userId )
                    .audience( audience )
                    .serviceAccount( serviceAccount );

            return builder.build();
        }
        // unauthenticated
        return null;
    }

    private String getApplicationId( @NotNull Iterable<String> strings )
    {
        Iterator<String> it = strings.iterator();
        String next = it.hasNext() ? it.next() : "";
        return next.toLowerCase();
    }


    @VisibleForTesting
    User internalAuthenticate( HttpServletRequest request ) throws ServiceUnavailableException
    {
        return super.authenticate( request );
    }
}
