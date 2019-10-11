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
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
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
 * The Firebase JWT token thread-safe authenticator that ignores (no additional checks)
 * validated token's audience and issuer. Inspired by {@link GoogleJwtAuthenticator}.
 * <p>
 * The user is type of the {@link VerifiedUser} and is available as request attribute:
 * <p>
 * {@code (VerifiedUser) request.getAttribute( VerifiedUser.class.getName() );}
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

    private static final GoogleIdTokenVerifier verifier;

    static
    {
        HttpTransport transport = Client.getInstance().getHttpTransport();
        JsonFactory jsonFactory = Client.getInstance().getJsonFactory();

        GooglePublicKeysManager.Builder keyBuilder = new GooglePublicKeysManager.Builder( transport, jsonFactory );
        keyBuilder.setPublicCertsEncodedUrl( PUBLIC_CERTS_URL );

        GoogleIdTokenVerifier.Builder builder = new GoogleIdTokenVerifier.Builder( keyBuilder.build() );
        // no check against issuers
        builder.setIssuer( null );

        verifier = builder.build();
    }

    /**
     * Valid token will result in a new instance {@link VerifiedUser} with all these properties populated.
     * <ul>
     *     <li>subject as {@link VerifiedUser#getId()}</li>
     *     <li>email as {@link VerifiedUser#getEmail()}</li>
     *     <li>aud as {@link VerifiedUser#getAudience()}</li>
     * </ul>
     * If some of these properties are not present {@code null} will be returned.
     */
    @Override
    public User authenticate( HttpServletRequest request )
    {
        String token = GoogleAuth.getAuthToken( request );

        if ( !GoogleAuth.isJwt( token ) )
        {
            logger.warn( "Not a JWT token." );
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

        VerifiedUser user;
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
            VerifiedUser.Builder builder = new VerifiedUser.Builder();
            builder.email( email ).userId( userId ).audience( audience ).token( token );
            user = builder.build();

            request.setAttribute( VerifiedUser.class.getName(), user );
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
