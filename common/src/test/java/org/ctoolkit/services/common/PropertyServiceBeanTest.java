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

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheFactory;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;

/**
 * The fully functional backend property service test.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class PropertyServiceBeanTest
        extends BackendServiceTestCase
{
    private static final String PRODUCTION_PROPERTY = "service.property.appId.production";

    private static final String TEST_PROPERTY = "service.property.appId.test";

    @Inject
    private CacheFactory factory;

    @Inject
    private PropertyService propertyService;

    // not used directly in the test, just making sure implementation for Cache has been bound
    @Inject
    private Cache cache;

    @Test
    public void setStringProperty() throws Exception
    {
        String key = "string-property";
        String value = "string-value";
        String modifiedValue = "modified-string-value";

        // set property
        propertyService.setString( key, value );

        // test get property
        assertThat( propertyService.getString( key ) ).isEqualTo( value );

        // set property once again with different value
        propertyService.setString( key, modifiedValue );

        // test modified property value
        assertThat( propertyService.getString( key ) ).isEqualTo( modifiedValue );

        // set property to null
        propertyService.setString( key, null );

        // test if property is null
        assertThat( propertyService.getString( key ) ).isNull();
    }

    @Test
    public void setLongStringProperty() throws Exception
    {
        String key = "long-string-property";
        InputStream stream = PropertyServiceBeanTest.class.getResourceAsStream( "text.properties" );
        String value = CharStreams.toString( new InputStreamReader( stream, Charsets.UTF_8.name() ) );

        // set property
        propertyService.setString( key, value );

        // test get property
        assertThat( propertyService.getString( key ) ).isEqualTo( value );
    }

    @Test
    public void setDoubleProperty()
    {
        String key = "double-property";
        Double value = 1D;
        Double modifiedValue = 2D;

        // set property
        propertyService.setDouble( key, value );

        // test get property
        assertThat( propertyService.getDouble( key ) ).isEqualTo( value );

        // set property once again with different value
        propertyService.setDouble( key, modifiedValue );

        // test modified property value
        assertThat( propertyService.getDouble( key ) ).isEqualTo( modifiedValue );

        // set property to null
        propertyService.setDouble( key, null );

        // test if property is null
        assertThat( propertyService.getDouble( key ) ).isNull();
    }

    @Test
    public void setIntegerProperty()
    {
        String key = "integer-property";
        Integer value = 1;
        Integer modifiedValue = 2;

        // set property
        propertyService.setInteger( key, value );

        // test get property
        assertThat( propertyService.getInteger( key ) ).isEqualTo( value );

        // set property once again with different value
        propertyService.setInteger( key, modifiedValue );

        // test modified property value
        assertThat( propertyService.getInteger( key ) ).isEqualTo( modifiedValue );

        // set property to null
        propertyService.setInteger( key, null );

        // test if property is null
        assertThat( propertyService.getInteger( key ) ).isNull();
    }

    @Test
    public void isTestingEnvironment()
    {
        Map<String, String> config = new HashMap<>();
        config.put( PRODUCTION_PROPERTY, "localhost" );
        config.put( TEST_PROPERTY, "localhostAsProd" );

        // test application is running on testing environment
        PropertyServiceBean psb = new PropertyServiceBean( factory, config );

        assertThat( psb.isTestEnvironment() ).isFalse();
        assertThat( psb.isProductionEnvironment() ).isFalse();
        assertThat( psb.isDevelopmentEnvironment() ).isTrue();

        // test application is running on production environment with ID as localhostAsProd
        config.clear();
        config.put( PRODUCTION_PROPERTY, "localhostAsProd" );
        config.put( TEST_PROPERTY, "localhostAsTest" );

        System.setProperty( "com.google.appengine.runtime.environment", "Production" );
        System.setProperty( "com.google.appengine.application.id", "localhostAsProd" );
        psb = new PropertyServiceBean( factory, config );

        assertThat( psb.isTestEnvironment() ).isFalse();
        assertThat( psb.isProductionEnvironment() ).isTrue();
        assertThat( psb.isDevelopmentEnvironment() ).isFalse();

        // test application is running on production environment with ID as localhostAsTest
        config.clear();
        config.put( PRODUCTION_PROPERTY, "localhostAsProd" );
        config.put( TEST_PROPERTY, "localhostAsTest" );

        System.setProperty( "com.google.appengine.runtime.environment", "Production" );
        System.setProperty( "com.google.appengine.application.id", "localhostAsTest" );
        psb = new PropertyServiceBean( factory, config );

        assertThat( psb.isTestEnvironment() ).isTrue();
        assertThat( psb.isProductionEnvironment() ).isFalse();
        assertThat( psb.isDevelopmentEnvironment() ).isFalse();
    }
}