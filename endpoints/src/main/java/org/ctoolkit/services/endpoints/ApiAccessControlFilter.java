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

package org.ctoolkit.services.endpoints;

import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Engine adds these headers by default, thus adding these headers is useful mainly for local development.
 * These headers are added:
 * <ul>
 * <li>access-control-allow-credentials: true</li>
 * <li>access-control-allow-origin: *</li>
 * <li>access-control-allow-methods: DELETE,GET,HEAD,PATCH,POST,PUT</li>
 * <li>access-control-allow-headers: content-type,authorization,x-http-method-override</li>
 * </ul>
 *
 * @author <a href="mailto:medvegy@turnonline.biz">Aurel Medvegy</a>
 */
@Singleton
public class ApiAccessControlFilter
        implements Filter
{
    @Override
    public void init( FilterConfig filterConfig ) throws ServletException
    {
    }

    @Override
    public void doFilter( ServletRequest request, ServletResponse servletResponse, FilterChain chain )
            throws IOException, ServletException
    {
        HttpServletResponse response = ( HttpServletResponse ) servletResponse;

        response.addHeader( "access-control-allow-credentials", "true" );
        response.addHeader( "access-control-allow-origin", "*" );
        response.addHeader( "access-control-allow-methods", "DELETE,GET,HEAD,PATCH,POST,PUT" );

        String value = "content-type,authorization,x-http-method-override";
        response.addHeader( "access-control-allow-headers", value );
        chain.doFilter( request, response );
    }

    @Override
    public void destroy()
    {
    }
}
