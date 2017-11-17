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

import com.googlecode.objectify.annotation.Id;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The base objectify entity to be used in the client code.
 * The @Id of the entity with type of <code>Long</code>. If 'Id' is not set (null value)
 * a numeric value will be generated for you using the standard App Engine allocator.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 * @see <a href="https://github.com/objectify/objectify/wiki/Entities">Entities</a>
 */
public abstract class EntityLongIdentity
        extends BaseEntityIdentity<Long>
{
    /**
     * Objectify checks the explicit type, cannot be generic.
     */
    @Id
    private Long id;

    /**
     * Constructs a new instance.
     */
    public EntityLongIdentity()
    {
    }

    /**
     * Constructs a new instance with given ID as an instance identification.
     *
     * @param id the entity ID to be set
     */
    public EntityLongIdentity( Long id )
    {
        this.id = checkNotNull( id );
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
}
