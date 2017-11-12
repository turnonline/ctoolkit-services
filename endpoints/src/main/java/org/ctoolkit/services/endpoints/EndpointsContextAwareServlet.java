/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ctoolkit.services.endpoints;

import com.google.api.server.spi.EndpointMethod;
import com.google.api.server.spi.EndpointsContext;
import com.google.api.server.spi.ServletInitializationParameters;
import com.google.api.server.spi.Strings;
import com.google.api.server.spi.SystemService;
import com.google.api.server.spi.SystemService.EndpointNode;
import com.google.api.server.spi.config.ApiConfigException;
import com.google.api.server.spi.config.model.ApiClassConfig.MethodConfigMap;
import com.google.api.server.spi.config.model.ApiConfig;
import com.google.api.server.spi.config.model.ApiMethodConfig;
import com.google.api.server.spi.dispatcher.PathDispatcher;
import com.google.api.server.spi.guice.ServiceMap;
import com.google.api.server.spi.handlers.ApiProxyHandler;
import com.google.api.server.spi.handlers.CorsHandler;
import com.google.api.server.spi.handlers.ExplorerHandler;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;

/**
 * The endpoints servlet that makes {@link EndpointsContext} available via guice static injection for later processing.
 * In order to employ this servlet, change your Guice configuration, for example:
 * <pre>
 * {@code
 * public class MyEndpointsModule
 *          extends EndpointsModule
 * {
 *    @literal @Override
 *     protected void configureEndpoints( String urlPattern,
 *                                        ServletInitializationParameters initParameters,
 *                                        boolean useLegacyServlet )
 *     {
 *         ...
 *         super.serve( urlPattern ).with( EndpointsContextAwareServlet.class, initParameters.asMap() );
 *     }
 * }
 * }
 * </pre>
 * In order to make it available use static injection, for example:
 * <pre>
 * {@code
 * @literal @Inject
 * private static Injector injector;
 *
 * @literal @Inject
 * private Provider<ServletContext> context;
 *
 * // before use of EndpointsContext, call injector
 * injector.injectMembers( this );
 *
 * // then
 * EndpointsContext ec = EndpointsContext.class.cast( context.get().getAttribute( EndpointsContext.class.getName() ) );
 * }
 * </pre>
 * And don't forget {@link com.google.inject.AbstractModule#requestInjection(Object)};
 *
 * @see com.google.api.server.spi.EndpointsServlet
 * @see com.google.api.server.spi.guice.GuiceEndpointsServlet
 * @see com.google.api.server.spi.handlers.EndpointsMethodHandler
 */
@Singleton
public class EndpointsContextAwareServlet
        extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    private static final String EXPLORER_PATH = "explorer";

    private final ServiceMap services;

    private ServletInitializationParameters initParameters;

    private SystemService systemService;

    private PathDispatcher<EndpointsContext> dispatcher;

    private CorsHandler corsHandler;

    @Inject
    public EndpointsContextAwareServlet( ServiceMap services )
    {
        this.services = Preconditions.checkNotNull( services, "services" );
    }

    @Override
    public void init( ServletConfig config ) throws ServletException
    {
        super.init( config );
        ClassLoader classLoader = getClass().getClassLoader();
        this.initParameters = ServletInitializationParameters.fromServletConfig( config, classLoader );
        this.systemService = createSystemService( classLoader, initParameters );
        this.dispatcher = createDispatcher();
        this.corsHandler = new CorsHandler();
    }

    @Override
    public void service( HttpServletRequest request, HttpServletResponse response ) throws IOException
    {
        String method = getRequestMethod( request );
        if ( "OPTIONS".equals( method ) )
        {
            corsHandler.handle( request, response );
        }
        else
        {
            String path = Strings.stripSlash(
                    request.getRequestURI().substring( request.getServletPath().length() ) );
            EndpointsContext context = new EndpointsContext( method, path, request, response );
            if ( !dispatcher.dispatch( method, path, context ) )
            {
                response.setStatus( HttpServletResponse.SC_NOT_FOUND );
                response.getWriter().append( "Not Found" );
            }
        }
    }

    private String getRequestMethod( HttpServletRequest request )
    {
        Enumeration headerNames = request.getHeaderNames();
        String methodOverride = null;
        while ( headerNames.hasMoreElements() )
        {
            String headerName = ( String ) headerNames.nextElement();
            if ( headerName.toLowerCase().equals( "x-http-method-override" ) )
            {
                methodOverride = request.getHeader( headerName );
                break;
            }
        }
        return methodOverride != null ? methodOverride.toUpperCase() : request.getMethod();
    }

    private PathDispatcher<EndpointsContext> createDispatcher()
    {
        PathDispatcher.Builder<EndpointsContext> builder = PathDispatcher.builder();
        List<EndpointNode> endpoints = systemService.getEndpoints();
        // We're building an ImmutableList here, because it will eventually be used for JSON-RPC.
        ImmutableList.Builder<EndpointsContextMethodHandler> handlersBuilder = ImmutableList.builder();
        for ( EndpointNode endpoint : endpoints )
        {
            ApiConfig apiConfig = endpoint.getConfig();
            MethodConfigMap methods = apiConfig.getApiClassConfig().getMethods();
            for ( Entry<EndpointMethod, ApiMethodConfig> methodEntry : methods.entrySet() )
            {
                if ( !methodEntry.getValue().isIgnored() )
                {
                    handlersBuilder.add(
                            new EndpointsContextMethodHandler( initParameters, getServletContext(), methodEntry.getKey(),
                                    apiConfig, methodEntry.getValue(), systemService ) );
                }
            }
        }
        ImmutableList<EndpointsContextMethodHandler> handlers = handlersBuilder.build();
        for ( EndpointsContextMethodHandler handler : handlers )
        {
            builder.add( handler.getRestMethod(), Strings.stripTrailingSlash( handler.getRestPath() ),
                    handler.getRestHandler() );
        }
        ExplorerHandler explorerHandler = new ExplorerHandler();
        builder.add( "GET", EXPLORER_PATH, explorerHandler );
        builder.add( "GET", EXPLORER_PATH + "/", explorerHandler );
        builder.add( "GET", "static/proxy.html", new ApiProxyHandler() );
        return builder.build();
    }

    private SystemService createSystemService( ClassLoader classLoader,
                                               ServletInitializationParameters initParameters ) throws ServletException
    {
        try
        {
            SystemService.Builder builder = SystemService.builder()
                    .withDefaults( classLoader )
                    .setStandardConfigLoader( classLoader )
                    .setIllegalArgumentIsBackendError( initParameters.isIllegalArgumentBackendError() )
                    .setDiscoveryServiceEnabled( true );
            for ( Class<?> serviceClass : initParameters.getServiceClasses() )
            {
                builder.addService( serviceClass, createService( serviceClass ) );
            }
            return builder.build();
        }
        catch ( ApiConfigException | ClassNotFoundException e )
        {
            throw new ServletException( e );
        }
    }

    private <T> T createService( Class<T> serviceClass )
    {
        return services.get( serviceClass );
    }
}
