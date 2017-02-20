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
import org.ctoolkit.services.storage.EntityIdentity;

/**
 * The GWT compatible id entity. The @Id as 'id', type of <code>Long</code>. If 'id' is not set (null value),
 * will be set automatically by datastore engine.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public abstract class GwtIdEntity<P extends EntityIdentity>
        extends BaseEntity<P>
{
    private static final long serialVersionUID = 1L;

    @Id
    protected Long id;

    @Override
    public String getKey()
    {
        return null;
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

    @Override
    public String getKind()
    {
        return this.getClass().getSimpleName();
    }

    @Override
    public P getParent()
    {
        return null;
    }

    @Override
    public String toString()
    {
        return "{id=" + id + "} " + super.toString();
    }
}
