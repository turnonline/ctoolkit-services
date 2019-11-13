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

package org.ctoolkit.services.storage.guice;

import com.google.inject.Injector;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;

/**
 * Objectify factory with plugged-in Guice to be responsible for entity instantiation.
 *
 * @author <a href="mailto:medvegy@turnonline.biz">Aurel Medvegy</a>
 */
@Singleton
public class GuicefiedOfyFactory
        extends ObjectifyFactory
{
    private final Injector injector;

    @Inject
    public GuicefiedOfyFactory( Injector injector, Set<EntityRegistrar> configs )
    {
        this.injector = injector;

        ObjectifyService.init( this );
        for ( EntityRegistrar next : configs )
        {
            next.register( this );
        }
    }

    @Override
    public <T> T construct( Class<T> type )
    {
        return injector.getInstance( type );
    }
}
