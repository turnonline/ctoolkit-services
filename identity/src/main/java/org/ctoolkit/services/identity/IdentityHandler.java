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
import org.ctoolkit.restapi.client.TokenVerifier;
import org.ctoolkit.restapi.client.identity.Identity;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A handler class to wrap identity verification in to a standalone class and provide convenient methods.
 *
 * @author <a href="mailto:jozef.pohorelec@ctoolkit.org">Jozef Pohorelec</a>
 */
@Singleton
public final class IdentityHandler
{
    /**
     * Default cookie name of the identity toolkit token
     */
    public static final String GTOKEN = "gtoken";

    private final TokenVerifier<Identity> tokenVerifier;

    @Inject
    public IdentityHandler( TokenVerifier<Identity> tokenVerifier )
    {

        this.tokenVerifier = tokenVerifier;
    }

    /**
     * Verifies identity token taken from the request against public certs.
     * Once verification is successful returns populated identity instance.
     * If verification fails or token has expired returns <code>null</code>.
     *
     * @param request the HTTP request
     * @return the successfully verified identity instance or null
     */
    public Identity resolve( @Nonnull HttpServletRequest request )
    {
        checkNotNull( request );

        String token = getToken( request );

        if ( !Strings.isNullOrEmpty( token ) )
        {
            Identity json = tokenVerifier.verifyAndGet( token );

            if ( json.getExpiration().after( new Date() ) )
            {
                return json;
            }
        }

        return null;
    }

    /**
     * Returns the identity token from the request. Searched either in headers (first) or cookies.
     * If not found returns <code>null</code>.
     *
     * @param request the HTTP request
     * @return the identity token
     */
    public final String getToken( @Nonnull HttpServletRequest request )
    {
        checkNotNull( request );

        String token = request.getHeader( GTOKEN );
        if ( !Strings.isNullOrEmpty( token ) )
        {
            return token;
        }

        // not found in header thus search in cookies
        Cookie[] cookies = request.getCookies();

        if ( cookies == null )
        {
            return null;
        }

        for ( Cookie cookie : cookies )
        {
            if ( GTOKEN.equals( cookie.getName() ) )
            {
                token = cookie.getValue();
            }
        }
        return token;
    }

    /**
     * Delete identity toolkit token cookie.
     *
     * @param request  the HTTP servlet request
     * @param response the HTTP servlet response
     */
    public void delete( @Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response )
    {
        checkNotNull( request );
        checkNotNull( response );

        Cookie[] cookies = request.getCookies();

        if ( cookies == null )
        {
            return;
        }

        for ( Cookie cookie : cookies )
        {
            if ( GTOKEN.equals( cookie.getName() ) )
            {
                //the zero value causes the cookie to be deleted
                cookie.setMaxAge( 0 );
                cookie.setValue( "" );
                cookie.setPath( "/" );

                response.addCookie( cookie );
            }
        }
    }
}
