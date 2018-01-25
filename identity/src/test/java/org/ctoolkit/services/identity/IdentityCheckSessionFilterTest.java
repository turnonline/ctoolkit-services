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

import com.google.firebase.auth.FirebaseToken;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import org.testng.annotations.Test;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.Set;

import static org.ctoolkit.services.identity.IdentityCheckSessionFilter.IGNORE_PATHS;
import static org.ctoolkit.services.identity.IdentityCheckSessionFilter.LOGIN_PATH;
import static org.ctoolkit.services.identity.IdentityCheckSessionFilter.REDIRECT_PATH;
import static org.ctoolkit.services.identity.IdentityCheckSessionFilter.SESSION_AUTH_USER_ATTRIBUTE;
import static org.ctoolkit.services.identity.IdentityCheckSessionFilter.SIGN_UP_PATH;


/**
 * Testing of {@link IdentityCheckSessionFilter}.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class IdentityCheckSessionFilterTest
{
    private final static String SESSION_ATTR_VALUE = "session_attribute_value";

    private final static String REDIRECT_VALUE = "/redirect_attribute_value";

    private final static String SIGN_UP_VALUE = "/sign_up_attribute_value";

    private final static String LOGIN_VALUE = "/login_attribute_value";

    private final static String IGNORE_PATHS_VALUES = "pathY,/path2";

    @Tested
    private IdentityCheckSessionFilter tested;

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
        filterConfigExpectations( config );

        tested.init( config );
    }

    @Test
    public void ignorePath1( final @Mocked FilterConfig config,
                             final @Mocked HttpServletRequest request,
                             final @Mocked HttpServletResponse response,
                             final @Mocked FilterChain chain ) throws Exception
    {
        filterConfigExpectations( config );

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
                identityHandler.resolveVerifyToken( ( HttpServletRequest ) any );
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
                                 final @Mocked FirebaseToken token,
                                 final @Mocked IdentityLoginListener listener ) throws Exception
    {
        listeners.add( listener );

        filterConfigExpectations( config );

        new Expectations()
        {
            {
                identityHandler.resolveVerifyToken( ( HttpServletRequest ) any );
                result = token;

                token.getEmail();
                result = "identity.test@ctoolkit.org";
            }
        };

        tested.init( config );
        tested.doFilter( request, response, chain );

        new Verifications()
        {
            {
                listener.processIdentity( request, response, token, SESSION_ATTR_VALUE );

                response.sendRedirect( anyString );
                times = 0;

                session.invalidate();
                times = 0;

                identityHandler.delete( request, response );

                chain.doFilter( request, response );
            }
        };
    }

    @Test
    public void sendRedirect( final @Mocked HttpServletRequest request,
                              final @Mocked HttpServletResponse response,
                              final @Mocked FilterChain chain,
                              final @Mocked FilterConfig config,
                              final @Mocked IdentityLoginListener listener ) throws Exception
    {
        listeners.add( listener );

        filterConfigExpectations( config );

        new Expectations()
        {
            {
                request.getSession().getAttribute( SESSION_ATTR_VALUE );
                result = "non null value";

                request.getRequestURI();
                result = LOGIN_VALUE;
            }
        };

        tested.init( config );
        tested.doFilter( request, response, chain );

        new Verifications()
        {
            {
                listener.processIdentity( ( HttpServletRequest ) any, ( HttpServletResponse ) any,
                        ( FirebaseToken ) any, anyString );
                times = 0;

                request.getSession().invalidate();
                times = 0;

                response.sendRedirect( REDIRECT_VALUE );
                chain.doFilter( request, response );
            }
        };
    }

    private void filterConfigExpectations( final FilterConfig filterConfig )
    {
        new Expectations()
        {
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
        };
    }
}