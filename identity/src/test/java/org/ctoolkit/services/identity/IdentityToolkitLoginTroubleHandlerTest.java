/*
 * Copyright (c) 2017 Comvai, s.r.o. All Rights Reserved.
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

package org.ctoolkit.services.identity;

import com.google.api.client.http.HttpStatusCodes;
import com.google.common.base.Optional;
import com.google.identitytoolkit.GitkitClient;
import com.google.identitytoolkit.GitkitServerException;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import org.ctoolkit.restapi.client.identity.IdentityToolkitClient;
import org.testng.annotations.Test;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import static org.ctoolkit.services.identity.IdentityToolkitCheckSessionFilter.LOGIN_PATH;

/**
 * Testing of {@link IdentityToolkitLoginTroubleHandler}.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class IdentityToolkitLoginTroubleHandlerTest
{
    @Tested
    private IdentityToolkitLoginTroubleHandler tested;

    @Injectable
    private IdentityToolkitClient client;

    @SuppressWarnings( "MismatchedQueryAndUpdateOfCollection" )
    @Injectable
    private Set<IdentityTroubleListener> listeners = new HashSet<>();

    @Test( expectedExceptions = IllegalArgumentException.class )
    public void initNoLoginPath( final @Mocked ServletConfig config ) throws Exception
    {
        tested.init( config );
    }

    @Test
    public void fullInit( final @Mocked ServletConfig config ) throws Exception
    {
        new ServletConfigExpectations( config );

        tested.init( config );
    }

    @Test
    public void notificationFails( final @Mocked HttpServletRequest request,
                                   final @Mocked HttpServletResponse response,
                                   final @Mocked ServletConfig config,
                                   final @Mocked IdentityToolkitClient.OobResponse oobResponse,
                                   final @Mocked IdentityTroubleListener listener ) throws Exception
    {
        final PrintWriter writer = new PrintWriter( new StringWriter() );
        listeners.add( listener );

        new ServletConfigExpectations( config );

        new Expectations( writer )
        {
            {
                oobResponse.getOobAction();
                result = new GitkitServerException( "Error simulation" );

            }
        };

        tested.init( config );
        tested.doPost( request, response );

        new Verifications()
        {
            {
                listener.resetPassword( anyString, anyString );
                times = 0;

                listener.changeEmail( null, anyString, anyString );
                times = 0;

                writer.append( anyString );
                times = 0;

                response.setStatus( HttpStatusCodes.STATUS_CODE_SERVER_ERROR );
            }
        };
    }

    @Test
    public void resetPassword( final @Mocked HttpServletRequest request,
                               final @Mocked HttpServletResponse response,
                               final @Mocked ServletConfig config,
                               final @Mocked IdentityToolkitClient.OobResponse oobResponse,
                               final @Mocked IdentityTroubleListener listener ) throws Exception
    {
        // jmockit (1.19) does not allow to mock PrintWriter directly thus following construction is being used
        // Class java.io.PrintWriter cannot be @Mocked fully; instead, use @Injectable or partial mocking
        final PrintWriter writer = new PrintWriter( new StringWriter() );

        final String testBody = "<successful>true</successful>";
        final String link = "/test-reset-link";
        final Optional<String> url = Optional.of( link );
        final String email = "identity.test@ctoolkit.org";
        listeners.add( listener );

        new ServletConfigExpectations( config );

        new Expectations( writer )
        {
            {
                response.getWriter();
                result = writer;

                oobResponse.getOobAction();
                result = GitkitClient.OobAction.RESET_PASSWORD;

                oobResponse.getOobUrl();
                result = url;

                oobResponse.getEmail();
                result = email;

                oobResponse.getResponseBody();
                result = testBody;
            }
        };

        tested.init( config );
        tested.doPost( request, response );

        new Verifications()
        {
            {
                listener.changeEmail( null, anyString, anyString );
                times = 0;

                listener.resetPassword( email, link );
                writer.append( testBody );
            }
        };
    }

    @Test
    public void changeEmail( final @Mocked HttpServletRequest request,
                             final @Mocked HttpServletResponse response,
                             final @Mocked ServletConfig config,
                             final @Mocked IdentityToolkitClient.OobResponse oobResponse,
                             final @Mocked IdentityTroubleListener listener ) throws Exception
    {
        // jmockit (1.19) does not allow to mock PrintWriter directly thus following construction is being used
        // Class java.io.PrintWriter cannot be @Mocked fully; instead, use @Injectable or partial mocking
        final PrintWriter writer = new PrintWriter( new StringWriter() );

        final String testBody = "<successful>true</successful>";
        final String email = "identity.test@ctoolkit.org";
        final String newEmail = "identity.new@ctoolkit.org";
        listeners.add( listener );

        new ServletConfigExpectations( config );

        new Expectations( writer )
        {
            {
                response.getWriter();
                result = writer;

                oobResponse.getOobAction();
                result = GitkitClient.OobAction.CHANGE_EMAIL;

                oobResponse.getEmail();
                result = email;

                oobResponse.getNewEmail();
                result = newEmail;

                oobResponse.getResponseBody();
                result = testBody;
            }
        };

        tested.init( config );
        tested.doPost( request, response );

        new Verifications()
        {
            {
                listener.resetPassword( anyString, anyString );
                times = 0;

                listener.changeEmail( null, email, newEmail );
                writer.append( testBody );
            }
        };
    }

    @SuppressWarnings( "WeakerAccess" )
    final class ServletConfigExpectations
            extends Expectations
    {
        String LOGIN_VALUE = "/login_attribute_value";

        ServletConfigExpectations( ServletConfig config )
        {
            config.getInitParameter( LOGIN_PATH );
            result = LOGIN_VALUE;
        }
    }
}