/*
 * Copyright (c) 2019 Comvai, s.r.o. All Rights Reserved.
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

import com.google.api.server.spi.auth.EspAuthenticator;
import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Authenticator;
import com.google.api.server.spi.config.Singleton;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import javax.servlet.http.HttpServletRequest;

import static org.ctoolkit.services.endpoints.ThirdPartyToServerAuthenticator.ON_BEHALF_OF_EMAIL;
import static org.ctoolkit.services.endpoints.ThirdPartyToServerAuthenticator.ON_BEHALF_OF_USER_ID;

/**
 * <p>
 * Intended for server to server calls within TurnOnline.biz Ecosystem. JWT based Authenticator.
 * Issuer and Audience of the caller must match with Endpoints Configuration, otherwise user will be unauthenticated.
 * This will work smoothly if server to server calls are within the same project (Google Cloud Project)
 * as Issuer and Audience of the caller and called are same by nature.
 * Implementation is using {@link EspAuthenticator}.
 * </p>
 * <p>
 * In order to impersonate call these HTTP request headers must be present,
 * otherwise returns {@code null} as unauthenticated.
 * <ul>
 *     <li><strong>vnd.turnon.cloud.on-behalf-of-email</strong></li>
 *     <li><strong>vnd.turnon.cloud.on-behalf-of-user-id</strong></li>
 *     <li><strong>vnd.turnon.cloud.on-behalf-of-audience</strong></li>
 * </ul>
 * <p>
 * The authenticated user is type of the {@link VerifiedUser} and is available as request attribute:
 * <p>
 * {@code request.getAttribute( VerifiedUser.class.getName() );}
 *
 * @author <a href="mailto:medvegy@turnonline.biz">Aurel Medvegy</a>
 * @see VerifiedUser
 * @see EspAuthenticator
 */
@ThreadSafe
@Singleton
public class ClosedServerToServerAuthenticator
        implements Authenticator
{
    public static final String ON_BEHALF_OF_AUDIENCE = "vnd.turnon.cloud.on-behalf-of-audience";

    private static final Logger LOGGER = LoggerFactory.getLogger( ClosedServerToServerAuthenticator.class );

    private final EspAuthenticator authenticator;

    public ClosedServerToServerAuthenticator()
    {
        this( new EspAuthenticator() );
    }

    @VisibleForTesting
    ClosedServerToServerAuthenticator( EspAuthenticator authenticator )
    {
        this.authenticator = authenticator;
    }

    /**
     * Once successfully authenticated, returns {@link VerifiedUser} with intended user's audience.
     * If any of 'on behalf of' header is missing, it will return {@code null} as unauthenticated.
     *
     * @return the authenticated user or {@code null} if authentication did not pass
     * (if any of the preconditions had not meet)
     */
    @Override
    public User authenticate( HttpServletRequest request )
    {
        String email = request.getHeader( ON_BEHALF_OF_EMAIL );
        String userId = request.getHeader( ON_BEHALF_OF_USER_ID );
        String audience = request.getHeader( ON_BEHALF_OF_AUDIENCE );

        // first check headers presence in order to pass to the next authenticator as quickly as possible
        if ( Strings.isNullOrEmpty( email )
                || Strings.isNullOrEmpty( userId )
                || Strings.isNullOrEmpty( audience ) )
        {
            LOGGER.info( "All of the header properties 'email', 'userId', 'audience' must be present: "
                    + MoreObjects.toStringHelper( "VerifiedUser" )
                    .add( "email", email )
                    .add( "userId", userId )
                    .add( "audience", audience )
                    .toString() );

            return null;
        }

        User user = authenticator.authenticate( request );
        if ( user == null )
        {
            return null;
        }

        VerifiedUser.Builder builder = new VerifiedUser.Builder()
                .email( email )
                .userId( userId )
                .audience( audience )
                .serviceAccount( user.getEmail() );

        VerifiedUser verifiedUser = builder.build();
        request.setAttribute( VerifiedUser.class.getName(), verifiedUser );

        return verifiedUser;
    }
}
