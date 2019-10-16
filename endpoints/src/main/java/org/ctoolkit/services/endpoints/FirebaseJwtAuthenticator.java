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

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GooglePublicKeysManager;
import com.google.api.server.spi.Client;
import com.google.api.server.spi.auth.GoogleAuth;
import com.google.api.server.spi.auth.GoogleJwtAuthenticator;
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

/**
 * Intended for client to server calls within TurnOnline.biz Open Ecosystem.
 * The Firebase JWT token thread-safe authenticator that ignores (no additional checks)
 * validated token's audience and issuer. Inspired by {@link GoogleJwtAuthenticator}.
 * <p>
 * The user is type of the {@link AudienceUser} and is available as request attribute:
 * <p>
 * {@code (AudienceUser) request.getAttribute( AudienceUser.class.getName() );}
 *
 * @author <a href="mailto:medvegy@turnonline.biz">Aurel Medvegy</a>
 */
@ThreadSafe
@Singleton
public class FirebaseJwtAuthenticator
        implements Authenticator
{
    private static final Logger logger = LoggerFactory.getLogger( FirebaseJwtAuthenticator.class );

    private static final String PUBLIC_CERTS_URL = "https://www.googleapis.com/service_accounts/v1/metadata/x509/securetoken@system.gserviceaccount.com";

    private final GoogleIdTokenVerifier verifier;

    public FirebaseJwtAuthenticator()
    {
        verifier = new GoogleIdTokenVerifier.Builder(
                new GooglePublicKeysManager.Builder(
                        Client.getInstance().getHttpTransport(),
                        Client.getInstance().getJsonFactory() )
                        .setPublicCertsEncodedUrl( PUBLIC_CERTS_URL )
                        .build() )
                // no check against issuers
                .setIssuer( null )
                .build();
    }

    /**
     * Valid token will result in a new instance {@link AudienceUser} with all these properties populated.
     * <ul>
     *     <li>subject as {@link AudienceUser#getId()}</li>
     *     <li>email as {@link AudienceUser#getEmail()}</li>
     *     <li>aud as {@link AudienceUser#getAudience()}</li>
     * </ul>
     * If some of these properties are not present {@code null} will be returned.
     */
    @Override
    public User authenticate( HttpServletRequest request )
    {
        String token = GoogleAuth.getAuthToken( request );

        if ( !GoogleAuth.isJwt( token ) )
        {
            return null;
        }

        GoogleIdToken idToken;
        try
        {
            idToken = getVerifier().verify( token );
            if ( idToken == null )
            {
                return null;
            }
        }
        catch ( Exception e )
        {
            logger.warn( "Error while verifying JWT", e );
            return null;
        }

        String userId = idToken.getPayload().getSubject();
        String email = idToken.getPayload().getEmail();
        String audience = ( String ) idToken.getPayload().getAudience();

        AudienceUser user;
        if ( Strings.isNullOrEmpty( email )
                || Strings.isNullOrEmpty( userId )
                || Strings.isNullOrEmpty( audience ) )
        {
            logger.info( "All of the User properties 'email', 'subject' (userId), and 'audience' must be present: "
                    + MoreObjects.toStringHelper( "User" )
                    .add( "email", email )
                    .add( "userId", userId )
                    .add( "audience", audience )
                    .toString() );

            return null;
        }
        else
        {
            AudienceUser.Builder builder = new AudienceUser.Builder();
            builder.email( email ).userId( userId ).audience( audience ).token( token );
            user = builder.build();

            request.setAttribute( AudienceUser.class.getName(), user );
        }

        logger.info( "Firebase authenticated user: " + user.getId() );

        return user;
    }

    @VisibleForTesting
    GoogleIdTokenVerifier getVerifier()
    {
        return verifier;
    }
}
