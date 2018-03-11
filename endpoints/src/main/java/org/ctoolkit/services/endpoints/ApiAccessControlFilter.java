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

import com.google.common.net.HttpHeaders;

import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS;
import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS;
import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS;
import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;

/**
 * Engine adds these headers by default, thus adding these headers is useful mainly for local development.
 * These headers are added:
 * <ul>
 * <li>{@link HttpHeaders#ACCESS_CONTROL_ALLOW_CREDENTIALS}: true</li>
 * <li>{@link HttpHeaders#ACCESS_CONTROL_ALLOW_ORIGIN}: *</li>
 * <li>{@link HttpHeaders#ACCESS_CONTROL_ALLOW_METHODS}: DELETE,GET,HEAD,PATCH,POST,PUT</li>
 * <li>{@link HttpHeaders#ACCESS_CONTROL_ALLOW_HEADERS}: Content-Type,Authorization,x-http-method-override</li>
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

        response.addHeader( ACCESS_CONTROL_ALLOW_CREDENTIALS, "true" );
        response.addHeader( ACCESS_CONTROL_ALLOW_ORIGIN, "*" );
        response.addHeader( ACCESS_CONTROL_ALLOW_METHODS, "DELETE,GET,HEAD,PATCH,POST,PUT" );

        String value = CONTENT_TYPE + "," + AUTHORIZATION + ",x-http-method-override";
        response.addHeader( ACCESS_CONTROL_ALLOW_HEADERS, value );
        chain.doFilter( request, response );
    }

    @Override
    public void destroy()
    {
    }
}
