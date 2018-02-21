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
import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Authenticator;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

/**
 * The Firebase JWT token thread-safe authenticator that ignores validated token's audience and issuer.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
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

    @Override
    public User authenticate( HttpServletRequest request )
    {
        String token = GoogleAuth.getAuthToken( request );

        if ( token == null )
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
            logger.warn( e.getMessage() );
            return null;
        }

        String userId = idToken.getPayload().getSubject();
        String email = idToken.getPayload().getEmail();

        User user;
        if ( email == null )
        {
            return null;
        }
        else
        {
            if ( userId == null )
            {
                user = new User( email );
            }
            else
            {
                user = new User( userId, email );
            }
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
