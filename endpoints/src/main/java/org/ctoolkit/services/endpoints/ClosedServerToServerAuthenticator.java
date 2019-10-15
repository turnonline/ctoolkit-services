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

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GooglePublicKeysManager;
import com.google.api.server.spi.Client;
import com.google.api.server.spi.auth.GoogleAuth;
import com.google.api.server.spi.auth.GoogleJwtAuthenticator;
import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Authenticator;
import com.google.api.server.spi.config.Singleton;
import com.google.appengine.api.utils.SystemProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.ctoolkit.services.endpoints.ThirdPartyToServerAuthenticator.ON_BEHALF_OF_EMAIL;
import static org.ctoolkit.services.endpoints.ThirdPartyToServerAuthenticator.ON_BEHALF_OF_USER_ID;

/**
 * <p>
 * Intended for server to server calls within TurnOnline.biz Ecosystem.
 * </p>
 * <p>
 * JWT based Authenticator for App Engine;
 * service account format: <strong>my-project-id@appspot.gserviceaccount.com</strong>.
 * Project ID evaluated at runtime in Google managed environments.
 * Authenticating tokens issued as <strong>Firebase Custom Tokens</strong>.
 * Issuer and Audience of the caller must match, otherwise user will be unauthenticated.
 * This will work smoothly if server to server calls are within the same project (Project ID).
 * </p>
 * <p>
 * In order to impersonate call, these claims must be included in the token,
 * otherwise returns {@code null} as unauthenticated.
 * <ul>
 *     <li><strong>vnd.turnon.cloud.on-behalf-of-email</strong></li>
 *     <li><strong>vnd.turnon.cloud.on-behalf-of-user-id</strong></li>
 *     <li><strong>vnd.turnon.cloud.on-behalf-of-audience</strong></li>
 * </ul>
 * This HTTP request header must be present, otherwise processing of this Authenticator will be skipped
 * <ul>
 *     <li><strong>vnd.turnon.cloud.internal-call=true</strong></li>
 * </ul>
 * <p>
 * The authenticated user is type of the {@link AudienceUser} and is available as request attribute:
 * <p>
 * {@code request.getAttribute( AudienceUser.class.getName() );}
 *
 * @author <a href="mailto:medvegy@turnonline.biz">Aurel Medvegy</a>
 * @see AudienceUser
 * @see GoogleJwtAuthenticator inspired by GoogleJwtAuthenticator
 * @see <a href="https://firebase.google.com/docs/auth/admin/create-custom-tokens">Firebase Custom Tokens</a>
 */
@ThreadSafe
@Singleton
public class ClosedServerToServerAuthenticator
        implements Authenticator
{
    public static final String ON_BEHALF_OF_AUDIENCE = "vnd.turnon.cloud.on-behalf-of-audience";

    public static final String INTERNAL_CALL = "vnd.turnon.cloud.internal-call";

    private static final Logger LOGGER = LoggerFactory.getLogger( ClosedServerToServerAuthenticator.class );

    private static final String projectId = SystemProperty.applicationId.get();

    private final GoogleIdTokenVerifier verifier;

    public ClosedServerToServerAuthenticator()
    {
        this( new GoogleIdTokenVerifier.Builder(
                new GooglePublicKeysManager.Builder(
                        Client.getInstance().getHttpTransport(),
                        Client.getInstance().getJsonFactory() )
                        .setPublicCertsEncodedUrl( getCertUrl() )
                        .build() )
                .setIssuer( getIssuer() )
                .build()
        );
    }

    @VisibleForTesting
    ClosedServerToServerAuthenticator( GoogleIdTokenVerifier verifier )
    {
        this.verifier = verifier;
    }

    static String getCertUrl()
    {
        return String.format( "https://www.googleapis.com/robot/v1/metadata/x509/%s%%40appspot.gserviceaccount.com",
                checkNotNull( projectId, "Project ID is mandatory" ) );
    }

    static String getIssuer()
    {
        return checkNotNull( projectId, "Project ID is mandatory" ) + "@appspot.gserviceaccount.com";
    }

    /**
     * Once successfully authenticated, returns impersonated {@link AudienceUser} with intended user's audience.
     *
     * @return the authenticated user or {@code null} if authentication did not pass
     * (if any of the preconditions had not meet)
     */
    @Override
    public User authenticate( HttpServletRequest request )
    {
        String on = request.getHeader( INTERNAL_CALL );
        if ( !Boolean.parseBoolean( on ) )
        {
            // Not an internal call, skip processing
            return null;
        }

        String token = GoogleAuth.getAuthToken( request );
        if ( !GoogleAuth.isJwt( token ) )
        {
            return null;
        }

        Stopwatch stopwatch = Stopwatch.createStarted();
        GoogleIdToken idToken;

        try
        {
            idToken = verifier.verify( token );
        }
        catch ( GeneralSecurityException | IOException | IllegalArgumentException e )
        {
            LOGGER.error( "Error while verifying JWT", e );
            LOGGER.info( "Token failure took " + stopwatch.stop() );
            return null;
        }

        if ( idToken == null )
        {
            LOGGER.warn( "Unauthenticated token: " + token );
            LOGGER.info( "Token failure took " + stopwatch.stop() );
            return null;
        }
        LOGGER.info( "Token verification took " + stopwatch.stop() );

        String serviceAccount = idToken.getPayload().getSubject();

        @SuppressWarnings( "unchecked" )
        Map<String, Object> claims = ( Map<String, Object> ) idToken.getPayload().get( "claims" );
        String email = null;
        String userId = null;
        String audience = null;

        // check expected claims presence
        if ( claims != null && !claims.isEmpty() )
        {
            email = ( String ) claims.get( ON_BEHALF_OF_EMAIL );
            userId = ( String ) claims.get( ON_BEHALF_OF_USER_ID );
            audience = ( String ) claims.get( ON_BEHALF_OF_AUDIENCE );
        }

        if ( Strings.isNullOrEmpty( email )
                || Strings.isNullOrEmpty( userId )
                || Strings.isNullOrEmpty( audience ) )
        {
            LOGGER.warn( "Claims are: " + claims );
            LOGGER.info( "All of these claim properties 'email', 'userId', 'audience' must be present: "
                    + MoreObjects.toStringHelper( "AudienceUser" )
                    .add( "email", email )
                    .add( "userId", userId )
                    .add( "audience", audience )
                    .toString() );

            return null;
        }

        AudienceUser.Builder builder = new AudienceUser.Builder()
                .email( email )
                .userId( userId )
                .audience( audience )
                .serviceAccount( serviceAccount );

        AudienceUser verifiedUser = builder.build();
        request.setAttribute( AudienceUser.class.getName(), verifiedUser );

        return verifiedUser;
    }
}
