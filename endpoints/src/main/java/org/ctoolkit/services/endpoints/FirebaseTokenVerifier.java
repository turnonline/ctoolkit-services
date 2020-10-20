/*
 * Copyright (c) 2020 Comvai, s.r.o. All Rights Reserved.
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
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The dedicated Firebase thread-safe token verifier.
 * <p>
 * <b>Default {@link GoogleIdTokenVerifier}</b>
 * configuration checks the issuer against fixed list of expected issuers.
 * However, within TurnOnline.biz Ecosystem we want to allow any of the issuers.
 * </p>
 *
 * @author <a href="mailto:medvegy@turnonline.biz">Aurel Medvegy</a>
 */
public class FirebaseTokenVerifier
{
    private static final Logger LOGGER = LoggerFactory.getLogger( FirebaseTokenVerifier.class );

    private static final String PUBLIC_CERTS_URL = "https://www.googleapis.com/service_accounts/v1/metadata/x509/securetoken@system.gserviceaccount.com";

    private final GoogleIdTokenVerifier verifier;

    public FirebaseTokenVerifier()
    {
        verifier = new GoogleIdTokenVerifier.Builder(
                new GooglePublicKeysManager.Builder(
                        Client.getInstance().getHttpTransport(),
                        Client.getInstance().getJsonFactory() )
                        .setPublicCertsEncodedUrl( PUBLIC_CERTS_URL )
                        .build() )
                // null to suppress the issuer check, will be validated later
                .setIssuer( null )
                .build();
    }

    /**
     * Verifies that the given token is valid and returns the ID token if succeeded.
     *
     * @param token Google ID token string
     * @return Google ID token if verified successfully or {@code null} if failed
     */
    public GoogleIdToken verify( String token )
    {
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
            LOGGER.warn( "Error while verifying token", e );
            return null;
        }

        return idToken;
    }

    @VisibleForTesting
    GoogleIdTokenVerifier getVerifier()
    {
        return verifier;
    }
}
