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

package org.ctoolkit.services.endpoints;

import com.google.common.base.Optional;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;

import java.util.Date;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The {@link com.google.api.server.spi.config.ResourceTransformer} helper methods.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class TransformerHelper
{
    /**
     * Converts to <code>Boolean</code> value if presented.
     *
     * @param map the map of properties taken from input JSON
     * @param key the JSON property as a key
     * @return the optional <code>Boolean</code> value
     */
    public static Optional<Boolean> toBoolean( Map<String, Object> map, String key )
    {
        checkNotNull( key );
        return Optional.fromNullable( ( Boolean ) map.get( key ) );
    }

    /**
     * Converts to <code>String</code> value if presented.
     *
     * @param map the map of properties taken from input JSON
     * @param key the JSON property as a key
     * @return the optional <code>String</code> value
     */
    public static Optional<String> getString( Map<String, Object> map, String key )
    {
        checkNotNull( key );
        return Optional.fromNullable( ( String ) map.get( key ) );
    }

    /**
     * Converts to <code>Integer</code> value if presented.
     *
     * @param map the map of properties taken from input JSON
     * @param key the JSON property as a key
     * @return the optional <code>Integer</code> value
     */
    public static Optional<Integer> toInteger( Map<String, Object> map, String key )
    {
        checkNotNull( key );
        return Optional.fromNullable( ( Integer ) map.get( key ) );
    }

    /**
     * Converts to <code>Long</code> value if presented.
     *
     * @param map the map of properties taken from input JSON
     * @param key the JSON property as a key
     * @return the optional <code>Long</code> value
     */
    public static Optional<Long> toLong( Map<String, Object> map, String key )
    {
        checkNotNull( key );
        return Optional.fromNullable( ( Long ) map.get( key ) );
    }

    /**
     * Converts to <code>Double</code> value if presented.
     *
     * @param map the map of properties taken from input JSON
     * @param key the JSON property as a key
     * @return the optional <code>Double</code> value
     */
    public static Optional<Double> toDouble( Map<String, Object> map, String key )
    {
        checkNotNull( key );
        return Optional.fromNullable( ( Double ) map.get( key ) );
    }

    /**
     * Converts to <code>Date</code> value if presented.
     *
     * @param map the map of properties taken from input JSON
     * @param key the JSON property as a key
     * @return the optional <code>Date</code> value
     */
    public static Optional<Date> toDate( Map<String, Object> map, String key )
    {
        checkNotNull( key );
        return Optional.fromNullable( ( Date ) map.get( key ) );
    }

    /**
     * Converts to <code>Map<String, String></code> value if presented.
     *
     * @param map the map of properties taken from input JSON
     * @param key the JSON property as a key
     * @return the optional <code>Map<String, String></code> map
     */
    @SuppressWarnings( "unchecked" )
    public static Optional<Map<String, String>> toMap( Map<String, Object> map, String key )
    {
        checkNotNull( key );
        return Optional.fromNullable( ( Map<String, String> ) map.get( key ) );
    }

    /**
     * Puts to map only a non null value for given key.
     *
     * @param map   the map of properties to be serialized to JSON
     * @param key   the JSON property as a key
     * @param value the JSON property value
     */
    public static void put( Map<String, Object> map, String key, Object value )
    {
        checkNotNull( map );
        checkNotNull( key );

        if ( value != null )
        {
            map.put( key, value );
        }
    }

    /**
     * Example of usage:
     * <pre>
     * {@code
     * TypeToken<Map<String, String>> subsidiaryMapType;
     *  subsidiaryMapType = mapOf( TypeToken.of( String.class ), TypeToken.of( String.class ) );
     * }
     * </pre>
     */
    public static <K, V> TypeToken<Map<K, V>> mapOf( TypeToken<K> keyToken, TypeToken<V> valueToken )
    {
        return new TypeToken<Map<K, V>>()
        {
        }
                .where( new TypeParameter<K>()
                {
                }, keyToken )
                .where( new TypeParameter<V>()
                {
                }, valueToken );
    }
}
