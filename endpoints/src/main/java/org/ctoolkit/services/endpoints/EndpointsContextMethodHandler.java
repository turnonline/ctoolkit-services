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

import com.google.api.server.spi.EndpointMethod;
import com.google.api.server.spi.EndpointsContext;
import com.google.api.server.spi.ServletInitializationParameters;
import com.google.api.server.spi.SystemService;
import com.google.api.server.spi.config.model.ApiConfig;
import com.google.api.server.spi.config.model.ApiMethodConfig;
import com.google.api.server.spi.config.model.ApiSerializationConfig;
import com.google.api.server.spi.handlers.EndpointsMethodHandler;
import com.google.api.server.spi.request.ParamReader;
import com.google.api.server.spi.request.RestServletRequestParamReader;
import com.google.common.annotations.VisibleForTesting;

import javax.servlet.ServletContext;

/**
 * Setting {@link EndpointsContext} as servlet context attribute {@link ServletContext#setAttribute(String, Object)}.
 */
class EndpointsContextMethodHandler
        extends EndpointsMethodHandler
{
    private final EndpointMethod endpointMethod;

    private final ApiMethodConfig methodConfig;

    private final ServletContext servletContext;

    EndpointsContextMethodHandler( ServletInitializationParameters initParameters,
                                   ServletContext servletContext,
                                   EndpointMethod endpointMethod,
                                   ApiConfig apiConfig,
                                   ApiMethodConfig methodConfig,
                                   SystemService systemService )
    {
        super( initParameters, servletContext, endpointMethod, apiConfig, methodConfig, systemService );
        this.endpointMethod = endpointMethod;
        this.methodConfig = methodConfig;
        this.servletContext = servletContext;
    }


    @VisibleForTesting
    protected ParamReader createRestParamReader( EndpointsContext context,
                                                 ApiSerializationConfig serializationConfig )
    {
        RestServletRequestParamReader reader = new RestServletRequestParamReader( endpointMethod, context.getRequest(),
                servletContext, serializationConfig, methodConfig, context.getRawPathParameters() );

        servletContext.setAttribute( EndpointsContext.class.getName(), context );
        return reader;
    }
}
