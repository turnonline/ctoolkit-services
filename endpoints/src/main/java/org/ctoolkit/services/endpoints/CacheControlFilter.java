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

import com.google.common.net.HttpHeaders;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The filter to configure HTTP response 'Cache-Control' max age header for an associated URL pattern.
 *
 * @author <a href="mailto:medvegy@turnonline.biz">Aurel Medvegy</a>
 */
public abstract class CacheControlFilter
        implements Filter
{
    private String cacheControl;

    @Override
    public void init( FilterConfig config )
    {
        Integer maxAge = getMaxAge();
        cacheControl = "public,max-age=" + maxAge;
    }

    @Override
    public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain )
            throws IOException, ServletException
    {
        HttpServletResponse httpResponse = ( HttpServletResponse ) response;
        httpResponse.addHeader( HttpHeaders.CACHE_CONTROL, cacheControl );
        httpResponse.setHeader( HttpHeaders.VARY, getVary() );

        chain.doFilter( request, response );
    }

    @Override
    public void destroy()
    {
    }

    /**
     * The 'Cache-Control' max age init parameter configured in seconds.
     *
     * @return the max age in seconds
     */
    protected abstract Integer getMaxAge();

    /**
     * Override this if you want to provide custom 'Vary' header. Default is 'Referer'
     *
     * @return vary header value
     */
    protected String getVary()
    {
        return "Referer";
    }
}
