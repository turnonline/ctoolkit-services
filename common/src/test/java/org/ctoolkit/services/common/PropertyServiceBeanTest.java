package org.ctoolkit.services.common;

import junit.framework.Assert;
import net.sf.jsr107cache.CacheFactory;
import org.testng.annotations.Test;

import javax.inject.Inject;

/**
 * The fully functional backend property service test.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class PropertyServiceBeanTest
        extends GuiceBerryTestNgCase
{
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
        // test application is running on testing environment
        PropertyServiceBean psb = new PropertyServiceBean( "localhost", "localhostAsProd", factory );
        Assert.assertFalse( psb.isTestEnvironment() );
        Assert.assertFalse( psb.isProductionEnvironment() );
        Assert.assertTrue( psb.isDevelopmentEnvironment() );

        // test application is running on production environment with ID as localhostAsProd
        System.setProperty( "com.google.appengine.runtime.environment", "Production" );
        System.setProperty( "com.google.appengine.application.id", "localhostAsProd" );
        psb = new PropertyServiceBean( "localhostAsProd", "localhostAsTest", factory );
        Assert.assertFalse( psb.isTestEnvironment() );
        Assert.assertTrue( psb.isProductionEnvironment() );
        Assert.assertFalse( psb.isDevelopmentEnvironment() );

        // test application is running on production environment with ID as localhostAsTest
        System.setProperty( "com.google.appengine.runtime.environment", "Production" );
        System.setProperty( "com.google.appengine.application.id", "localhostAsTest" );
        psb = new PropertyServiceBean( "localhostAsProd", "localhostAsTest", factory );
        Assert.assertTrue( psb.isTestEnvironment() );
        Assert.assertFalse( psb.isProductionEnvironment() );
        Assert.assertFalse( psb.isDevelopmentEnvironment() );
    }

    @Test
    public void preconditions()
    {
        try
        {
            new PropertyServiceBean( null, null, factory );
            Assert.fail( IllegalArgumentException.class.getName() + " should be thrown!" );
        }
        catch ( Exception e )
        {
            Assert.assertEquals( IllegalArgumentException.class, e.getClass() );
        }
        try
        {
            new PropertyServiceBean( null, "localhostAsProd", factory );
            Assert.fail( IllegalArgumentException.class.getName() + " should be thrown!" );
        }
        catch ( Exception e )
        {
            Assert.assertEquals( IllegalArgumentException.class, e.getClass() );
        }
        try
        {
            new PropertyServiceBean( "localhost", null, factory );
            Assert.fail( IllegalArgumentException.class.getName() + " should be thrown!" );
        }
        catch ( Exception e )
        {
            Assert.assertEquals( IllegalArgumentException.class, e.getClass() );
        }
    }
}