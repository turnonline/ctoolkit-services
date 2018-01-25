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

package org.ctoolkit.services.storage.appengine.objectify;

import com.googlecode.objectify.Key;
import org.ctoolkit.services.storage.EntityExecutor;
import org.ctoolkit.services.storage.EntityIdentity;
import org.ctoolkit.services.storage.criteria.Criteria;

import javax.annotation.Nonnull;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Objectify entity executor implementation.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
@Singleton
public class ObjectifyEntityExecutor
        implements EntityExecutor
{
    @Override
    @SuppressWarnings( value = "unchecked" )
    public <T> List<T> list( @Nonnull Criteria<T> criteria )
    {
        return new ObjectifyCriteriaBuilder<T>().build( criteria ).list();
    }

    @Override
    public <T> T first( @Nonnull Criteria<T> criteria )
    {
        List<T> list = new ObjectifyCriteriaBuilder<T>().build( criteria ).list();
        return list.isEmpty() ? null : list.get( 0 );
    }

    @Override
    public <T> List<Long> fetchIds( @Nonnull Criteria<T> criteria )
    {
        ObjectifyCriteriaBuilder<T> builder = new ObjectifyCriteriaBuilder<>();
        com.googlecode.objectify.cmd.Query<T> build = builder.build( criteria );

        List<Long> ids = new ArrayList<>();

        for ( Key<T> key : build.keys() )
        {
            ids.add( key.getId() );
        }

        return ids;
    }

    @Override
    public <T> List<String> fetchNames( @Nonnull Criteria<T> criteria )
    {
        ObjectifyCriteriaBuilder<T> builder = new ObjectifyCriteriaBuilder<>();
        com.googlecode.objectify.cmd.Query<T> build = builder.build( criteria );

        List<String> ids = new ArrayList<>();

        for ( Key<T> key : build.keys() )
        {
            ids.add( key.getName() );
        }

        return ids;
    }

    @Override
    public void save( @Nonnull EntityIdentity entity, @Nonnull Map<String, Object> map )
    {
        Key<?> key = Key.create( entity );
        PropertyMapDbHandler dbHandler = new PropertyMapDbHandler( key, map );

        dbHandler.save();
    }

    @Override
    public Map<String, Object> load( @Nonnull EntityIdentity entity )
    {
        Key<?> key = Key.create( entity );
        PropertyMapDbHandler dbHandler = new PropertyMapDbHandler( key );

        return dbHandler.load();
    }
}