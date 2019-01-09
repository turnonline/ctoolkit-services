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

import com.google.api.control.ServiceManagementConfigFilter;
import com.google.api.control.extensions.appengine.GoogleAppEngineControlFilter;
import com.google.appengine.api.utils.SystemProperty;
import com.google.inject.servlet.ServletModule;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * The endpoints control configuration to let you monitor this API in the cloud console.
 * It configures following filter properties:
 * <ul>
 * <li>endpoints.projectId: taken in runtime {@code SystemProperty.applicationId.get()}</li>
 * <li>endpoints.serviceName: constructed like {@code System.getenv( "ENDPOINTS_SERVICE_NAME" )}</li>
 * </ul>
 * Once added in local development, will connect remotely to the cloud console with 'endpoints.serviceName'
 * and all activities will be monitored. Keep in mind access must be granted to the 'endpoints.serviceName'
 * (Endpoints/Share API) otherwise exception will be raised.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 * @see <a href="https://console.cloud.google.com/endpoints">Cloud Endpoints Console</a>
 */
public class EndpointsMonitorConfig
        extends ServletModule
{
    public static final String ENDPOINTS_SERVLET_PATH = "/api/*";

    @Override
    protected void configureServlets()
    {
        bind( ServiceManagementConfigFilter.class ).in( Singleton.class );
        filter( ENDPOINTS_SERVLET_PATH ).through( ServiceManagementConfigFilter.class );

        String projectId = SystemProperty.applicationId.get();
        String serviceName = System.getenv( "ENDPOINTS_SERVICE_NAME" );

        Map<String, String> apiController = new HashMap<>();
        apiController.put( "endpoints.projectId", projectId );
        apiController.put( "endpoints.serviceName", serviceName );

        bind( GoogleAppEngineControlFilter.class ).in( Singleton.class );
        filter( ENDPOINTS_SERVLET_PATH ).through( GoogleAppEngineControlFilter.class, apiController );
    }
}
