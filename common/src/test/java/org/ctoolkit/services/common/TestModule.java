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

package org.ctoolkit.services.common;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.ctoolkit.services.guice.CtoolkitServicesAppEngineModule;

import javax.cache.Cache;

/**
 * The guice module configuration for testing purpose only.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class TestModule
        extends AbstractModule
{
    @Override
    protected void configure()
    {
        install( new CtoolkitServicesAppEngineModule() );
        install( new CtoolkitCommonServicesModule() );

        bind( Cache.class ).toProvider( JCacheProvider.class );

        PropertyConfig config = new PropertyConfig();
        config.setTestAppI( "localhost" );
        config.setProductionAppI( "localhostAsProd" );
        Names.bindProperties( binder(), config );
    }
}
