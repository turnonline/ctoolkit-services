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
import com.google.api.server.spi.auth.GoogleAuth;
import com.google.api.server.spi.auth.common.User;
import com.google.appengine.api.utils.SystemProperty;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.ctoolkit.services.endpoints.ClosedServerToServerAuthenticator.INTERNAL_CALL;
import static org.ctoolkit.services.endpoints.ClosedServerToServerAuthenticator.ON_BEHALF_OF_AUDIENCE;
import static org.ctoolkit.services.endpoints.ThirdPartyToServerAuthenticator.ON_BEHALF_OF_EMAIL;
import static org.ctoolkit.services.endpoints.ThirdPartyToServerAuthenticator.ON_BEHALF_OF_USER_ID;

/**
 * {@link ClosedServerToServerAuthenticator} unit testing.
 *
 * @author <a href="mailto:medvegy@turnonline.biz">Aurel Medvegy</a>
 */
public class ClosedServerToServerAuthenticatorTest
{
    private static final String FAKE_TOKEN = "eyJhbGciOiJSUzI1NiJ9.eyJh..";

    private static final String EMAIL = "john.bee@ctoolkit.org";

    private static final String USER_ID = "6THGW5tz6";

    private static final String AUDIENCE = "ctoolkit-pid";

    private static final String SERVICE_ACCOUNT = "sa-name@my-project-id.iam.gserviceaccount.com";

    @Tested
    private ClosedServerToServerAuthenticator tested;

    @Mocked
    private GoogleIdTokenVerifier verifier;

    @Mocked
    private HttpServletRequest request;

    @Mocked
    private GoogleIdToken idToken;

    private GoogleIdToken.Payload payload;

    @BeforeMethod
    public void before()
    {
        SystemProperty.applicationId.set( "turn-online-eu" );
        tested = new ClosedServerToServerAuthenticator( verifier );
        payload = new GoogleIdToken.Payload();
        payload.setSubject( SERVICE_ACCOUNT );
    }

    @Test
    public void authenticate_ValidResponse()
    {
        Map<String, Object> claims = claims( EMAIL, USER_ID, AUDIENCE );
        payload.set( "claims", claims );

        new Expectations()
        {
            {
                request.getHeader( INTERNAL_CALL );
                result = Boolean.TRUE.toString();

                idToken.getPayload();
                result = payload;
            }
        };

        mockUpAuthToken();

        User authenticated = tested.authenticate( request );

        assertWithMessage( "Authenticated user" )
                .that( authenticated )
                .isNotNull();

        assertWithMessage( "Authenticated user" )
                .that( authenticated )
                .isInstanceOf( VerifiedUser.class );

        VerifiedUser verifiedUser = ( VerifiedUser ) authenticated;
        assertWithMessage( "Authenticated user email" )
                .that( verifiedUser.getEmail() )
                .isEqualTo( EMAIL );

        assertWithMessage( "Authenticated user Id" )
                .that( verifiedUser.getId() )
                .isEqualTo( USER_ID );

        assertWithMessage( "Authenticated user audience" )
                .that( verifiedUser.getAudience() )
                .isEqualTo( AUDIENCE );

        assertWithMessage( "Authenticated user origin service account" )
                .that( verifiedUser.getServiceAccount() )
                .isEqualTo( SERVICE_ACCOUNT );

        assertWithMessage( "Public Certs Encoded Url" )
                .that( ClosedServerToServerAuthenticator.getCertUrl() )
                .isEqualTo( "https://www.googleapis.com/robot/v1/metadata/x509/turn-online-eu%40appspot.gserviceaccount.com" );

        assertWithMessage( "JWT issuer" )
                .that( ClosedServerToServerAuthenticator.getIssuer() )
                .isEqualTo( "turn-online-eu@appspot.gserviceaccount.com" );

        new Verifications()
        {
            {
                VerifiedUser vu;
                request.setAttribute( VerifiedUser.class.getName(), vu = withCapture() );

                assertWithMessage( "Authenticated user taken from request attribute" )
                        .that( vu )
                        .isSameAs( verifiedUser );
            }
        };
    }

    @Test
    public void authenticate_MissingHeaderInternalCall()
    {
        Map<String, Object> claims = claims( EMAIL, USER_ID, AUDIENCE );
        payload.set( "claims", claims );

        new Expectations()
        {
            {
                request.getHeader( INTERNAL_CALL );
                result = null;

                idToken.getPayload();
                result = payload;
                minTimes = 0;
            }
        };

        mockUpAuthToken();

        assertThat( tested.authenticate( request ) ).isNull();
    }

    @Test
    public void authenticate_HeaderInternalCall_False()
    {
        Map<String, Object> claims = claims( EMAIL, USER_ID, AUDIENCE );
        payload.set( "claims", claims );

        new Expectations()
        {
            {
                request.getHeader( INTERNAL_CALL );
                result = Boolean.FALSE.toString();

                idToken.getPayload();
                result = payload;
                minTimes = 0;
            }
        };

        mockUpAuthToken();

        assertThat( tested.authenticate( request ) ).isNull();
    }

    @Test
    public void authenticate_AuthenticationFailure() throws GeneralSecurityException, IOException
    {
        Map<String, Object> claims = claims( EMAIL, USER_ID, AUDIENCE );
        payload.set( "claims", claims );

        new Expectations()
        {
            {
                request.getHeader( INTERNAL_CALL );
                result = Boolean.TRUE.toString();

                verifier.verify( anyString );
                result = null;
            }
        };

        mockUpAuthToken();

        assertThat( tested.authenticate( request ) ).isNull();
    }

    @Test
    public void authenticate_AuthenticationFailure_GeneralSecurity() throws GeneralSecurityException, IOException
    {
        Map<String, Object> claims = claims( EMAIL, USER_ID, AUDIENCE );
        payload.set( "claims", claims );

        new Expectations()
        {
            {
                request.getHeader( INTERNAL_CALL );
                result = Boolean.TRUE.toString();

                verifier.verify( anyString );
                result = new GeneralSecurityException();
            }
        };

        mockUpAuthToken();

        assertThat( tested.authenticate( request ) ).isNull();
    }

    @Test
    public void authenticate_AuthenticationFailure_IO() throws GeneralSecurityException, IOException
    {
        Map<String, Object> claims = claims( EMAIL, USER_ID, AUDIENCE );
        payload.set( "claims", claims );

        new Expectations()
        {
            {
                request.getHeader( INTERNAL_CALL );
                result = Boolean.TRUE.toString();

                verifier.verify( anyString );
                result = new IOException();
            }
        };

        mockUpAuthToken();

        assertThat( tested.authenticate( request ) ).isNull();
    }

    @Test
    public void authenticate_AuthenticationFailure_IllegalArgument() throws GeneralSecurityException, IOException
    {
        Map<String, Object> claims = claims( EMAIL, USER_ID, AUDIENCE );
        payload.set( "claims", claims );

        new Expectations()
        {
            {
                request.getHeader( INTERNAL_CALL );
                result = Boolean.TRUE.toString();

                verifier.verify( anyString );
                result = new IllegalArgumentException();
            }
        };

        mockUpAuthToken();

        assertThat( tested.authenticate( request ) ).isNull();
    }

    @Test
    public void authenticate_NotJwtToken()
    {
        Map<String, Object> claims = claims( EMAIL, USER_ID, AUDIENCE );
        payload.set( "claims", claims );

        new Expectations()
        {
            {
                request.getHeader( INTERNAL_CALL );
                result = Boolean.TRUE.toString();
            }
        };

        new MockUp<GoogleAuth>()
        {
            @Mock
            public String getAuthToken( HttpServletRequest request )
            {
                return FAKE_TOKEN;
            }

            @Mock
            public boolean isJwt( String token )
            {
                return false;
            }
        };

        assertThat( tested.authenticate( request ) ).isNull();
    }

    @Test
    public void authenticate_MissingEmailClaim()
    {
        Map<String, Object> claims = claims( null, USER_ID, AUDIENCE );
        payload.set( "claims", claims );

        new Expectations()
        {
            {
                request.getHeader( INTERNAL_CALL );
                result = Boolean.TRUE.toString();

                idToken.getPayload();
                result = payload;
            }
        };

        mockUpAuthToken();

        assertThat( tested.authenticate( request ) ).isNull();
    }

    @Test
    public void authenticate_MissingUserIdClaim()
    {
        Map<String, Object> claims = claims( ON_BEHALF_OF_EMAIL, null, AUDIENCE );
        payload.set( "claims", claims );

        new Expectations()
        {
            {
                request.getHeader( INTERNAL_CALL );
                result = Boolean.TRUE.toString();

                idToken.getPayload();
                result = payload;
            }
        };

        mockUpAuthToken();

        assertThat( tested.authenticate( request ) ).isNull();
    }

    @Test
    public void authenticate_MissingAudienceClaim()
    {
        Map<String, Object> claims = claims( ON_BEHALF_OF_EMAIL, USER_ID, null );
        payload.set( "claims", claims );

        new Expectations()
        {
            {
                request.getHeader( INTERNAL_CALL );
                result = Boolean.TRUE.toString();

                idToken.getPayload();
                result = payload;
            }
        };

        mockUpAuthToken();

        assertThat( tested.authenticate( request ) ).isNull();
    }

    private Map<String, Object> claims( @Nullable String email, @Nullable String userId, @Nullable String audience )
    {
        Map<String, Object> claims = new HashMap<>();
        claims.put( ON_BEHALF_OF_EMAIL, email );
        claims.put( ON_BEHALF_OF_USER_ID, userId );
        claims.put( ON_BEHALF_OF_AUDIENCE, audience );
        return claims;
    }

    private void mockUpAuthToken()
    {
        new MockUp<GoogleAuth>()
        {
            @Mock
            public String getAuthToken( HttpServletRequest request )
            {
                return FAKE_TOKEN;
            }

            @Mock
            public boolean isJwt( String token )
            {
                return FAKE_TOKEN.equals( token );
            }
        };
    }
}