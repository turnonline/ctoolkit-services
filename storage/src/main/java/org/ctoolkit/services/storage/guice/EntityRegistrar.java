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

/**
 * The class delegates timing of the objectify entities registration to the Guice,
 * with help of implementation of the {@link GuicefiedOfyFactory}.
 * <p>
 * The implementation of this interface to be taken into account must be configured via Guice:
 * <pre>
 *  Multibinder&#60;EntityRegistrar&#62; registrar = Multibinder.newSetBinder( binder(), EntityRegistrar.class );
 *  registrar.addBinding().to( MyEntityRegistrar.class );
 * </pre>
 * Before using Objectify to load or save data, you must register all entity classes used in application.
 * In order to make sure all registration occurs in right time (either for production use or in tests) use
 * implementation of {@link EntityRegistrar} to register all service entities.
 * <p>
 * To turn it on, somewhere in module use: {@code bind( GuicefiedOfyFactory.class ).asEagerSingleton();}
 * and leverage full Guice DI in entities too.
 *
 * @author <a href="mailto:medvegy@turnonline.biz">Aurel Medvegy</a>
 */
public interface EntityRegistrar
{
    /**
     * Provide your own list of entities to be registered with Objectify, for example:
     * {@code factory.register( Product.class );}
     * <p>
     * Entities registered here will leverage full Guice injection.
     *
     * @param factory the factory used for entity registration
     */
    void register( GuicefiedOfyFactory factory );
}
