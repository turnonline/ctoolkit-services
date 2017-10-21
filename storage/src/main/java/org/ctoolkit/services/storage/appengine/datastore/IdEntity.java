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

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Id;
import org.ctoolkit.services.storage.EntityIdentity;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The objectified id entity. The @Id as 'id', type of <code>Long</code>. If 'id' is not set
 * (null value), will be set automatically by datastore engine.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public abstract class IdEntity<P extends EntityIdentity>
        extends BaseEntity<P>
{
    private static final long serialVersionUID = 1L;

    @Id
    protected Long id;

    /**
     * Constructs a new instance.
     */
    public IdEntity()
    {
    }

    /**
     * Constructs a new instance with given ID as instance identification.
     *
     * @param id the entity ID to be set
     */
    public IdEntity( Long id )
    {
        this.id = checkNotNull( id );
    }

    @Override
    public String getName()
    {
        return null;
    }

    @Override
    public Long getId()
    {
        return id;
    }

    /**
     * Manually sets the ID of this entity instance.
     *
     * @param id the instance ID to be set
     */
    protected void setId( Long id )
    {
        this.id = id;
    }

    /**
     * Returns the unique string identification unique across all entities.
     *
     * @return the unique string identification
     */
    public String getKey()
    {
        if ( this.id == null )
        {
            return null;
        }

        return Key.create( this ).getString();
    }

    /**
     * Returns the objectify reference of this instance.
     *
     * @return the objectify reference
     */
    @SuppressWarnings( "unchecked" )
    <T extends IdEntity> Ref<T> ref()
    {
        if ( this.id == null )
        {
            return null;
        }

        return Ref.create( ( T ) this );
    }

    @Override
    public String getKind()
    {
        return this.getClass().getSimpleName();
    }

    /**
     * Override if needed.
     */
    @Override
    public P getParent()
    {
        return null;
    }

    /**
     * Override if needed.
     */
    @Override
    public void setParent( P parent )
    {
    }

    @Override
    public String toString()
    {
        return "{id=" + id + "} " + super.toString();
    }
}
