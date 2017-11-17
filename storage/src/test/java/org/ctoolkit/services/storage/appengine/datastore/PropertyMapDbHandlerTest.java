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

package org.ctoolkit.services.storage.appengine.datastore;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.googlecode.objectify.Key;
import org.ctoolkit.services.storage.EntityExecutor;
import org.ctoolkit.services.storage.appengine.ServiceTestNgCase;
import org.ctoolkit.services.storage.appengine.TestModule;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * The property map handler tested with datastore.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
@Guice( modules = TestModule.class )
public class PropertyMapDbHandlerTest
        extends ServiceTestNgCase
{
    private static final String PROP_1 = "Property1";

    private static final String PROP_2 = "Property2";

    private static final String PROP_3 = "Property3";

    private static final String PROP_4 = "Property4";

    @Inject
    private EntityExecutor executor;

    @Test
    public void saveAndLoad() throws Exception
    {
        ParentFakeEntity entity = new ParentFakeEntity();
        entity.save();
        Map<String, Object> map = new HashMap<>();

        map.put( PROP_1, 10L );
        map.put( PROP_2, "aValue" );

        // test of first save
        executor.save( entity, map );

        // load dynamic properties to test
        Map<String, Object> result = executor.load( entity );
        assertEquals( result.size(), 2, "Number of elements in dynamic property map is" );
        assertTrue( Maps.difference( map, result ).areEqual() );

        map.remove( PROP_1 );
        map.put( PROP_3, "no no no" );
        map.put( PROP_4, "yes yes" );
        // test of second save, some property should be removed
        executor.save( entity, map );

        // load dynamic properties to test
        result = executor.load( entity );
        assertEquals( result.size(), 3, "Number of elements in dynamic property map is" );
        assertTrue( Maps.difference( map, result ).areEqual() );

        // load low level entity to test removed property
        DatastoreService lowDb = DatastoreServiceFactory.getDatastoreService();
        Entity dbEntity = lowDb.get( Key.create( entity ).getRaw() );
        Map<String, Object> dbProperties = dbEntity.getProperties();
        assertFalse( dbProperties.containsKey( PROP_1 ), "Property '" + PROP_1 + "' should be removed from entity." );

        PropertyMapDbHandler dbHandler = new PropertyMapDbHandler( Key.create( entity ) );
        dbProperties = dbHandler.load( Lists.newArrayList( PROP_3, "whateverBla" ) );
        assertEquals( dbProperties.size(), 1, "Only single property is being" );
        assertTrue( dbProperties.containsKey( PROP_3 ), "Only '" + PROP_3 + "' is being expected." );
    }

    @Test( expectedExceptions = IllegalArgumentException.class )
    public void loadNotFound() throws Exception
    {
        Key<?> key = Key.create( FakeEntity.class, 789L );
        PropertyMapDbHandler dbHandler = new PropertyMapDbHandler( key );
        dbHandler.load();
    }

    @Test( expectedExceptions = IllegalArgumentException.class )
    public void saveNotFound() throws Exception
    {
        Key<?> key = Key.create( FakeEntity.class, 789L );
        PropertyMapDbHandler dbHandler = new PropertyMapDbHandler( key, new HashMap<String, Object>() );
        dbHandler.save();
    }

    @Test( expectedExceptions = IllegalArgumentException.class )
    public void saveNullMap() throws Exception
    {
        Key<?> key = Key.create( FakeEntity.class, 789L );
        PropertyMapDbHandler dbHandler = new PropertyMapDbHandler( key );
        dbHandler.save();
    }
}