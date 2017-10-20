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

package org.ctoolkit.services.guice;

import com.google.appengine.api.utils.SystemProperty;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

/**
 * The App Engine context listener to distinguish between configurations either for
 * Production (running on App Engine in cloud) or for Development (local) environment.
 * Based on the environment dedicated injector will be initialized.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public abstract class AppEngineEnvironmentContextListener
        extends GuiceServletContextListener
{

    @Override
    protected final Injector getInjector()
    {
        if ( SystemProperty.environment.value() == SystemProperty.Environment.Value.Production )
        {
            // The app is running on App Engine...
            return getProductionInjector();
        }
        else
        {
            // The app is running on local development SDK...
            return getDevelopmentInjector();
        }
    }

    /**
     * Initializes dedicated {@link Injector} intended for development use.
     * Override this method to create injector running on local App Engine SDK.
     *
     * @return the guice injector
     */
    protected abstract Injector getDevelopmentInjector();

    /**
     * Initializes dedicated {@link Injector} intended for production use.
     * Override this method to create injector aimed to be used on Google Cloud - App Engine.
     *
     * @return the guice injector
     */
    protected abstract Injector getProductionInjector();
}
