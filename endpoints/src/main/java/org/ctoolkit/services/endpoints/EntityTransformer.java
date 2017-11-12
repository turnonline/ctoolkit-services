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

import com.google.api.server.spi.EndpointsContext;
import com.google.api.server.spi.config.ResourceTransformer;
import com.google.common.base.Strings;
import com.google.inject.Injector;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.ServletContext;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The abstract resource transformer to get target entity be populated directly from the input JSON map.
 * In your implementation of {@link #transformFrom(Map)} populate only those properties (patch)
 * that are presented in the input JSON map, for example:
 * <pre>
 * {@code
 * @literal @Override
 * public MyEntity transformFrom( Map<String, Object> map )
 * {
 *     MyEntity entity = super.transformFrom( map );
 *
 *     Optional<String> sValue = getString( map, "firstName" );
 *     if ( sValue.isPresent() )
 *     {
 *         entity.setFirstName( sValue.get() );
 *     }
 * }
 * }
 * </pre>
 * Transformer requires following configurations in your Guice modules.
 * <p>
 * First, requests must be server via {@link EndpointsContextAwareServlet}, see javadoc.
 * Next, in order to make Guice injection work use:
 * <p>
 * {@code
 * MyModule#requestStaticInjection(EntityTransformer.class);
 * }
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public abstract class EntityTransformer<TFrom>
        implements ResourceTransformer<TFrom>
{
    @Inject
    private static Injector injector;

    /**
     * The property is part of the API method with path 'res1/{id}'.
     */
    private final String ENTITY_ID;

    @Inject
    private Provider<ServletContext> context;

    /**
     * Public content transformer initialized for default API path 'res1/{id}'.
     */
    public EntityTransformer()
    {
        this( "id" );
    }

    /**
     * The entity transformer initialized with customized {@link #ENTITY_ID} value.
     *
     * @param entityId the key to be used to take a value from the path parameters in order to retrieve entity
     */
    public EntityTransformer( String entityId )
    {
        ENTITY_ID = checkNotNull( entityId );
    }

    @Override
    public TFrom transformFrom( Map<String, Object> map )
    {
        if ( injector == null )
        {
            String msg = Injector.class.getSimpleName()
                    + " cannot be null. Configure first: MyModule#requestStaticInjection(EntityTransformer.class);";

            throw new NullPointerException( msg );
        }

        injector.injectMembers( this );

        String attribute = EndpointsContext.class.getName();
        EndpointsContext endpointsContext = EndpointsContext.class.cast( context.get().getAttribute( attribute ) );
        if ( endpointsContext == null )
        {
            String msg = EndpointsContext.class.getSimpleName() + " cannot be null. Configure in your " +
                    "EndpointsModule#configureEndpoints(..) impl to serve with EndpointsContextAwareServlet";
            throw new NullPointerException( msg );
        }

        String entityId = endpointsContext.getRawPathParameters().get( ENTITY_ID );
        TFrom target;

        if ( Strings.isNullOrEmpty( entityId ) )
        {
            target = createFromInstance( map );
        }
        else
        {
            target = createFromInstance( entityId, map );
        }

        if ( target == null )
        {
            throw new IllegalArgumentException( "The desired 'TFrom' instance cannot be null." );
        }

        return target;
    }

    /**
     * Prepare desired entity instance (for HttpMethod.POST - create) to be updated from the JSON input.
     *
     * @param map the JSON map as a client input from the endpoint
     * @return the instance of desired type
     */
    protected abstract TFrom createFromInstance( Map<String, Object> map );

    /**
     * Prepare desired entity instance (for HttpMethod.PUT - update) to be updated from the JSON input.
     *
     * @param entityId the unique identification of the entity
     * @param map      the JSON map as a client input from the endpoint
     * @return the instance of desired type
     */
    protected abstract TFrom createFromInstance( @Nonnull String entityId, Map<String, Object> map );
}
