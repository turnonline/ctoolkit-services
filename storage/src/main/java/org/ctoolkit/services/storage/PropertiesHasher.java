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

package org.ctoolkit.services.storage;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.hash.Funnel;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.TreeMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The contract to manage group of entity properties overall hashcode.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public interface PropertiesHasher
{
    /**
     * Calculates (in memory only) the hashcode based on the client selected properties.
     * It does not affect the persisted hashcode.
     *
     * @return the final hash code
     */
    String calcPropsHashCode();

    /**
     * Calculates (in memory only) the hashcode. It's based on the client provided map of the properties.
     * It does not affect the persisted hashcode.
     *
     * @param propertiesMap the map of the key-value properties that might contain nested map (key - another map)
     * @return the final hash code
     */
    @SuppressWarnings( "UnstableApiUsage" )
    default String calcPropsHashCode( @Nonnull Map<String, Object> propertiesMap )
    {
        checkNotNull( propertiesMap );

        Funnel<Map<String, Object>> funnel;
        funnel = ( Funnel<Map<String, Object>> ) ( from, into ) -> from.forEach( ( property, value ) -> {
            if ( value instanceof String )
            {
                into.putString( ( String ) value, Charsets.UTF_8 );
            }
            else if ( value instanceof Long )
            {
                into.putLong( ( Long ) value );
            }
            else if ( value instanceof Integer )
            {
                into.putInt( ( Integer ) value );
            }
            else if ( value instanceof Double )
            {
                into.putDouble( ( Double ) value );
            }
            else if ( value instanceof Boolean )
            {
                into.putBoolean( ( Boolean ) value );
            }
            else if ( value instanceof Float )
            {
                into.putFloat( ( Float ) value );
            }
            else if ( value instanceof Character )
            {
                into.putChar( ( Character ) value );
            }
        } );

        Map<String, Object> flatMap = flatMap( propertiesMap, null );

        Hasher hasher = Hashing.sha256().newHasher();
        hasher = hasher.putObject( flatMap, funnel );

        return hasher.hash().toString();
    }

    /**
     * Flattens the given map to the flat map of the properties where the original property
     * nested within another map will have a dot separated key, for example 'postalAddress.firstName'.
     * <p>
     * It's using the natural ordering of its keys.
     *
     * @param input     the map of the key-value properties that might contain nested map
     * @param parentKey the key that will act as a parent key separated by dot
     * @return the flattened map of the properties
     */
    default Map<String, Object> flatMap( @Nonnull Map<String, Object> input, @Nullable String parentKey )
    {
        checkNotNull( input );

        // Preferred TreeMap used here to maintain key ordering
        Map<String, Object> flatMap = new TreeMap<>();

        input.forEach( ( key, value ) -> {
            String newKey = Strings.isNullOrEmpty( parentKey ) ? key : ( parentKey + "." + key );
            if ( value instanceof Map )
            {
                @SuppressWarnings( "unchecked" ) Map<String, Object> nested = ( Map<String, Object> ) value;
                flatMap.putAll( flatMap( nested, newKey ) );
            }
            else
            {
                flatMap.put( newKey, value );
            }
        } );
        return flatMap;
    }

    /**
     * Returns the associated properties hashcode entity instance.
     * It returns {@code null} if there is nothing to calculate.
     *
     * @return the properties hashcode entity instance
     */
    PropertiesHashCode getPropsHashCode();

    /**
     * Calculates and persists the current snapshot of the entity properties hashcode.
     *
     * @return true if recalculated properties hashcode has been applied
     */
    default boolean putPropsHashCode()
    {
        PropertiesHashCode propsHashCode = getPropsHashCode();
        if ( propsHashCode == null )
        {
            return false;
        }
        propsHashCode.putHashCode( this );
        propsHashCode.save();
        return true;
    }

    /**
     * Checks whether current values of the client selected properties has changed.
     * Returns {@code false} if {@link #getPropsHashCode()} returns {@code null}.
     *
     * @return true if any of the value has changed
     */
    default boolean isPropsHashCodeChanged()
    {
        PropertiesHashCode propsHashCode = getPropsHashCode();
        if ( propsHashCode == null )
        {
            return false;
        }
        return !calcPropsHashCode().equals( propsHashCode.getHashCode() );
    }
}
