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
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;

import static com.google.common.truth.Truth.assertThat;
import static org.ctoolkit.services.endpoints.ServerToServerAuthenticator.ON_BEHALF_OF_EMAIL;
import static org.ctoolkit.services.endpoints.ServerToServerAuthenticator.ON_BEHALF_OF_USER_ID;

/**
 * {@link ServerToServerAuthenticator} unit testing.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class ServerToServerAuthenticatorTest
{
    private static final String PROJECT_ID = "my-project-id";

    private static final String SERVICE_ACCOUNT = PROJECT_ID + "@appspot.gserviceaccount.com";

    private static final String SERVICE_ACCOUNT_ID = "912372899392829W";

    private static final String EMAIL = "john.foo@ctoolkit.org";

    private static final String USER_ID = "765098234";

    @Tested
    private ServerToServerAuthenticator tested;

    @Injectable
    private String appId = PROJECT_ID;

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
        assertThat( authenticated ).isInstanceOf( OnBehalfOfUser.class );
        assertThat( authenticated.getEmail() ).isEqualTo( EMAIL );
        assertThat( authenticated.getId() ).isEqualTo( USER_ID );
        assertThat( ( ( OnBehalfOfUser ) authenticated ).getServiceAccount() ).isEqualTo( SERVICE_ACCOUNT );
        assertThat( ( ( OnBehalfOfUser ) authenticated ).getServiceAccountId() ).isEqualTo( SERVICE_ACCOUNT_ID );
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
    public void authenticate_ApplicationIdDidNotMatch() throws ServiceUnavailableException
    {
        User user = new User( null, "12887.nmkjdd@appspot.gserviceaccount.com" );

        new Expectations( tested )
        {
            {
                tested.internalAuthenticate( request );
                result = user;

                request.getHeader( ON_BEHALF_OF_EMAIL );
                result = "john.malkovic@ctoolkit.biz";
                minTimes = 0;

                request.getHeader( ON_BEHALF_OF_USER_ID );
                result = USER_ID;
                minTimes = 0;
            }
        };

        assertThat( tested.authenticate( request ) ).isNull();
    }
}