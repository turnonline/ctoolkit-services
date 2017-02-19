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

package org.ctoolkit.services.common;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Properties;

/**
 * The service configuration holder represents a map of properties used to configure service.
 * <p>
 * Values are part of the binding thus available via injection like:
 * <pre>
 *   public class MyClass {
 *     &#064;Inject <b>@Named("service.property.appId.test")</b> String appId;
 *     ...
 *   }</pre>
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class PropertyConfig
        extends Properties
{
    private static final String SERVICE_ATTR = "service.property.";

    /**
     * Binding property for needs of the DI frameworks.
     */
    private static final String PRODUCTION_APP_ID = "appId.production";

    /**
     * Binding property for needs of the DI frameworks.
     */
    private static final String TEST_APP_ID = "appId.test";

    /**
     * Creates empty configuration instance.
     */
    public PropertyConfig()
    {
    }

    /**
     * Creates configuration instance with pre-filled properties.
     *
     * @param properties the map of values as a source of properties
     */
    public PropertyConfig( @Nonnull Map<String, String> properties )
    {
        for ( String next : properties.keySet() )
        {
            setProperty( next, properties.get( next ) );
        }
    }

    /**
     * Sets the App ID to configure test environment 'service.property.appId.test' property
     * to let service aware in which environment application is running.
     *
     * @param appId the test application ID to be set
     * @return this instance to chain
     */
    public PropertyConfig setTestAppI( String appId )
    {
        if ( !isNullOrEmpty( appId ) )
        {
            setProperty( SERVICE_ATTR + TEST_APP_ID, appId );
        }
        return this;
    }

    /**
     * Sets the App ID to configure production environment 'service.property.appId.production' property
     * to let service aware in which environment application is running.
     *
     * @param appId the production application ID to be set
     * @return this instance to chain
     */
    public PropertyConfig setProductionAppI( String appId )
    {
        if ( !isNullOrEmpty( appId ) )
        {
            setProperty( SERVICE_ATTR + PRODUCTION_APP_ID, appId );
        }
        return this;
    }

    private boolean isNullOrEmpty( String string )
    {
        return string == null || string.length() == 0;
    }
}
