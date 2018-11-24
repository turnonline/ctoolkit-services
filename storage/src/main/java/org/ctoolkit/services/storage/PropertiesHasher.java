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
     * @return the hash code
     */
    String calcPropsHashCode();

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
