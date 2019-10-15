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

import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.response.ServiceUnavailableException;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.ctoolkit.services.endpoints.ThirdPartyToServerAuthenticator.ON_BEHALF_OF_EMAIL;
import static org.ctoolkit.services.endpoints.ThirdPartyToServerAuthenticator.ON_BEHALF_OF_USER_ID;

/**
 * {@link ThirdPartyToServerAuthenticator} unit testing.
 *
 * @author <a href="mailto:medvegy@turnonline.biz">Aurel Medvegy</a>
 */
public class ThirdPartyToServerAuthenticatorTest
{
    private static final String PROJECT_ID = "my-project-id";

    private static final String SERVICE_ACCOUNT = PROJECT_ID + "@appspot.gserviceaccount.com";

    private static final String SERVICE_ACCOUNT_ID = "912372899392829W";

    private static final String EMAIL = "john.foo@ctoolkit.org";

    private static final String USER_ID = "765098234";

    @Tested
    private ThirdPartyToServerAuthenticator tested;

    @Mocked
    private HttpServletRequest request;

    @Test
    public void authenticate_Pass() throws ServiceUnavailableException
    {
        User user = new User( SERVICE_ACCOUNT_ID, SERVICE_ACCOUNT.toUpperCase() );

        new Expectations( tested )
        {
            {
                tested.internalAuthenticate( request );
                result = user;

                request.getHeader( ON_BEHALF_OF_EMAIL );
                result = EMAIL;

                request.getHeader( ON_BEHALF_OF_USER_ID );
                result = USER_ID;
            }
        };

        User authenticated = tested.authenticate( request );

        assertThat( authenticated ).isNotNull();
        assertThat( authenticated ).isInstanceOf( AudienceUser.class );
        assertThat( authenticated.getEmail() ).isEqualTo( EMAIL );
        assertThat( authenticated.getId() ).isEqualTo( USER_ID );

        AudienceUser verifiedUser = ( AudienceUser ) authenticated;
        assertThat( verifiedUser.getAudience() ).isEqualTo( PROJECT_ID );
        assertThat( verifiedUser.getServiceAccount() ).isEqualTo( SERVICE_ACCOUNT );

        new Verifications()
        {
            {
                AudienceUser vu;
                request.setAttribute( AudienceUser.class.getName(), vu = withCapture() );

                assertWithMessage( "Authenticated user taken from request attribute" )
                        .that( vu )
                        .isSameAs( verifiedUser );
            }
        };
    }

    @Test
    public void authenticate_PassXHeaders() throws ServiceUnavailableException
    {
        User user = new User( SERVICE_ACCOUNT_ID, SERVICE_ACCOUNT.toUpperCase() );

        new Expectations( tested )
        {
            {
                tested.internalAuthenticate( request );
                result = user;

                request.getHeader( "X-On-Behalf-Of-Email" );
                result = EMAIL;

                request.getHeader( "X-On-Behalf-Of-User-Id" );
                result = USER_ID;
            }
        };

        User authenticated = tested.authenticate( request );

        assertThat( authenticated ).isNotNull();
        assertThat( authenticated ).isInstanceOf( AudienceUser.class );
        assertThat( authenticated.getEmail() ).isEqualTo( EMAIL );
        assertThat( authenticated.getId() ).isEqualTo( USER_ID );
        assertThat( ( ( AudienceUser ) authenticated ).getAudience() ).isEqualTo( PROJECT_ID );
        assertThat( ( ( AudienceUser ) authenticated ).getServiceAccount() ).isEqualTo( SERVICE_ACCOUNT );
    }

    @Test
    public void authenticate_EmailHeaderMissing() throws ServiceUnavailableException
    {
        User user = new User( SERVICE_ACCOUNT_ID, SERVICE_ACCOUNT );

        new Expectations( tested )
        {
            {
                tested.internalAuthenticate( request );
                result = user;

                request.getHeader( ON_BEHALF_OF_EMAIL );
                result = null;

                request.getHeader( ON_BEHALF_OF_USER_ID );
                result = USER_ID;
            }
        };

        User authenticated = tested.authenticate( request );

        assertThat( authenticated ).isNotNull();
        assertThat( authenticated ).isInstanceOf( User.class );
        assertThat( authenticated ).isNotInstanceOf( AudienceUser.class );
        assertThat( authenticated.getEmail() ).isEqualTo( SERVICE_ACCOUNT );
        assertThat( authenticated.getId() ).isEqualTo( SERVICE_ACCOUNT_ID );
    }

    @Test
    public void authenticate_UserIdHeaderMissing() throws ServiceUnavailableException
    {
        User user = new User( SERVICE_ACCOUNT_ID, SERVICE_ACCOUNT );

        new Expectations( tested )
        {
            {
                tested.internalAuthenticate( request );
                result = user;

                request.getHeader( ON_BEHALF_OF_EMAIL );
                result = EMAIL;

                request.getHeader( ON_BEHALF_OF_USER_ID );
                result = null;
            }
        };

        User authenticated = tested.authenticate( request );

        assertThat( authenticated ).isNotNull();
        assertThat( authenticated ).isInstanceOf( User.class );
        assertThat( authenticated ).isNotInstanceOf( AudienceUser.class );
        assertThat( authenticated.getEmail() ).isEqualTo( SERVICE_ACCOUNT );
        assertThat( authenticated.getId() ).isEqualTo( SERVICE_ACCOUNT_ID );
    }

    @Test
    public void authenticate_NotServiceAccount() throws ServiceUnavailableException
    {
        User user = new User( SERVICE_ACCOUNT_ID, "john.malkovic@ctoolkit.biz" );

        new Expectations( tested )
        {
            {
                tested.internalAuthenticate( request );
                result = user;

                request.getHeader( ON_BEHALF_OF_EMAIL );
                result = EMAIL;
                minTimes = 0;

                request.getHeader( ON_BEHALF_OF_USER_ID );
                result = USER_ID;
                minTimes = 0;
            }
        };

        assertThat( tested.authenticate( request ) ).isNull();
    }

    @Test
    public void authenticate_MissingAllHeaders() throws ServiceUnavailableException
    {
        User user = new User( SERVICE_ACCOUNT_ID, SERVICE_ACCOUNT );

        new Expectations( tested )
        {
            {
                tested.internalAuthenticate( request );
                result = user;

                request.getHeader( ON_BEHALF_OF_EMAIL );
                result = null;

                request.getHeader( ON_BEHALF_OF_USER_ID );
                result = null;
            }
        };

        User authenticated = tested.authenticate( request );
        assertThat( authenticated ).isNotNull();
        assertThat( authenticated ).isInstanceOf( User.class );
        assertThat( authenticated ).isNotInstanceOf( AudienceUser.class );
        assertThat( authenticated.getEmail() ).isEqualTo( SERVICE_ACCOUNT );
        assertThat( authenticated.getId() ).isEqualTo( SERVICE_ACCOUNT_ID );
    }
}