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

package org.ctoolkit.services.storage;

/**
 * The entity identity abstraction.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public interface EntityIdentity<P extends EntityIdentity>
{
    /**
     * Returns the unique string identification unique across all entities of all kinds and parents.
     *
     * @return the unique string identification
     */
    String getKey();

    /**
     * Returns the name (type of <code>String</code>) identification
     * unique only for entities with the same kind and parent.
     *
     * @return the name associated with this entity, or null if has an id
     */
    String getName();

    /**
     * Returns the id (type of <code>Long</code>) identification unique only for entities with the same kind and parent.
     *
     * @return the id associated with this entity, or null if has a name
     */
    Long getId();

    /**
     * Returns the datastore kind of this entity.
     *
     * @return the datastore kind associated with this entity
     */
    String getKind();

    /**
     * Returns the version of this entity instance. Incremented on save.
     *
     * @return the version of this entity instance
     */
    Integer getVersion();

    /**
     * Returns the parent identification of this entity instance, or null if there is no parent.
     *
     * @return the parent identification
     */
    P getParent();

    /**
     * Sets the parent identification of this entity instance, or null if there is no parent.
     *
     * @param parent the parent instance to be set
     */
    void setParent( P parent );
}
