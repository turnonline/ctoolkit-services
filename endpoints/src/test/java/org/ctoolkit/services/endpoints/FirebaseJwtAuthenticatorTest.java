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
import com.google.api.server.spi.auth.GoogleAuth;
import com.google.api.server.spi.auth.common.User;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * {@link FirebaseJwtAuthenticator} unit testing.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class FirebaseJwtAuthenticatorTest
{
    private static final String FAKE_TOKEN = "token-123";

    @Tested
    private FirebaseJwtAuthenticator tested;

    @Injectable
    private GoogleIdTokenVerifier verifier;

    @Test
    public void authenticateOk( @Mocked final HttpServletRequest request,
                                @Mocked final GoogleIdTokenVerifier verifier,
                                @Mocked final GoogleIdToken idToken,
                                @SuppressWarnings( "unused" ) @Mocked GoogleAuth googleAuth )
            throws GeneralSecurityException, IOException
    {
        final GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setSubject( "userId123" );
        payload.setEmail( "verified@turnonline.biz" );
        payload.setAudience( "my-audience" );

        verificationPassedExpectations( tested, request, verifier, idToken, payload );

        User user = tested.authenticate( request );
        VerifiedUser verifiedUser = ( VerifiedUser ) user;

        assertNotNull( user );
        assertEquals( verifiedUser.getId(), "userId123" );
        assertEquals( verifiedUser.getEmail(), "verified@turnonline.biz" );
        assertEquals( verifiedUser.getToken(), FAKE_TOKEN );
        assertEquals( verifiedUser.getAudience(), "my-audience" );
    }

    @Test
    public void authenticateOkUserIdNull( @Mocked final HttpServletRequest request,
                                          @Mocked final GoogleIdTokenVerifier verifier,
                                          @Mocked final GoogleIdToken idToken,
                                          @SuppressWarnings( "unused" ) @Mocked GoogleAuth googleAuth )
            throws GeneralSecurityException, IOException
    {
        final GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmail( "verified@turnonline.biz" );

        verificationPassedExpectations( tested, request, verifier, idToken, payload );

        User user = tested.authenticate( request );

        assertNotNull( user );
        assertNull( user.getId() );
        assertEquals( user.getEmail(), "verified@turnonline.biz" );
    }

    @Test
    public void authenticateVerifiedButEmailNull( @Mocked final HttpServletRequest request,
                                                  @Mocked final GoogleIdTokenVerifier verifier,
                                                  @Mocked final GoogleIdToken idToken,
                                                  @SuppressWarnings( "unused" ) @Mocked GoogleAuth googleAuth )
            throws GeneralSecurityException, IOException
    {
        final GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setSubject( "userId123" );

        new Expectations( tested )
        {
            {
                GoogleAuth.getAuthToken( request );
                result = FAKE_TOKEN;

                tested.isJwt( FAKE_TOKEN );
                result = true;

                tested.getVerifier();
                result = verifier;

                verifier.verify( FAKE_TOKEN );
                result = idToken;

                idToken.getPayload();
                result = payload;

                request.setAttribute( VerifiedUser.class.getName(), any );
                times = 0;
            }
        };

        User user = tested.authenticate( request );

        assertNull( user );
    }

    @Test
    public void authenticateException( @Mocked final HttpServletRequest request,
                                       @Mocked final GoogleIdTokenVerifier verifier,
                                       @SuppressWarnings( "unused" ) @Mocked GoogleAuth googleAuth )
            throws GeneralSecurityException, IOException
    {
        new Expectations( tested )
        {
            {
                GoogleAuth.getAuthToken( request );
                result = FAKE_TOKEN;

                tested.isJwt( FAKE_TOKEN );
                result = true;

                tested.getVerifier();
                result = verifier;

                verifier.verify( FAKE_TOKEN );
                result = new Exception();
            }
        };

        User user = tested.authenticate( request );

        assertNull( user );
    }

    @Test
    public void authenticateInvalidJwtToken( @Mocked final HttpServletRequest request,
                                             @SuppressWarnings( "unused" ) @Mocked GoogleAuth googleAuth )
    {
        new Expectations( tested )
        {
            {
                GoogleAuth.getAuthToken( request );
                result = FAKE_TOKEN;

                tested.isJwt( FAKE_TOKEN );
                result = false;

                tested.getVerifier();
                times = 0;
            }
        };

        User user = tested.authenticate( request );

        assertNull( user );
    }

    @Test
    public void authenticateMissingToken( @Mocked final HttpServletRequest request,
                                          @SuppressWarnings( "unused" ) @Mocked GoogleAuth googleAuth )
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
    public void authenticateFail( @Mocked final HttpServletRequest request,
                                  @Mocked final GoogleIdTokenVerifier verifier,
                                  @SuppressWarnings( "unused" ) @Mocked GoogleAuth googleAuth )
            throws GeneralSecurityException, IOException
    {
        new Expectations( tested )
        {
            {
                GoogleAuth.getAuthToken( request );
                result = FAKE_TOKEN;

                tested.isJwt( FAKE_TOKEN );
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

    private void verificationPassedExpectations( final FirebaseJwtAuthenticator tested,
                                                 final HttpServletRequest request,
                                                 final GoogleIdTokenVerifier verifier,
                                                 final GoogleIdToken idToken,
                                                 final GoogleIdToken.Payload payload )
            throws GeneralSecurityException, IOException
    {
        new Expectations( tested )
        {
            {
                GoogleAuth.getAuthToken( request );
                result = FAKE_TOKEN;

                tested.isJwt( FAKE_TOKEN );
                result = true;

                tested.getVerifier();
                result = verifier;

                verifier.verify( FAKE_TOKEN );
                result = idToken;

                idToken.getPayload();
                result = payload;

                request.setAttribute( VerifiedUser.class.getName(), any );
                times = 1;
            }
        };
    }
}