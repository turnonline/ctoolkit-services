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

import com.google.common.base.Strings;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * The application's session filter to manage authenticated session.
 * If there is an valid token and unauthenticated session (filter config value value of the
 * {@link #SESSION_AUTH_USER_ATTRIBUTE}) filter will call
 * {@link IdentityLoginListener#processIdentity(HttpServletRequest, HttpServletResponse, FirebaseToken, String)}
 * to make it authenticated (client's implementation responsibility).
 * Once session become authenticated, token cookie will be removed.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
@Singleton
@ThreadSafe
public class IdentityCheckSessionFilter
        implements Filter
{
    private static final Logger logger = LoggerFactory.getLogger( IdentityCheckSessionFilter.class );

    /**
     * The filter config attribute to configure key where authenticated identity user will be placed in session
     */
    public static String SESSION_AUTH_USER_ATTRIBUTE = "_identity_filter_SESSION_AUTH_USER_ATTRIBUTE";

    /**
     * The filter config attribute to configure a redirect path, for example "/my-account".
     * Once configured, logged in user, filter matching at these paths LOGIN_PATH or SIGN_UP_PATH,
     * will be redirected to this.
     */
    public static String REDIRECT_PATH = "_identity_filter_REDIRECT_TO_IF_LOGGED_IN";

    /**
     * The filter config attribute to configure a sign up path, for example "/sign-up"
     */
    public static String SIGN_UP_PATH = "_identity_filter_SIGN_UP_PATH";

    /**
     * The filter config attribute to configure a login path, for example "/login"
     */
    public static String LOGIN_PATH = "_identity_filter_LOGIN_PATH";

    /**
     * The filter config attribute to configure a list of comma separated servlet paths to be ignored by filter
     */
    public static String IGNORE_PATHS = "_identity_filter_IGNORE_PATHS";

    private final IdentityHandler identityHandler;

    private final Set<IdentityLoginListener> listeners;

    private String sessionAttribute;

    private String loggedInRedirect;

    private String signUpPath;

    private String loginPath;

    private Set<String> ignorePaths = new HashSet<>();

    @Inject
    public IdentityCheckSessionFilter( IdentityHandler identityHandler, Set<IdentityLoginListener> listeners )
    {
        this.identityHandler = identityHandler;
        this.listeners = listeners;
    }

    @Override
    public void init( FilterConfig filterConfig )
            throws ServletException
    {
        this.sessionAttribute = filterConfig.getInitParameter( SESSION_AUTH_USER_ATTRIBUTE );
        this.loggedInRedirect = filterConfig.getInitParameter( REDIRECT_PATH );
        this.signUpPath = filterConfig.getInitParameter( SIGN_UP_PATH );
        this.loginPath = filterConfig.getInitParameter( LOGIN_PATH );

        if ( Strings.isNullOrEmpty( this.sessionAttribute ) )
        {
            throw new IllegalArgumentException( "Session attribute must be configured. " );
        }
        if ( Strings.isNullOrEmpty( this.loggedInRedirect ) )
        {
            this.loggedInRedirect = "";
            logger.warn( "No REDIRECT_PATH has been configured!" );
        }
        if ( Strings.isNullOrEmpty( this.signUpPath ) )
        {
            this.signUpPath = "";
            logger.warn( "No SIGN_UP_PATH has been configured!" );
        }
        if ( Strings.isNullOrEmpty( this.loginPath ) )
        {
            this.loginPath = "";
            logger.warn( "No LOGIN_PATH has been configured!" );
        }

        initIgnorePaths( filterConfig );
    }

    @Override
    public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain )
            throws IOException, ServletException
    {
        HttpServletRequest httpRequest = ( HttpServletRequest ) request;
        HttpServletResponse httpResponse = ( HttpServletResponse ) response;

        if ( ignoreServletPath( httpRequest ) )
        {
            chain.doFilter( request, response );
            return;
        }

        if ( httpRequest.getSession().getAttribute( sessionAttribute ) == null )
        {
            FirebaseToken identity = identityHandler.resolveVerifyToken( httpRequest );
            String signedEmail = identity != null ? identity.getEmail() : null;

            if ( signedEmail != null )
            {
                // the user is logged in but authenticated session has not been created yet
                for ( IdentityLoginListener listener : listeners )
                {
                    listener.processIdentity( httpRequest, httpResponse, identity, sessionAttribute );
                }

                // identity cookie is not needed now
                identityHandler.delete( httpRequest, httpResponse );
            }
        }
        else
        {
            if ( !Strings.isNullOrEmpty( loggedInRedirect ) &&
                    ( httpRequest.getRequestURI().startsWith( signUpPath )
                            || httpRequest.getRequestURI().startsWith( loginPath ) ) )
            {
                // if user is logged in redirect him for these pages
                httpResponse.sendRedirect( loggedInRedirect );
            }
        }

        chain.doFilter( request, response );
    }

    /**
     * Initializes the ignore paths with values taken from the comma separated string.
     */
    private void initIgnorePaths( final FilterConfig filterConfig )
    {
        String value = filterConfig.getInitParameter( IGNORE_PATHS );
        if ( !Strings.isNullOrEmpty( value ) )
        {
            for ( String path : value.split( "," ) )
            {
                path = path.trim();
                if ( !path.startsWith( "/" ) )
                {
                    path = "/" + path;
                }
                ignorePaths.add( path );
            }
        }
    }

    /**
     * Checks whether given request has servlet path to be ignored.
     *
     * @param request the current http request
     * @return true if the request should be ignored, otherwise false
     */
    private boolean ignoreServletPath( final HttpServletRequest request )
    {
        if ( !ignorePaths.isEmpty() )
        {
            String servletPath = request.getServletPath();
            if ( !Strings.isNullOrEmpty( servletPath ) )
            {
                for ( String toBeIgnored : ignorePaths )
                {
                    if ( servletPath.startsWith( toBeIgnored ) )
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public void destroy()
    {
    }
}
