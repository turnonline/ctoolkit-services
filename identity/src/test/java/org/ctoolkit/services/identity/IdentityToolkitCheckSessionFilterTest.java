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

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import org.ctoolkit.restapi.client.identity.Identity;
import org.testng.annotations.Test;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.Set;

import static org.ctoolkit.services.identity.IdentityToolkitCheckSessionFilter.IGNORE_PATHS;
import static org.ctoolkit.services.identity.IdentityToolkitCheckSessionFilter.LOGIN_PATH;
import static org.ctoolkit.services.identity.IdentityToolkitCheckSessionFilter.REDIRECT_PATH;
import static org.ctoolkit.services.identity.IdentityToolkitCheckSessionFilter.SESSION_AUTH_USER_ATTRIBUTE;
import static org.ctoolkit.services.identity.IdentityToolkitCheckSessionFilter.SIGN_UP_PATH;


/**
 * Testing of {@link IdentityToolkitCheckSessionFilter}.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class IdentityToolkitCheckSessionFilterTest
{
    private final static String SESSION_ATTR_VALUE = "session_attribute_value";

    @Tested
    private IdentityToolkitCheckSessionFilter tested;

    @Injectable
    private IdentityHandler identityHandler;

    @SuppressWarnings( "MismatchedQueryAndUpdateOfCollection" )
    @Injectable
    private Set<IdentityLoginListener> listeners = new HashSet<>();

    @Test( expectedExceptions = IllegalArgumentException.class )
    public void initNoSessionAttribute( final @Mocked FilterConfig config )
            throws Exception
    {
        tested.init( config );
    }

    @Test
    public void fullInit( final @Mocked FilterConfig config ) throws Exception
    {
        new FilterConfigExpectations( config );

        tested.init( config );
    }

    @Test
    public void ignorePath1( final @Mocked FilterConfig config,
                             final @Mocked HttpServletRequest request,
                             final @Mocked HttpServletResponse response,
                             final @Mocked FilterChain chain ) throws Exception
    {
        new FilterConfigExpectations( config );

        new Expectations()
        {
            {
                request.getServletPath();
                result = "/pathY";
            }
        };

        tested.init( config );
        tested.doFilter( request, response, chain );

        new Verifications()
        {
            {
                identityHandler.resolve( ( HttpServletRequest ) any );
                times = 0;

                chain.doFilter( request, response );
            }
        };
    }

    @Test
    public void processIdentity( final @Mocked HttpServletRequest request,
                                 final @Mocked HttpServletResponse response,
                                 final @Mocked HttpSession session,
                                 final @Mocked FilterChain chain,
                                 final @Mocked FilterConfig config,
                                 final @Mocked Identity identity,
                                 final @Mocked IdentityLoginListener listener ) throws Exception
    {
        listeners.add( listener );

        new FilterConfigExpectations( config );

        new Expectations()
        {
            {
                identityHandler.resolve( ( HttpServletRequest ) any );
                result = identity;

                identity.getEmail();
                result = "identity.test@ctoolkit.org";

                session.getAttribute( SESSION_ATTR_VALUE );
                result = null;
            }
        };

        tested.init( config );
        tested.doFilter( request, response, chain );

        new Verifications()
        {
            {
                listener.processIdentity( request, response, identity, SESSION_ATTR_VALUE );

                response.sendRedirect( anyString );
                times = 0;

                session.invalidate();
                times = 0;

                chain.doFilter( request, response );
            }
        };
    }

    @Test
    public void sendRedirect( final @Mocked HttpServletRequest request,
                              final @Mocked HttpServletResponse response,
                              final @Mocked HttpSession session,
                              final @Mocked FilterChain chain,
                              final @Mocked FilterConfig config,
                              final @Mocked Identity identity,
                              final @Mocked IdentityLoginListener listener ) throws Exception
    {
        listeners.add( listener );

        new FilterConfigExpectations( config );

        new Expectations()
        {
            {
                identityHandler.resolve( ( HttpServletRequest ) any );
                result = identity;

                identity.getEmail();
                result = "identity.test@ctoolkit.org";

                session.getAttribute( SESSION_ATTR_VALUE );
                result = "non null value";

                request.getRequestURI();
                result = FilterConfigExpectations.LOGIN_VALUE;
            }
        };

        tested.init( config );
        tested.doFilter( request, response, chain );

        new Verifications()
        {
            {
                listener.processIdentity( ( HttpServletRequest ) any, ( HttpServletResponse ) any,
                        ( Identity ) any, anyString );
                times = 0;

                session.invalidate();
                times = 0;

                response.sendRedirect( FilterConfigExpectations.REDIRECT_VALUE );
                chain.doFilter( request, response );
            }
        };
    }

    @Test
    public void invalidateSession( final @Mocked HttpServletRequest request,
                                   final @Mocked HttpServletResponse response,
                                   final @Mocked HttpSession session,
                                   final @Mocked FilterChain chain,
                                   final @Mocked FilterConfig config,
                                   final @Mocked IdentityLoginListener listener ) throws Exception
    {
        listeners.add( listener );

        new FilterConfigExpectations( config );

        new Expectations()
        {
            {
                session.getAttribute( SESSION_ATTR_VALUE );
                result = "non null value";
            }
        };

        tested.init( config );
        tested.doFilter( request, response, chain );

        new Verifications()
        {
            {
                listener.processIdentity( ( HttpServletRequest ) any, ( HttpServletResponse ) any,
                        ( Identity ) any, anyString );
                times = 0;

                response.sendRedirect( FilterConfigExpectations.REDIRECT_VALUE );
                times = 0;

                session.invalidate();
                chain.doFilter( request, response );
            }
        };
    }

    private final static class FilterConfigExpectations
            extends Expectations
    {
        static String REDIRECT_VALUE = "/redirect_attribute_value";

        static String SIGN_UP_VALUE = "/sign_up_attribute_value";

        static String LOGIN_VALUE = "/login_attribute_value";

        static String IGNORE_PATHS_VALUES = "pathY,/path2";

        FilterConfigExpectations( FilterConfig filterConfig )
        {
            filterConfig.getInitParameter( SESSION_AUTH_USER_ATTRIBUTE );
            result = SESSION_ATTR_VALUE;

            filterConfig.getInitParameter( REDIRECT_PATH );
            result = REDIRECT_VALUE;

            filterConfig.getInitParameter( SIGN_UP_PATH );
            result = SIGN_UP_VALUE;

            filterConfig.getInitParameter( LOGIN_PATH );
            result = LOGIN_VALUE;

            filterConfig.getInitParameter( IGNORE_PATHS );
            result = IGNORE_PATHS_VALUES;
        }
    }
}