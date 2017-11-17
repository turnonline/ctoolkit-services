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
 * The entity identity abstraction with generic type of the ID.
 *
 * @param <ID_TYPE> the type of the ID of this entity
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public interface EntityIdentity<ID_TYPE>
{
    /**
     * Returns the unique string identification unique across all entities of all kinds and parents.
     *
     * @return the unique string identification
     */
    String getKey();

    /**
     * Returns the identification unique only for entities with the same kind and parent.
     *
     * @return the id associated with this entity, or null if has a name
     */
    ID_TYPE getId();

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
     * Saves (creates or updates) this entity in to the datastore best within transaction.
     * Cascading save support is optional.
     * <p>
     * Active record pattern:save.
     */
    void save();

    /**
     * Deletes the entity from the datastore best within transaction.
     * Cascading delete support is optional.
     * <p>
     * Active record pattern:delete.
     */
    void delete();
}
