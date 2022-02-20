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

import javax.cache.Cache;

/**
 * The set of convenient methods to handle application properties.
 * + {@link Cache} instantiation.
 *
 * @author <a href="mailto:jozef.pohorelec@ctoolkit.org">Jozef Pohorelec</a>
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public interface PropertyService
{
    /**
     * Creates a Cache instance using the namespace parameter with default configuration.
     * <p>
     * Returns the fake Cache instance in case of the Cache instance creation failure (logged) with no real cache
     * functionality.
     * <p>
     * Note:
     * The maximum size of a cached data value is 1 MiB (2^20 bytes) minus the size of the key minus
     * an implementation-dependent overhead, which is approximately 73 bytes. A key cannot be larger than 250 bytes.
     * In the Java runtime, keys that are objects or strings longer than 250 bytes will be hashed.
     *
     * @return the cache instance.
     */
    Cache create();

    /**
     * Return property as String for specified property key.
     *
     * @param key property key
     * @return property as String
     * @throws IllegalArgumentException if property for specified key does not exists
     */
    String getString( String key );

    /**
     * Set property as String for specified key
     *
     * @param key   property key
     * @param value property value
     */
    void setString( String key, String value );

    /**
     * Return property as Double for specified property key.
     *
     * @param key property key
     * @return property as Double
     * @throws IllegalArgumentException if property for specified key does not exists
     */
    Double getDouble( String key );

    /**
     * Set property as Double for specified key
     *
     * @param key   property key
     * @param value property value
     */
    void setDouble( String key, Double value );

    /**
     * Return property as Integer for specified property key.
     *
     * @param key property key
     * @return property as Integer
     * @throws IllegalArgumentException if property for specified key does not exists
     */
    Integer getInteger( String key );

    /**
     * Set property as Integer for specified key
     *
     * @param key   property key
     * @param value property value
     */
    void setInteger( String key, Integer value );

    /**
     * Returns <code>true</code> if current application is running marked as PRODUCTION environment.
     * That means on exactly defined App Engine AppId, otherwise returns <code>false</code>.
     *
     * @return true if current application is running in production environment
     */
    boolean isProductionEnvironment();

    /**
     * Returns <code>true</code> if current application is running marked as TEST environment.
     * That means on exactly defined App Engine AppId, otherwise returns <code>false</code>.
     *
     * @return true if current application is running in test environment
     */
    boolean isTestEnvironment();

    /**
     * Returns <code>true</code> if current application is running in DEVELOPMENT environment (SDK). If so methods
     * {@link #isProductionEnvironment()} and  {@link #isTestEnvironment()} returns false.
     *
     * @return true if current application is running in local development mode
     */
    boolean isDevelopmentEnvironment();

    /**
     * Returns the AppSpot current secure URL at '.appspot.com' with subdomain (application Id).
     * For example application Id 'turn-online' - http://rest-api.turn-online.appspot.com
     *
     * @return the current secure URL at '.appspot.com'
     */
    String getAppSpotURL();

    /**
     * Returns the AppSpot current secure URL at '.appspot.com' with subdomain (application Id).
     * Optionally with subdomain prefix - App Engine module name for example
     * with prefix 'rest-api' and application Id 'turn-online' - http://rest-api-dot-turn-online.appspot.com
     *
     * @param prefix the module prefix
     * @return the current secure URL at '.appspot.com'
     */
    String getAppSpotURL( String prefix );
}