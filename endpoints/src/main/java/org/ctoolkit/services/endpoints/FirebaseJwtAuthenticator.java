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
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

/**
 * The Firebase JWT token thread-safe authenticator that ignores validated token's audience and issuer.
 * Inspired by {@link GoogleJwtAuthenticator}.
 * <p>
 * The user is type of the {@link VerifiedUser} and is available as request attribute:
 * <p>
 * {@code (VerifiedUser) request.getAttribute( VerifiedUser.class.getName() );}
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
@Singleton
public class FirebaseJwtAuthenticator
        implements Authenticator
{
    private static final Logger logger = LoggerFactory.getLogger( FirebaseJwtAuthenticator.class );

    // Identifies JSON Web Tokens, from GoogleAuth.java
    private static final String BASE64_REGEX = "[a-zA-Z0-9+/=_-]{6,}+";

    private static final Pattern JWT_PATTERN =
            Pattern.compile( String.format( "%s\\.%s\\.%s", BASE64_REGEX, BASE64_REGEX, BASE64_REGEX ) );

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

    public static boolean isJwt( String token )
    {
        return token != null && JWT_PATTERN.matcher( token ).matches();
    }

    @Override
    public User authenticate( HttpServletRequest request )
    {
        String token = GoogleAuth.getAuthToken( request );

        if ( !isJwt( token ) )
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
            logger.warn( e.getMessage() );
            return null;
        }

        String userId = idToken.getPayload().getSubject();
        String email = idToken.getPayload().getEmail();
        String audience = ( String ) idToken.getPayload().getAudience();

        User user;
        if ( email == null )
        {
            return null;
        }
        else
        {
            VerifiedUser.Builder builder = new VerifiedUser.Builder();
            builder.email( email ).userId( userId ).audience( audience ).token( token );
            user = new VerifiedUser( builder );

            request.setAttribute( VerifiedUser.class.getName(), user );
        }

        logger.info( "Firebase authenticated user: " + user );

        return user;
    }

    @VisibleForTesting
    GoogleIdTokenVerifier getVerifier()
    {
        return verifier;
    }
}
