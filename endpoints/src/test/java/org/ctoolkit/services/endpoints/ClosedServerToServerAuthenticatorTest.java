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
import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
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
    private static final String EMAIL = "john.bee@ctoolkit.org";

    private static final String USER_ID = "6THGW5tz6";

    private static final String AUDIENCE = "ctoolkit-pid";

    private static final String SERVICE_ACCOUNT = "sa-name@my-project-id.iam.gserviceaccount.com";

    @Tested
    private ClosedServerToServerAuthenticator tested;

    @Mocked
    private EspAuthenticator authenticator;

    @Mocked
    private HttpServletRequest request;

    private User serviceAccountUser = new User( null, SERVICE_ACCOUNT );

    @BeforeMethod
    public void before()
    {
        tested = new ClosedServerToServerAuthenticator( authenticator );
    }

    @Test
    public void authenticate_ValidResponse()
    {
        new Expectations()
        {
            {
                request.getHeader( ON_BEHALF_OF_EMAIL );
                result = EMAIL;

                request.getHeader( ON_BEHALF_OF_USER_ID );
                result = USER_ID;

                request.getHeader( ON_BEHALF_OF_AUDIENCE );
                result = AUDIENCE;

                authenticator.authenticate( request );
                result = serviceAccountUser;
            }
        };

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

        assertWithMessage( "Authenticated user email" )
                .that( verifiedUser.getAudience() )
                .isEqualTo( AUDIENCE );

        assertWithMessage( "Authenticated user origin service account" )
                .that( verifiedUser.getServiceAccount() )
                .isEqualTo( SERVICE_ACCOUNT );

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
    public void authenticate_AuthenticationFailure()
    {
        new Expectations()
        {
            {
                request.getHeader( ON_BEHALF_OF_EMAIL );
                result = EMAIL;

                request.getHeader( ON_BEHALF_OF_USER_ID );
                result = USER_ID;

                request.getHeader( ON_BEHALF_OF_AUDIENCE );
                result = AUDIENCE;

                authenticator.authenticate( request );
                result = null;
            }
        };

        assertThat( tested.authenticate( request ) ).isNull();
    }

    @Test
    public void authenticate_MissingEmailHeader()
    {
        new Expectations()
        {
            {
                request.getHeader( ON_BEHALF_OF_EMAIL );
                result = null;

                request.getHeader( ON_BEHALF_OF_USER_ID );
                result = USER_ID;

                request.getHeader( ON_BEHALF_OF_AUDIENCE );
                result = AUDIENCE;
            }
        };

        assertThat( tested.authenticate( request ) ).isNull();
    }

    @Test
    public void authenticate_MissingUserIdHeader()
    {
        new Expectations()
        {
            {
                request.getHeader( ON_BEHALF_OF_EMAIL );
                result = EMAIL;

                request.getHeader( ON_BEHALF_OF_USER_ID );
                result = null;

                request.getHeader( ON_BEHALF_OF_AUDIENCE );
                result = AUDIENCE;
            }
        };

        assertThat( tested.authenticate( request ) ).isNull();
    }

    @Test
    public void authenticate_MissingAudienceHeader()
    {
        new Expectations()
        {
            {
                request.getHeader( ON_BEHALF_OF_EMAIL );
                result = EMAIL;

                request.getHeader( ON_BEHALF_OF_USER_ID );
                result = USER_ID;

                request.getHeader( ON_BEHALF_OF_AUDIENCE );
                result = null;
            }
        };

        assertThat( tested.authenticate( request ) ).isNull();
    }
}