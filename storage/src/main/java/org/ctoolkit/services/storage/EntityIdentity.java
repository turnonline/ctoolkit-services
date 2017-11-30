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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

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
     * Cascading save support is optional and implementation specific.
     * <p>
     * Active record pattern:save.
     */
    void save();

    /**
     * Similar to the {@link #save()}.
     * Use if you need more fine-grained control over which relationships of the entity
     * are going to be cascading saved. Cascading save with respecting ignored fields support
     * is optional and implementation specific.
     *
     * @param ignored the tree of the field names to be ignored while saving
     */
    Set<Ignored> save( @Nullable Ignored ignored );

    /**
     * Deletes the entity from the datastore best within transaction.
     * Cascading delete support is optional and implementation specific.
     * <p>
     * Active record pattern:delete.
     */
    void delete();

    /**
     * The tree of the field names (reference to the relationships), to be ignored while cascading save.
     */
    interface Ignored
    {
        /**
         * Adds the entity field name(s) to be ignored while cascading save.
         *
         * @return the current level of ignored to chain
         */
        Ignored ignore( @Nonnull String fieldName, String... fieldNames );

        /**
         * Returns the boolean indicating whether to ignore entity with given field name while cascading save.
         *
         * @param fieldName the entity field name to be checked
         * @return {@code true} to be ignored
         */
        boolean isIgnored( @Nonnull String fieldName );

        /**
         * Adds the next level of the entity field names as a child to be ignored.
         *
         * @param fieldName the property name that is a reference to another entity (relationship)
         *                  with it's own properties (field names) to be ignored while cascading save if any
         * @return the newly created child
         */
        Ignored addChild( @Nonnull String fieldName );

        /**
         * Returns the entity field name for current level. If returns {@code null} it means
         * represents a top level {@link Ignored} instance.
         *
         * @return the entity field name
         */
        String getFieldName();

        /**
         * Search for the child with given entity field name.
         *
         * @param fieldName the entity field name to search
         * @return the set of ignored entity field names or {@code null}
         */
        Ignored search( @Nonnull String fieldName );

        /**
         * Returns the set of children.
         *
         * @return the children
         */
        Set<Ignored> children();
    }

    /**
     * Declaration of the support for {@link Ignored} while cascading save.
     */
    interface HasIgnored
    {
        /**
         * Creates unattached new top level {@link Ignored} instance.
         *
         * @return the newly created instance
         */
        Ignored createIgnored();

        /**
         * Creates attached new top level {@link Ignored} instance.
         *
         * @return the newly created instance
         */
        Ignored newCascading();

        /**
         * Returns attached top level {@link Ignored} instance.
         *
         * @return always returns instance even empty
         */
        Ignored cascading();
    }
}
