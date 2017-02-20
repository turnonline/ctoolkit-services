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

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * An helper class to wrap identity verification in to a standalone class.
 *
 * @author <a href="mailto:jozef.pohorelec@ctoolkit.org">Jozef Pohorelec</a>
 */
@Singleton
public class IdentityResolver
{
    /**
     * Default cookie name of the identity toolkit token
     */
    public static final String GTOKEN = "gtoken";

    private final TokenVerifier<Identity> tokenVerifier;

    @Inject
    public IdentityResolver( TokenVerifier<Identity> tokenVerifier )
    {

        this.tokenVerifier = tokenVerifier;
    }

    public Identity resolve( HttpServletRequest httpRequest )
    {
        Cookie[] cookies = httpRequest.getCookies();

        if ( cookies == null )
        {
            return null;
        }

        String token = null;

        for ( Cookie cookie : cookies )
        {
            if ( GTOKEN.equals( cookie.getName() ) )
            {
                token = cookie.getValue();
            }
        }

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
     * Delete identity toolkit token cookie.
     *
     * @param request  the HTTP servlet request
     * @param response the HTTP servlet response
     */
    public void delete( HttpServletRequest request, HttpServletResponse response )
    {
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
