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

import com.googlecode.objectify.annotation.Entity;
import org.ctoolkit.services.storage.appengine.objectify.EntityLongIdentity;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.googlecode.objectify.ObjectifyService.ofy;
import static org.ctoolkit.services.storage.PropertiesHasher.DEFAULT;

/**
 * Extend this class to have its own implementation annotated with {@link Entity}
 * <p>
 * The entity to store hashcode evaluated from client selected related properties.
 * Kept as separate entity that will be <strong>loaded</strong> and <strong>updated</strong> only
 * <strong>if needed</strong>.
 * Thus will not have an impact on performance of the associated entity while loading from the datastore
 * and updates will be managed outside of the associated entity.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public abstract class PropertiesHashCode
        extends EntityLongIdentity
{
    private Map<String, String> hashCodes;

    public PropertiesHashCode()
    {
        hashCodes = new HashMap<>();
    }

    /**
     * Calculates and assigns the current value of the entity properties HashCode.
     *
     * @param name   the name to be associated with specified hasher
     * @param hasher the managing hasher
     */
    boolean snapshot( @Nonnull String name, @Nonnull PropertiesHasher hasher )
    {
        checkNotNull( hasher );
        checkNotNull( name );
        String hashCode = hasher.calcPropsHashCode( name );
        if ( hashCode == null )
        {
            return false;
        }
        this.hashCodes.put( name, hashCode );
        return true;
    }

    /**
     * Returns the persisted value of the hashcode.
     *
     * @return the current hashcode
     */
    public String getHashCode()
    {
        return getHashCode( DEFAULT );
    }

    /**
     * Returns the persisted value of the hashcode.
     *
     * @param name the name of the hasher for which to get the hash code
     * @return the current hashcode
     */
    public String getHashCode( @Nonnull String name )
    {
        checkNotNull( name );
        return hashCodes.get( name );
    }

    @Override
    public void save()
    {
        ofy().transact( () -> ofy().save().entity( this ).now() );
    }

    @Override
    public void delete()
    {
        ofy().transact( () -> ofy().delete().entity( this ).now() );
    }
}
