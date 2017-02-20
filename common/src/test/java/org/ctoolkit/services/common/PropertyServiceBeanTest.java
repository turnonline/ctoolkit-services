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

import junit.framework.Assert;
import net.sf.jsr107cache.CacheFactory;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * The fully functional backend property service test.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class PropertyServiceBeanTest
        extends GuiceBerryTestNgCase
{
    private static final String PRODUCTION_PROPERTY = "service.property.appId.production";

    private static final String TEST_PROPERTY = "service.property.appId.test";

    @Inject
    CacheFactory factory;

    @Inject
    private PropertyService propertyService;

    @Test
    public void setStringProperty() throws Exception
    {
        String key = "string-property";
        String value = "string-value";
        String modifiedValue = "modified-string-value";

        // set property
        propertyService.setString( key, value );

        // test get property
        Assert.assertEquals( value, propertyService.getString( key ) );

        // set property once again with different value
        propertyService.setString( key, modifiedValue );

        // test modified property value
        Assert.assertEquals( modifiedValue, propertyService.getString( key ) );

        // set property to null
        propertyService.setString( key, null );

        // test if property is null
        Assert.assertNull( propertyService.getString( key ) );
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
        Assert.assertEquals( value, propertyService.getDouble( key ) );

        // set property once again with different value
        propertyService.setDouble( key, modifiedValue );

        // test modified property value
        Assert.assertEquals( modifiedValue, propertyService.getDouble( key ) );

        // set property to null
        propertyService.setDouble( key, null );

        // test if property is null
        Assert.assertNull( propertyService.getDouble( key ) );
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
        Assert.assertEquals( value, propertyService.getInteger( key ) );

        // set property once again with different value
        propertyService.setInteger( key, modifiedValue );

        // test modified property value
        Assert.assertEquals( modifiedValue, propertyService.getInteger( key ) );

        // set property to null
        propertyService.setInteger( key, null );

        // test if property is null
        Assert.assertNull( propertyService.getInteger( key ) );
    }

    @Test
    public void isTestingEnvironment()
    {
        Map<String, String> config = new HashMap<>();
        config.put( PRODUCTION_PROPERTY, "localhost" );
        config.put( TEST_PROPERTY, "localhostAsProd" );

        // test application is running on testing environment
        PropertyServiceBean psb = new PropertyServiceBean( factory, config );

        Assert.assertFalse( psb.isTestEnvironment() );
        Assert.assertFalse( psb.isProductionEnvironment() );
        Assert.assertTrue( psb.isDevelopmentEnvironment() );

        // test application is running on production environment with ID as localhostAsProd
        config.clear();
        config.put( PRODUCTION_PROPERTY, "localhostAsProd" );
        config.put( TEST_PROPERTY, "localhostAsTest" );

        System.setProperty( "com.google.appengine.runtime.environment", "Production" );
        System.setProperty( "com.google.appengine.application.id", "localhostAsProd" );
        psb = new PropertyServiceBean( factory, config );

        Assert.assertFalse( psb.isTestEnvironment() );
        Assert.assertTrue( psb.isProductionEnvironment() );
        Assert.assertFalse( psb.isDevelopmentEnvironment() );

        // test application is running on production environment with ID as localhostAsTest
        config.clear();
        config.put( PRODUCTION_PROPERTY, "localhostAsProd" );
        config.put( TEST_PROPERTY, "localhostAsTest" );

        System.setProperty( "com.google.appengine.runtime.environment", "Production" );
        System.setProperty( "com.google.appengine.application.id", "localhostAsTest" );
        psb = new PropertyServiceBean( factory, config );

        Assert.assertTrue( psb.isTestEnvironment() );
        Assert.assertFalse( psb.isProductionEnvironment() );
        Assert.assertFalse( psb.isDevelopmentEnvironment() );
    }
}