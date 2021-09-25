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
import com.google.api.server.spi.auth.GoogleAuth;
import com.google.api.server.spi.auth.common.User;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;

import static com.google.common.truth.Truth.assertWithMessage;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * {@link FirebaseJwtAuthenticator} unit testing.
 *
 * @author <a href="mailto:medvegy@turnonline.biz">Aurel Medvegy</a>
 */
@SuppressWarnings( "ResultOfMethodCallIgnored" )
public class FirebaseJwtAuthenticatorTest
{
    private static final String FAKE_TOKEN = "token-123";

    @Tested
    private FirebaseJwtAuthenticator tested;

    @Tested
    private FirebaseJwtAuthenticatorWithAliases testedAliases;

    @Tested
    private FirebaseTokenVerifier verifier;

    @Mocked
    private HttpServletRequest request;

    @Mocked
    private GoogleAuth googleAuth;

    @Mocked
    private GoogleIdToken idToken;

    @Test
    public void authenticate_Ok()
    {
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setSubject( "userId123" );
        payload.setEmail( "verified@turnonline.biz" );
        payload.setAudience( "my-audience" );

        passedExpectations( tested, request, verifier, idToken, payload );

        User user = tested.authenticate( request );
        AudienceUser verifiedUser = ( AudienceUser ) user;

        assertNotNull( user );
        assertEquals( verifiedUser.getId(), "userId123" );
        assertEquals( verifiedUser.getEmail(), "verified@turnonline.biz" );
        assertEquals( verifiedUser.getToken(), FAKE_TOKEN );
        assertEquals( verifiedUser.getAudience(), "my-audience" );
        assertEquals( verifiedUser.getAccess(), AudienceUser.Access.READ );

        new Verifications()
        {
            {
                AudienceUser vu;
                request.setAttribute( AudienceUser.class.getName(), vu = withCapture() );

                assertWithMessage( "Authenticated user taken from request attribute" )
                        .that( vu )
                        .isSameInstanceAs( verifiedUser );
            }
        };
    }

    @Test
    public void authenticate_GET_ReadAccess()
    {
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setSubject( "userId123" );
        payload.setEmail( "verified@turnonline.biz" );
        payload.setAudience( "my-audience" );

        passedExpectations( tested, request, verifier, idToken, payload );
        new Expectations()
        {
            {
                request.getMethod();
                result = "GET";
            }
        };

        User user = tested.authenticate( request );
        AudienceUser verifiedUser = ( AudienceUser ) user;

        assertNotNull( user );
        assertEquals( verifiedUser.getAccess(), AudienceUser.Access.READ );
    }

    @Test
    public void authenticate_HEAD_ReadAccess()
    {
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setSubject( "userId123" );
        payload.setEmail( "verified@turnonline.biz" );
        payload.setAudience( "my-audience" );

        passedExpectations( tested, request, verifier, idToken, payload );
        new Expectations()
        {
            {
                request.getMethod();
                result = "HEAD";
            }
        };

        User user = tested.authenticate( request );
        AudienceUser verifiedUser = ( AudienceUser ) user;

        assertNotNull( user );
        assertEquals( verifiedUser.getAccess(), AudienceUser.Access.READ );
    }

    @Test
    public void authenticate_OPTIONS_ReadAccess()
    {
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setSubject( "userId123" );
        payload.setEmail( "verified@turnonline.biz" );
        payload.setAudience( "my-audience" );

        passedExpectations( tested, request, verifier, idToken, payload );
        new Expectations()
        {
            {
                request.getMethod();
                result = "OPTIONS";
            }
        };

        User user = tested.authenticate( request );
        AudienceUser verifiedUser = ( AudienceUser ) user;

        assertNotNull( user );
        assertEquals( verifiedUser.getAccess(), AudienceUser.Access.READ );
    }

    @Test
    public void authenticate_POST_WriteAccess()
    {
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setSubject( "userId123" );
        payload.setEmail( "verified@turnonline.biz" );
        payload.setAudience( "my-audience" );

        passedExpectations( tested, request, verifier, idToken, payload );
        new Expectations()
        {
            {
                request.getMethod();
                result = "POST";
            }
        };

        User user = tested.authenticate( request );
        AudienceUser verifiedUser = ( AudienceUser ) user;

        assertNotNull( user );
        assertEquals( verifiedUser.getAccess(), AudienceUser.Access.WRITE );
    }

    @Test
    public void authenticate_PUT_WriteAccess()
    {
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setSubject( "userId123" );
        payload.setEmail( "verified@turnonline.biz" );
        payload.setAudience( "my-audience" );

        passedExpectations( tested, request, verifier, idToken, payload );
        new Expectations()
        {
            {
                request.getMethod();
                result = "PUT";
            }
        };

        User user = tested.authenticate( request );
        AudienceUser verifiedUser = ( AudienceUser ) user;

        assertNotNull( user );
        assertEquals( verifiedUser.getAccess(), AudienceUser.Access.WRITE );
    }

    @Test
    public void authenticate_PATCH_WriteAccess()
    {
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setSubject( "userId123" );
        payload.setEmail( "verified@turnonline.biz" );
        payload.setAudience( "my-audience" );

        passedExpectations( tested, request, verifier, idToken, payload );
        new Expectations()
        {
            {
                request.getMethod();
                result = "PATCH";
            }
        };

        User user = tested.authenticate( request );
        AudienceUser verifiedUser = ( AudienceUser ) user;

        assertNotNull( user );
        assertEquals( verifiedUser.getAccess(), AudienceUser.Access.WRITE );
    }

    @Test
    public void authenticate_DELETE_WriteAccess()
    {
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setSubject( "userId123" );
        payload.setEmail( "verified@turnonline.biz" );
        payload.setAudience( "my-audience" );

        passedExpectations( tested, request, verifier, idToken, payload );
        new Expectations()
        {
            {
                request.getMethod();
                result = "DELETE";
            }
        };

        User user = tested.authenticate( request );
        AudienceUser verifiedUser = ( AudienceUser ) user;

        assertNotNull( user );
        assertEquals( verifiedUser.getAccess(), AudienceUser.Access.WRITE );
    }

    @Test
    public void authenticate_NotOkUserIdNull()
    {
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmail( "verified@turnonline.biz" );
        payload.setSubject( null );
        payload.setAudience( "my-audience" );

        notPassedExpectations( tested, request, verifier, idToken, payload );

        assertNull( tested.authenticate( request ) );
    }

    @Test
    public void authenticate_NotOkUserAudienceNull()
    {
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmail( "verified@turnonline.biz" );
        payload.setSubject( "userId123" );
        payload.setAudience( null );

        notPassedExpectations( tested, request, verifier, idToken, payload );

        assertNull( tested.authenticate( request ) );
    }

    @Test
    public void authenticate_VerifiedButEmailNull()
    {
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setSubject( "userId123" );
        payload.setAudience( "my-audience" );

        notPassedExpectations( tested, request, verifier, idToken, payload );

        User user = tested.authenticate( request );

        assertNull( user );
    }

    @Test
    public void authenticateInvalidJwtToken()
    {
        new Expectations( tested )
        {
            {
                GoogleAuth.getAuthToken( request );
                result = FAKE_TOKEN;

                GoogleAuth.isJwt( FAKE_TOKEN );
                result = false;

                tested.getVerifier();
                times = 0;
            }
        };

        User user = tested.authenticate( request );

        assertNull( user );
    }

    @Test
    public void authenticateMissingToken()
    {
        new Expectations( tested )
        {
            {
                GoogleAuth.getAuthToken( request );
                result = null;

                tested.getVerifier();
                times = 0;
            }
        };

        User user = tested.authenticate( request );

        assertNull( user );
    }

    @Test
    public void authenticateFail()
    {
        new Expectations( tested )
        {
            {
                GoogleAuth.getAuthToken( request );
                result = FAKE_TOKEN;

                GoogleAuth.isJwt( FAKE_TOKEN );
                result = true;

                tested.getVerifier();
                result = verifier;

                verifier.verify( FAKE_TOKEN );
                result = null;
            }
        };

        User user = tested.authenticate( request );

        assertNull( user );
    }

    @Test
    public void authenticate_WithAudienceAliases()
    {
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setSubject( "userId123" );
        payload.setEmail( "verified@turnonline.biz" );
        payload.setAudience( "alias-b2" );

        passedExpectations( testedAliases, request, verifier, idToken, payload );

        User user = testedAliases.authenticate( request );
        AudienceUser verifiedUser = ( AudienceUser ) user;

        assertNotNull( user );
        assertEquals( verifiedUser.getId(), "userId123" );
        assertEquals( verifiedUser.getEmail(), "verified@turnonline.biz" );
        assertEquals( verifiedUser.getToken(), FAKE_TOKEN );
        assertEquals( verifiedUser.getAudience(), "target-audience" );
    }

    @Test
    public void authenticate_WithAudienceAliases_AliasDidNotMatch()
    {
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setSubject( "userId123" );
        payload.setEmail( "verified@turnonline.biz" );
        payload.setAudience( "alias-a3" );

        passedExpectations( testedAliases, request, verifier, idToken, payload );

        User user = testedAliases.authenticate( request );
        AudienceUser verifiedUser = ( AudienceUser ) user;

        assertNotNull( user );
        assertEquals( verifiedUser.getId(), "userId123" );
        assertEquals( verifiedUser.getEmail(), "verified@turnonline.biz" );
        assertEquals( verifiedUser.getToken(), FAKE_TOKEN );
        assertEquals( verifiedUser.getAudience(), "alias-a3" );
    }

    private void passedExpectations( FirebaseJwtAuthenticator tested,
                                     HttpServletRequest request,
                                     FirebaseTokenVerifier verifier,
                                     GoogleIdToken idToken,
                                     GoogleIdToken.Payload payload )
    {
        new Expectations( tested, verifier )
        {
            {
                GoogleAuth.getAuthToken( request );
                result = FAKE_TOKEN;

                GoogleAuth.isJwt( FAKE_TOKEN );
                result = true;

                tested.getVerifier();
                result = verifier;

                verifier.verify( FAKE_TOKEN );
                result = idToken;

                idToken.getPayload();
                result = payload;

                request.setAttribute( AudienceUser.class.getName(), any );
                times = 1;
            }
        };
    }

    private void notPassedExpectations( FirebaseJwtAuthenticator tested,
                                        HttpServletRequest request,
                                        FirebaseTokenVerifier verifier,
                                        GoogleIdToken idToken,
                                        GoogleIdToken.Payload payload )
    {
        new Expectations( tested )
        {
            {
                GoogleAuth.getAuthToken( request );
                result = FAKE_TOKEN;

                GoogleAuth.isJwt( FAKE_TOKEN );
                result = true;

                tested.getVerifier();
                result = verifier;

                verifier.verify( FAKE_TOKEN );
                result = idToken;

                idToken.getPayload();
                result = payload;

                request.setAttribute( AudienceUser.class.getName(), any );
                times = 0;
            }
        };
    }
}